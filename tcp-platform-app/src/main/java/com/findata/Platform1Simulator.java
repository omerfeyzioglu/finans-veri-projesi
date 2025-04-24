package com.findata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
// import java.net.InetAddress; // Artık burada kullanılmıyor
// import java.net.ServerSocket; // Artık burada kullanılmıyor
// import java.net.Socket; // Artık burada kullanılmıyor
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Platform1Simulator {

    private static final Logger log = LoggerFactory.getLogger(Platform1Simulator.class);

    // Paylaşılan kaynaklar (static kalabilirler veya TcpServer'a taşınabilirler)
    private static final Map<String, Rate> rates = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10); // Thread sayısını ayarla

    public static void main(String[] args) {
        // 1. Konfigürasyonu Yükle
        Config config = null;
        try {
            config = ConfigLoader.loadConfig(rates);
            log.info("Configuration loaded successfully. Port: {}, Interval: {}ms", config.getPort(), config.getBroadcastIntervalMs());
            log.info("Initial rates loaded: {}", rates.keySet());
        } catch (IOException e) {
            log.error("Failed to load configuration: {}", e.getMessage(), e);
            System.exit(1); // Config olmadan başlatılamaz
        }

        // 2. TcpServer Nesnesini Oluştur
        TcpServer server = new TcpServer(config, rates, scheduler);

        // 3. Graceful Shutdown Hook'u Ayarla
        // (Shutdown hook static scheduler'a erişebildiği için burada kalabilir)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook initiated...");
            // Sunucuya durma sinyali gönder (isteğe bağlı, stop() metodunu implemente edersek)
            // server.stop();

            // Scheduler'ı düzgünce kapat
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Scheduler did not terminate in time, forcing shutdown...");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ex) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("Shutdown hook finished.");
        }));

        // 4. Sunucuyu Başlat (Bu metot genellikle bloklar ve program burada bekler)
        try {
            server.start();
        } catch (Exception e) {
            // TcpServer.start() içindeki hatalar orada loglanmalı,
            // ama beklenmedik bir hata olursa burada yakalayabiliriz.
            log.error("An unexpected error occurred during server execution: {}", e.getMessage(), e);
        } finally {
            // Sunucu durduğunda (normal veya hata ile) scheduler'ı kapatmayı garanti edelim
            // (Shutdown hook çalışmazsa diye ek güvenlik)
            if (!scheduler.isShutdown()) {
                log.warn("Scheduler was not shut down by hook, shutting down now.");
                scheduler.shutdownNow();
            }
            log.info("Platform1Simulator main method finished.");
        }
    }
}