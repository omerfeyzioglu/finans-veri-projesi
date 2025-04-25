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

/**
 * İstemci bağlantılarını yöneten ve kur verilerini yayınlayan sınıf.
 * <p>
 * Bu sınıf, her bir bağlı istemci için ayrı bir thread olarak çalışır. İstemciden gelen
 * subscribe/unsubscribe komutlarını işler, istenen kurları belirli aralıklarla günceller
 * ve istemciye gönderir.
 * </p>
 * <p>
 * ISO 8601 formatında timestamp içeren, yapılandırılmış mesaj formatını kullanarak
 * kur verilerini istemciye gönderir: SEMBOL|bid:DEĞER|ask:DEĞER|timestamp:DEĞER
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public class ClientHandler implements Runnable {

    /** Loglama için kullanılan Logger nesnesi */
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    
    /** ISO 8601 formatında, milisaniyelerle ve UTC ('Z') olarak zaman damgası formatı */
    private static final DateTimeFormatter ISO_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    /** İstemci ile iletişim kurmak için kullanılan soket */
    private final Socket clientSocket;
    
    /** Tüm kurların paylaşılan haritası */
    private final Map<String, Rate> rates;
    
    /** Sunucu yapılandırma bilgisi */
    private final Config config;
    
    /** İstemcinin abone olduğu kurların zamanlayıcı görevleri */
    private final Map<String, ScheduledFuture<?>> subscriptions = new ConcurrentHashMap<>();
    
    /** Zamanlı görevleri yürütmek için kullanılan thread havuzu */
    private final ScheduledExecutorService scheduler;

    /**
     * Yeni bir istemci bağlantısı yöneticisi oluşturur.
     *
     * @param socket İstemci bağlantı soketi
     * @param rates Tüm kurların saklandığı harita
     * @param config Sunucu yapılandırması
     * @param scheduler Zamanlı görevleri yürütmek için kullanılan thread havuzu
     */
    public ClientHandler(Socket socket, Map<String, Rate> rates, Config config, ScheduledExecutorService scheduler) {
        this.clientSocket = socket;
        this.rates = rates;
        this.config = config;
        this.scheduler = scheduler;
    }

    /**
     * İstemci iletişimini başlatan ve istek döngüsünü yürüten ana metod.
     * <p>
     * Bu metod, istemciden gelen komutları okur, işler ve gerektiğinde
     * kur verilerini düzenli olarak gönderir. İstemci bağlantısı
     * koptuğunda kaynakları temizler.
     * </p>
     */
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

    /**
     * İstemciden gelen istekleri işler.
     * <p>
     * İstek formatı: "command|parameter" şeklindedir.
     * Desteklenen komutlar:
     * <ul>
     *   <li>subscribe|SYMBOL - Belirtilen sembole abone olma</li>
     *   <li>unsubscribe|SYMBOL - Belirtilen sembolden aboneliği iptal etme</li>
     *   <li>unsubscribe|all - Tüm abonelikleri iptal etme</li>
     * </ul>
     * </p>
     *
     * @param request İstemciden gelen istek
     * @param out İstemciye yanıt göndermek için kullanılan writer
     */
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

    /**
     * Belirtilen kura abone olur ve düzenli güncelleme göndermeye başlar.
     * <p>
     * Bu metod, istemcinin istediği sembole abone olmasını sağlar ve
     * yapılandırma ile belirtilen aralıklarla güncel kur verilerini
     * istemciye göndermeye başlar.
     * </p>
     *
     * @param rateName Abone olunacak kur sembolü
     * @param out İstemciye veri göndermek için kullanılan writer
     */
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
                // Önce kuru güncelle
                rate.update();
                Double[] currentVals = rate.getCurrentValues();
                String timestamp = ISO_TIMESTAMP_FORMATTER.format(Instant.now());

                // Kur verisini formatlayarak gönder
                String message = String.format(Locale.US,
                        "%s|bid:%.5f|ask:%.5f|timestamp:%s",
                        rateName, currentVals[0], currentVals[1], timestamp
                );

                // PrintWriter hata kontrolü
                if (out.checkError()) {
                    log.error("PrintWriter error for client {}, stopping broadcasts.", clientSocket.getInetAddress());
                    throw new IOException("PrintWriter error detected.");
                }
                out.println(message);

            } catch (Exception e) {
                log.error("Error during broadcast for rate {} to client {}: {}", rateName, clientSocket.getInetAddress(), e.getMessage(), e);
                stopBroadcast(rateName);
                throw new RuntimeException(e);
            }
        }, 0, config.getBroadcastIntervalMs(), TimeUnit.MILLISECONDS);

        subscriptions.put(rateName, task);
        log.info("Client {} subscribed to {}", clientSocket.getInetAddress(), rateName);
        out.println("Subscribed to " + rateName);
    }

    /**
     * Belirtilen kurdan aboneliği iptal eder.
     * <p>
     * Bu metod, istemcinin bir sembole olan aboneliğini iptal eder
     * ve o sembolle ilgili veri gönderimini durdurur. Özel olarak "all"
     * parametresi ile tüm abonelikler iptal edilebilir.
     * </p>
     *
     * @param rateName Aboneliği iptal edilecek kur sembolü veya "all"
     * @param out İstemciye yanıt göndermek için kullanılan writer
     */
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

    /**
     * Belirli bir kur için yapılan yayını durdurur.
     *
     * @param rateName Yayını durdurulacak kur sembolü
     * @return Yayın durduruldu ise true, sembol bulunamadı ise false
     */
    private boolean stopBroadcast(String rateName) {
        ScheduledFuture<?> task = subscriptions.remove(rateName);
        if (task != null) {
            task.cancel(false);
            log.debug("Stopped broadcast task for rate {} for client {}", rateName, clientSocket.getInetAddress());
            return true;
        }
        return false;
    }

    /**
     * Bu istemci için tüm kur yayınlarını durdurur.
     */
    private void stopAllBroadcasts() {
        if (!subscriptions.isEmpty()) {
            log.info("Stopping all ({}) broadcasts for client {}", subscriptions.size(), clientSocket.getInetAddress());
            subscriptions.values().forEach(task -> task.cancel(false));
            subscriptions.clear();
        }
    }

    /**
     * İstemci bağlantısı ve ilişkili kaynakları temizler.
     * <p>
     * Bu metod, istemci bağlantısı koptuğunda veya istemci ayrıldığında
     * çağrılır. Tüm yayınları durdurur ve soketi kapatır.
     * </p>
     */
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