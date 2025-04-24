package com.findata.mainapplication.Concrete;


import com.findata.mainapplication.Abstract.CoordinatorCallback;
import com.findata.mainapplication.Abstract.PlatformConnector;
import com.findata.mainapplication.model.Rate;
import jakarta.annotation.PreDestroy; // Spring Boot 3+
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component // Spring Bean olarak işaretle
public class TcpPlatformConnector implements PlatformConnector {

    private static final Logger log = LoggerFactory.getLogger(TcpPlatformConnector.class);

    @Value("${platform.tcp.name:PF1}")
    private String platformName;

    @Value("${platform.tcp.host:localhost}") // application.properties'den oku
    private String host;

    @Value("${platform.tcp.port:8081}")  // application.properties'den oku
    private int port;

    private CoordinatorCallback callback;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    // Gelen veriyi dinlemek için ayrı bir thread havuzu (tek thread yeterli)
    private ExecutorService listenerExecutor;
    // Hangi sembollere abone olduğumuzu takip etmek için (opsiyonel ama iyi pratik)
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();

    @Override
    public void setCallback(CoordinatorCallback callback) {
        this.callback = callback;
    }

    @Override
    public String getPlatformName() {
        return platformName;
    }

    @Override
    public synchronized void connect() {
        if (connected.get()) {
            log.warn("[{}] Already connected.", platformName);
            return;
        }
        try {
            log.info("[{}] Connecting to {}:{}", platformName, host, port);
            socket = new Socket(host, port);
            socket.setKeepAlive(true); // Bağlantının kopup kopmadığını anlamak için
            out = new PrintWriter(socket.getOutputStream(), true); // autoFlush=true
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected.set(true);
            log.info("[{}] Connection established.", platformName);

            // Bağlantı başarılıysa callback'i çağır
            if (callback != null) {
                callback.onConnect(platformName);
            }

            // Gelen verileri dinlemek için ayrı bir thread başlat
            startListenerThread();

            // Bağlandıktan sonra bekleyen abonelikleri gönderelim
            resubscribeAll();

        } catch (IOException e) {
            log.error("[{}] Connection failed: {}", platformName, e.getMessage());
            connected.set(false);
            if (callback != null) {
                callback.onError(platformName, "Connection failed: " + e.getMessage());
            }
            // Hata sonrası temizlik yap
            closeResources();
        }
    }

    private void startListenerThread() {
        // Eğer zaten çalışıyorsa veya kapatılmışsa yeniden başlatma
        if (listenerExecutor != null && !listenerExecutor.isShutdown()) {
            listenerExecutor.shutdownNow(); // Öncekini durdur
        }
        listenerExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, platformName + "-Listener"));
        listenerExecutor.submit(this::listenForMessages);
        log.info("[{}] Listener thread started.", platformName);
    }

    private void listenForMessages() {
        try {
            String line;
            while (connected.get() && (line = in.readLine()) != null) {
                log.trace("[{}] Received line: {}", platformName, line);
                parseAndHandleMessage(line);
            }
        } catch (SocketException e) {
            if (connected.get()) { // Eğer biz kapatmadıysak bu bir hatadır
                log.error("[{}] SocketException during listening (connection likely lost): {}", platformName, e.getMessage());
                if (callback != null) callback.onError(platformName, "Connection lost: " + e.getMessage());
            } else {
                log.info("[{}] Socket closed normally during listening.", platformName);
            }
        } catch (IOException e) {
            if (connected.get()) {
                log.error("[{}] IOException during listening: {}", platformName, e.getMessage(), e);
                if (callback != null) callback.onError(platformName, "Read error: " + e.getMessage());
            }
        } catch (Exception e) { // Beklenmedik hatalar
            log.error("[{}] Unexpected error during listening: {}", platformName, e.getMessage(), e);
            if (callback != null) callback.onError(platformName, "Unexpected listener error: " + e.getMessage());
        } finally {
            log.info("[{}] Listener thread finished.", platformName);
            // Bağlantı koptuysa durumu güncelle ve bildir
            if (connected.compareAndSet(true, false)) { // Eğer hala bağlı görünüyorsa kopmuş demektir
                if (callback != null) {
                    callback.onDisconnect(platformName);
                }
            }
            // Kaynakları temizle (disconnect çağrılmadıysa)
            closeResources();
        }
    }

    private void parseAndHandleMessage(String line) {
        try {
            // Örnek Format: PF1_USDTRY|bid:33.98895|ask:35.00697|timestamp:2025-04-01T22:29:23.839844300Z
            // Diğer mesajlar: "Subscribed to ...", "Unsubscribed from ...", "ERROR|..."
            if (line.startsWith("Subscribed to") || line.startsWith("Unsubscribed from")) {
                log.info("[{}] Received confirmation: {}", platformName, line);
                return; // Bilgi mesajı, rate update değil
            }
            if (line.startsWith("ERROR|")) {
                log.error("[{}] Received error from platform: {}", platformName, line);
                if (callback != null) callback.onError(platformName, line);
                return;
            }

            String[] parts = line.split("\\|");
            if (parts.length >= 4) { // Sembol, bid, ask, timestamp bekleniyor
                String platformSymbol = parts[0]; //örn: PF1_USDTRY
                String bidPart = parts[1]; //örn: bid:33.98895
                String askPart = parts[2]; //örn: ask:35.00697
                String timestampPart = parts[3]; //örn: timestamp:2025-04-01T...Z

                if (platformSymbol.startsWith(platformName + "_") &&
                        bidPart.startsWith("bid:") &&
                        askPart.startsWith("ask:") &&
                        timestampPart.startsWith("timestamp:"))
                {
                    String symbol = platformSymbol.substring(platformName.length() + 1);
                    double bid = Double.parseDouble(bidPart.substring(4).replace(',', '.'));
                    double ask = Double.parseDouble(askPart.substring(4).replace(',', '.'));
                    Instant timestamp = Instant.parse(timestampPart.substring(10)); // ISO formatını parse et

                    Rate rate = new Rate(platformName, symbol, bid, ask, timestamp);

                    if (callback != null) {
                        callback.onRateUpdate(rate);
                    }
                } else {
                    log.warn("[{}] Received non-rate message or malformed rate line: {}", platformName, line);
                }

            } else {
                log.warn("[{}] Received unexpected line format: {}", platformName, line);
            }
        } catch (DateTimeParseException e) {
            log.error("[{}] Failed to parse timestamp in line '{}': {}", platformName, line, e.getMessage());
        } catch (NumberFormatException e) {
            log.error("[{}] Failed to parse bid/ask in line '{}': {}", platformName, line, e.getMessage());
        } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
            log.error("[{}] Failed to parse structure of line '{}': {}", platformName, line, e.getMessage());
        } catch (Exception e) { // Diğer beklenmedik hatalar
            log.error("[{}] Unexpected error parsing line '{}': {}", platformName, line, e.getMessage(), e);
        }
    }


    @Override
    public synchronized void disconnect() {
        if (!connected.get()) {
            log.warn("[{}] Already disconnected.", platformName);
            return;
        }
        log.info("[{}] Disconnecting...", platformName);
        connected.set(false); // Önce flag'i set et ki listener loop durabilsin

        // Listener thread'i durdur
        if (listenerExecutor != null) {
            listenerExecutor.shutdown(); // Mevcut işi bitirmesini bekle
            try {
                if (!listenerExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    listenerExecutor.shutdownNow(); // Beklemezse zorla kapat
                }
            } catch (InterruptedException e) {
                listenerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("[{}] Listener thread stopped.", platformName);
        }

        // Soket ve stream'leri kapat
        closeResources();

        log.info("[{}] Disconnected successfully.", platformName);
        // Callback en son çağrılabilir veya listener thread'in finally bloğunda çağrılır
        if (callback != null) {
            callback.onDisconnect(platformName);
        }
    }

    private void closeResources() {
        try { if (out != null) out.close(); } catch (Exception e) { log.trace("[{}] Error closing print writer: {}", platformName, e.getMessage()); }
        try { if (in != null) in.close(); } catch (Exception e) { log.trace("[{}] Error closing buffered reader: {}", platformName, e.getMessage()); }
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) { log.trace("[{}] Error closing socket: {}", platformName, e.getMessage()); }
        out = null;
        in = null;
        socket = null;
        log.debug("[{}] TCP resources closed.", platformName);
    }

    @Override
    public void subscribe(String symbol) {
        if (!connected.get() || out == null) {
            log.warn("[{}] Cannot subscribe, not connected.", platformName);
            // Belki burada bağlanmayı deneyebilir mi? Veya hata fırlatabilir mi? Şimdilik sadece loglayalım.
            return;
        }
        String platformSymbol = platformName + "_" + symbol;
        log.info("[{}] Sending subscribe request for: {}", platformName, platformSymbol);
        subscribedSymbols.add(symbol); // Abone olunanları takip et
        out.println("subscribe|" + platformSymbol);
    }

    @Override
    public void unsubscribe(String symbol) {
        if (!connected.get() || out == null) {
            log.warn("[{}] Cannot unsubscribe, not connected.", platformName);
            return;
        }
        String platformSymbol = platformName + "_" + symbol;
        log.info("[{}] Sending unsubscribe request for: {}", platformName, platformSymbol);
        subscribedSymbols.remove(symbol); // Takip listesinden çıkar
        out.println("unsubscribe|" + platformSymbol);
        // Gelen "Unsubscribed from..." mesajını listener thread yakalayacak ve loglayacak.
    }

    private void resubscribeAll() {
        if (!connected.get() || subscribedSymbols.isEmpty()) {
            return;
        }
        log.info("[{}] Resubscribing to symbols after connection: {}", platformName, subscribedSymbols);
        // Kopya bir liste üzerinden iterasyon yapalım ki eş zamanlılık sorunu olmasın
        Set.copyOf(subscribedSymbols).forEach(this::subscribe);
    }


    @Override
    public boolean isConnected() {
        // Sadece flag'e bakmak yerine socket durumunu da kontrol edebiliriz
        return connected.get() && socket != null && socket.isConnected() && !socket.isClosed();
    }

    @PreDestroy // Spring context kapanırken çağrılır
    public void shutdown() {
        log.info("[{}] PreDestroy cleanup called.", platformName);
        disconnect();
    }
}
