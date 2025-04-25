package com.findata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * TCP Sunucusu ana sınıfı.
 * <p>
 * Bu sınıf, belirtilen port üzerinde bir TCP sunucusu çalıştırır ve gelen
 * bağlantı isteklerini kabul eder. Her bağlantı için ayrı bir {@link ClientHandler}
 * thread'i oluşturur.
 * </p>
 * <p>
 * Sunucu, yapılandırma ile belirtilen portu dinler ve bağlanan tüm istemcilere
 * kur verilerini gönderir. İstemciler, kur verilerine subscribe/unsubscribe
 * yapabilirler.
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public class TcpServer {

    /** Loglama için kullanılan Logger nesnesi */
    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    /** Sunucu yapılandırma bilgisi */
    private final Config config;
    
    /** Tüm kurların paylaşılan haritası */
    private final Map<String, Rate> rates;
    
    /** Zamanlı görevleri yürütmek için kullanılan thread havuzu */
    private final ScheduledExecutorService scheduler;
    
    /** Sunucunun çalışma durumunu kontrol eden bayrak */
    private volatile boolean running = true;

    /**
     * Yapılandırma ve paylaşılan kaynaklarla yeni bir TCP sunucusu oluşturur.
     *
     * @param config Sunucu yapılandırması
     * @param rates Tüm kurların saklandığı harita
     * @param scheduler Zamanlı görevleri yürütmek için kullanılan thread havuzu
     */
    public TcpServer(Config config, Map<String, Rate> rates, ScheduledExecutorService scheduler) {
        this.config = config;
        this.rates = rates;
        this.scheduler = scheduler;
    }

    /**
     * TCP sunucusunu başlatır ve gelen bağlantıları kabul etmeye başlar.
     * <p>
     * Bu metod, sunucu durdurulana kadar bloklayıcıdır. Her gelen bağlantı
     * için ayrı bir {@link ClientHandler} thread'i oluşturur.
     * </p>
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            InetAddress ip = InetAddress.getLocalHost();
            log.info("TCP Server starting on {}:{}", ip.getHostAddress(), config.getPort());

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    log.debug("New client connection accepted from {}", clientSocket.getInetAddress());
                    new Thread(new ClientHandler(clientSocket, rates, config, scheduler)).start();
                } catch (IOException e) {
                    if (!running) {
                        log.info("Server socket closed, shutting down accept loop.");
                        break;
                    }
                    log.error("Error accepting client connection: {}", e.getMessage(), e);
                    if (serverSocket.isClosed()) {
                        log.error("Server socket closed unexpectedly. Stopping server.");
                        running = false;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Could not start server on port {}: {}", config.getPort(), e.getMessage(), e);
        } finally {
            log.info("TCP Server stopped.");
        }
    }

    /**
     * TCP sunucusunu durdurur.
     * <p>
     * Bu metod, sunucunun çalışma durumunu değiştirerek, {@link #start()} metodundaki
     * bağlantı kabul döngüsünün sonlanmasını sağlar. Genellikle shutdown hook
     * tarafından çağrılır.
     * </p>
     */
    public void stop() {
        running = false;
        log.info("Stop signal received, server will shut down.");
    }
}