package com.findata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;

public class ClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    // ISO 8601 formatı, milisaniyelerle ve UTC ('Z') olarak. Örn: 2025-04-01T10:47:46.123Z
    private static final DateTimeFormatter ISO_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));


    private final Socket clientSocket;
    private final Map<String, Rate> rates; // Paylaşılan rate verisi
    private final Config config;
    private final Map<String, ScheduledFuture<?>> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler; // Dışarıdan verilen scheduler

    public ClientHandler(Socket socket, Map<String, Rate> rates, Config config, ScheduledExecutorService scheduler) {
        this.clientSocket = socket;
        this.rates = rates;
        this.config = config;
        this.scheduler = scheduler; // Paylaşılan scheduler'ı kullan
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            log.info("Client connected: {}", clientSocket.getInetAddress());

            String request;
            while ((request = in.readLine()) != null) {
                handleRequest(request.trim(), out);
            }
        } catch (SocketException e) {
            log.info("Client disconnected abruptly: {}", clientSocket.getInetAddress());
        } catch (IOException e) {
            log.error("IOException handling client {}: {}", clientSocket.getInetAddress(), e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void handleRequest(String request, PrintWriter out) {
        try {
            if (request.startsWith("subscribe|")) {
                String rateName = request.substring(request.indexOf('|') + 1);
                subscribe(rateName, out);
            } else if (request.startsWith("unsubscribe|")) {
                String rateName = request.substring(request.indexOf('|') + 1);
                unsubscribe(rateName, out);
            } else {
                log.warn("Invalid request format from {}: {}", clientSocket.getInetAddress(), request);
                out.println("ERROR|Invalid request format");
            }
        } catch (Exception e) {
            log.error("Error processing request '{}' from {}: {}", request, clientSocket.getInetAddress(), e.getMessage());
            out.println("ERROR|Internal server error");
        }
    }

    private void subscribe(String rateName, PrintWriter out) {
        Rate rate = rates.get(rateName);
        if (rate == null) {
            log.warn("Rate not found for subscription request: {}", rateName);
            out.println("ERROR|Rate data not found for " + rateName);
            return;
        }

        // Zaten abone ise, eski yayını iptal et
        stopBroadcast(rateName);

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            try {
                // Önce kuru güncelle (eğer birden fazla abone varsa senkronize şekilde güncellenir)
                rate.update();
                Double[] currentVals = rate.getCurrentValues();
                String timestamp = ISO_TIMESTAMP_FORMATTER.format(Instant.now());

                // Not: Dokümandaki format farklıydı (ID:number:DEĞER).
                // Kullanıcının kodundaki formatı koruyoruz: SEMBOL|bid:DEĞER|ask:DEĞER|timestamp:DEĞER
                String message = String.format(Locale.US, // Ondalık ayırıcı olarak nokta kullan
                        "%s|bid:%.5f|ask:%.5f|timestamp:%s",
                        rateName, currentVals[0], currentVals[1], timestamp
                );

                // PrintWriter thread-safe değildir, ancak her ClientHandler kendi out nesnesini kullanır.
                // Yine de yazma hatasını yakalamak önemlidir.
                if (out.checkError()) { // Önceki yazmada hata oluştu mu kontrol et
                    log.error("PrintWriter error for client {}, stopping broadcasts.", clientSocket.getInetAddress());
                    // Hata durumunda bu task'ı ve belki diğerlerini iptal et
                    throw new IOException("PrintWriter error detected.");
                }
                out.println(message);

            } catch (Exception e) { // Yakalanmayan hatalar veya IO hataları
                log.error("Error during broadcast for rate {} to client {}: {}", rateName, clientSocket.getInetAddress(), e.getMessage(), e);
                // Görevi iptal et ve istemciyi kapatmayı düşün
                stopBroadcast(rateName); // Kendini iptal etmeyi dene
                // Client'ı kapatmak için ana thread'e sinyal göndermek gerekebilir veya burada kapatılabilir.
                // Şimdilik sadece görevi durduralım.
                throw new RuntimeException(e); // Scheduler'ın hatayı yakalaması için tekrar fırlat
            }
        }, 0, config.getBroadcastIntervalMs(), TimeUnit.MILLISECONDS);

        subscriptions.put(rateName, task);
        log.info("Client {} subscribed to {}", clientSocket.getInetAddress(), rateName);
        out.println("Subscribed to " + rateName);
    }

    private void unsubscribe(String rateName, PrintWriter out) {
        if ("all".equalsIgnoreCase(rateName)) {
            stopAllBroadcasts();
            log.info("Client {} unsubscribed from all rates", clientSocket.getInetAddress());
            out.println("Unsubscribed from all rates.");
        } else {
            boolean stopped = stopBroadcast(rateName);
            if(stopped) {
                log.info("Client {} unsubscribed from {}", clientSocket.getInetAddress(), rateName);
                out.println("Unsubscribed from " + rateName);
            } else {
                log.warn("Client {} tried to unsubscribe from non-subscribed rate {}", clientSocket.getInetAddress(), rateName);
                out.println("ERROR|Not subscribed to " + rateName);
            }
        }
    }

    // Belirli bir yayın görevini durdurur
    private boolean stopBroadcast(String rateName) {
        ScheduledFuture<?> task = subscriptions.remove(rateName);
        if (task != null) {
            task.cancel(false); // Devam eden işlemi bitirmesine izin ver
            log.debug("Stopped broadcast task for rate {} for client {}", rateName, clientSocket.getInetAddress());
            return true;
        }
        return false;
    }

    // Bu istemci için tüm yayınları durdurur
    private void stopAllBroadcasts() {
        if (!subscriptions.isEmpty()) {
            log.info("Stopping all ({}) broadcasts for client {}", subscriptions.size(), clientSocket.getInetAddress());
            subscriptions.values().forEach(task -> task.cancel(false));
            subscriptions.clear();
        }
    }

    // Kaynakları temizler
    public void shutdown() {
        stopAllBroadcasts();
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
                log.info("Closed connection for client: {}", clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            log.error("Error closing client socket {}: {}", clientSocket.getInetAddress(), e.getMessage());
        }
    }
}