package com.findata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class TcpServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private final Config config;
    private final Map<String, Rate> rates;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running = true; // Sunucunun çalışma durumunu kontrol etmek için

    public TcpServer(Config config, Map<String, Rate> rates, ScheduledExecutorService scheduler) {
        this.config = config;
        this.rates = rates;
        this.scheduler = scheduler;
    }

    // Sunucuyu başlatan ana metot
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            InetAddress ip = InetAddress.getLocalHost();
            log.info("TCP Server starting on {}:{}", ip.getHostAddress(), config.getPort());

            while (running) { // 'running' bayrağı kontrol edilerek döngüden çıkılabilir
                try {
                    Socket clientSocket = serverSocket.accept(); // Bloklayan çağrı, yeni bağlantı bekler
                    // Her istemci için yeni bir ClientHandler başlat
                    // Paylaşılan 'rates' ve 'scheduler'ı ClientHandler'a ver
                    log.debug("New client connection accepted from {}", clientSocket.getInetAddress());
                    new Thread(new ClientHandler(clientSocket, rates, config, scheduler)).start();
                } catch (IOException e) {
                    if (!running) {
                        log.info("Server socket closed, shutting down accept loop.");
                        break; // 'stop' çağrıldığında döngüden çık
                    }
                    log.error("Error accepting client connection: {}", e.getMessage(), e);
                    // Sunucu soketi hatası durumunda döngüyü kırabilir veya devam edebiliriz.
                    // Şimdilik devam edelim, ciddi bir sorun yoksa.
                    if (serverSocket.isClosed()) {
                        log.error("Server socket closed unexpectedly. Stopping server.");
                        running = false; // Döngüden çıkmayı sağla
                    }
                }
            }
        } catch (IOException e) {
            log.error("Could not start server on port {}: {}", config.getPort(), e.getMessage(), e);
            // Bu durumda uygulama muhtemelen başlatılamaz.
            // Main metotta System.exit çağrıldığı için burada tekrar çağırmaya gerek yok.
        } finally {
            log.info("TCP Server stopped.");
        }
    }

    // Sunucuyu durdurmak için (örneğin shutdown hook'tan çağrılabilir)
    public void stop() {
        running = false;
        // ServerSocket'u kapatmaya zorlamak gerekebilir (accept'te bekliyorsa)
        // Ancak try-with-resources veya shutdown hook bunu yönetmeli.
        // Genellikle sadece 'running' flag'ini ayarlamak yeterli olabilir
        // veya ServerSocket'u dışarıdan kapatmak gerekebilir.
        // Şimdilik basit tutalım.
        log.info("Stop signal received, server will shut down.");
    }
}