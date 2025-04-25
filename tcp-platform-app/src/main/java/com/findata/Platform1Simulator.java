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

/**
 * TCP platformunun ana sınıfı ve uygulamanın giriş noktası.
 * <p>
 * Bu sınıf, simüle edilmiş finans verisi sağlayan bir TCP sunucusunu başlatır.
 * JSON yapılandırma dosyasından ayarları okur, paylaşılan kaynakları (kurlar, zamanlayıcı)
 * oluşturur ve TcpServer ile tüm bağlantı işlemlerini yönetir.
 * </p>
 * <p>
 * Uygulama, kabul edilebilir şekilde kapatılmasını sağlamak için bir shutdown hook
 * içerir ve tüm kaynakları düzgün şekilde temizler.
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public class Platform1Simulator {

    /** Loglama için kullanılan Logger nesnesi */
    private static final Logger log = LoggerFactory.getLogger(Platform1Simulator.class);

    /** Tüm kurların saklandığı thread-safe harita */
    private static final Map<String, Rate> rates = new ConcurrentHashMap<>();
    
    /** Zamanlı görevleri yürütmek için kullanılan thread havuzu */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    /**
     * Uygulamanın giriş noktası.
     * <p>
     * Bu metod şu işlemleri gerçekleştirir:
     * <ol>
     *   <li>Yapılandırma dosyasını yükler</li>
     *   <li>TcpServer nesnesini oluşturur</li>
     *   <li>Düzgün kapatma (graceful shutdown) için hook ekler</li>
     *   <li>TCP sunucusunu başlatır</li>
     * </ol>
     * </p>
     *
     * @param args Komut satırı argümanları (kullanılmamaktadır)
     */
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook initiated...");
            // Sunucuya durma sinyali gönder
            server.stop();

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

        // 4. Sunucuyu Başlat
        try {
            server.start();
        } catch (Exception e) {
            log.error("An unexpected error occurred during server execution: {}", e.getMessage(), e);
        } finally {
            // Sunucu durduğunda scheduler'ı kapatmayı garanti edelim
            if (!scheduler.isShutdown()) {
                log.warn("Scheduler was not shut down by hook, shutting down now.");
                scheduler.shutdownNow();
            }
            log.info("Platform1Simulator main method finished.");
        }
    }
}