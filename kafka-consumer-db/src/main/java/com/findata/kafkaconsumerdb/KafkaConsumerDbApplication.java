package com.findata.kafkaconsumerdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka Consumer DB uygulamasının ana sınıfı.
 * <p>
 * Bu uygulama, Kafka'dan gelen finansal kur verilerini dinler ve
 * veritabanına kaydeder. Spring Boot ile geliştirilmiş bir servis olarak
 * çalışır ve Kafka consumer, JPA repository gibi bileşenleri kullanır.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@SpringBootApplication
public class KafkaConsumerDbApplication {

    /**
     * Uygulamanın başlangıç noktası.
     *
     * @param args Komut satırı argümanları
     */
    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerDbApplication.class, args);
    }

}
