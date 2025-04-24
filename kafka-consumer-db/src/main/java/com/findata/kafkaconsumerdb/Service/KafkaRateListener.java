package com.findata.kafkaconsumerdb.Service; // Paket adını kontrol et

import com.findata.kafkaconsumerdb.Entity.RateRecord;
import com.findata.kafkaconsumerdb.Repository.RateRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload; // Mesaj içeriğini almak için
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // DB işlemi için

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
public class KafkaRateListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaRateListener.class);

    private final RateRecordRepository rateRecordRepository;

    @Autowired
    public KafkaRateListener(RateRecordRepository rateRecordRepository) {
        this.rateRecordRepository = rateRecordRepository;
    }

    // Birden fazla topic'i dinlemek için topics özelliğini dizi olarak kullanıyoruz
    // Topic isimlerini ve group id'yi application.properties'den alıyoruz
    @KafkaListener(topics = {"${kafka.topic.raw-rates}", "${kafka.topic.calculated-rates}"},
            groupId = "${spring.kafka.consumer.group-id}")
    @Transactional // Veritabanı işlemini transaction içinde yapalım
    public void listenRates(@Payload String message) { // @Payload ile mesajın içeriğini alıyoruz
        log.debug("Received message: {}", message);

        if (message == null || message.isBlank()) {
            log.warn("Received empty or null message. Skipping.");
            return;
        }

        // Mesajı parse et: KEY|BID|ASK|TIMESTAMP
        // Örnek: PF1_USDTRY|33.99242|34.99736|2025-04-02T02:41:39.124842655Z
        // Veya: EURTRY|36.17101|36.69978|2025-04-02T02:41:45.058957208Z
        try {
            String[] parts = message.split("\\|"); // '|' karakterine göre ayır

            if (parts.length != 4) {
                log.warn("Invalid message format received (Expected 4 parts separated by '|'): {}", message);
                return; // Hatalı formatı atla
            }

            String rateName = parts[0].trim();
            String bidStr = parts[1].trim();
            String askStr = parts[2].trim();
            String timestampStr = parts[3].trim();

            // Değerleri doğru tiplere çevir
            BigDecimal bid = new BigDecimal(bidStr); // String to BigDecimal
            BigDecimal ask = new BigDecimal(askStr); // String to BigDecimal
            Instant timestamp = Instant.parse(timestampStr); // ISO-8601 String to Instant

            // Yeni bir RateRecord entity'si oluştur
            RateRecord rateRecord = new RateRecord();
            rateRecord.setRateName(rateName);
            rateRecord.setBid(bid);
            rateRecord.setAsk(ask);
            rateRecord.setRateUpdatetime(timestamp);
            // dbUpdatetime @PrePersist/@PreUpdate ile otomatik dolacak

            // Repository kullanarak veritabanına kaydet
            RateRecord savedRecord = rateRecordRepository.save(rateRecord);
            log.info("Rate record saved successfully: {}", savedRecord);

        } catch (NumberFormatException e) {
            log.error("Failed to parse bid/ask from message '{}': {}", message, e.getMessage());
            // TODO: Hatalı mesajları bir Dead Letter Topic'e gönderme mekanizması eklenebilir
        } catch (DateTimeParseException e) {
            log.error("Failed to parse timestamp from message '{}': {}", message, e.getMessage());
            // TODO: Hatalı mesajları bir Dead Letter Topic'e gönderme mekanizması eklenebilir
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Invalid message structure (less than 4 parts) in message '{}': {}", message, e.getMessage());
            // TODO: Hatalı mesajları bir Dead Letter Topic'e gönderme mekanizması eklenebilir
        } catch (Exception e) { // Diğer beklenmedik hatalar (örn: DB hatası)
            log.error("Failed to process and save message '{}': {}", message, e.getMessage(), e);
            // TODO: Hatalı mesajları bir Dead Letter Topic'e gönderme mekanizması eklenebilir
        }
    }
}