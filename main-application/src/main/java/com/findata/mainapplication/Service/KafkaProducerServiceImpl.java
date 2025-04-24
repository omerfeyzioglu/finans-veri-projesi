package com.findata.mainapplication.Service;

import com.findata.mainapplication.Abstract.KafkaProducerService;
import com.findata.mainapplication.model.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult; // SendResult importu
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.CompletableFuture; // CompletableFuture importu

@Service // Bu sınıfın bir Spring Bean olduğunu belirtir
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);

    @Value("${kafka.topic.raw-rates:raw-rates}")
    private String rawRatesTopic;

    @Value("${kafka.topic.calculated-rates:calculated-rates}")
    private String calculatedRatesTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendRawRate(Rate rate) {
        if (rate == null || rate.getPlatform() == null || rate.getSymbol() == null || rate.getTimestamp() == null) {
            log.warn("Cannot send invalid raw rate to Kafka: {}", rate);
            return;
        }
        // Kafka anahtarı: Platform_Sembol (örn: PF1_USDTRY)
        String key = rate.getPlatform() + "_" + rate.getSymbol();
        String message = formatRateForKafka(key, rate);
        sendMessage(rawRatesTopic, key, message);
    }

    @Override
    public void sendCalculatedRate(Rate rate) {
        if (rate == null || rate.getSymbol() == null || rate.getTimestamp() == null) {
            log.warn("Cannot send invalid calculated rate to Kafka: {}", rate);
            return;
        }
        // Kafka anahtarı: Sembol (örn: USDTRY)
        String key = rate.getSymbol();
        String message = formatRateForKafka(key, rate);
        sendMessage(calculatedRatesTopic, key, message);
    }

    // Dokümandaki formata uygun String oluşturma: SYMBOL|BID|ASK|TIMESTAMP
    private String formatRateForKafka(String keySymbol, Rate rate) {
        // Locale.US kullanarak ondalık ayırıcının nokta olmasını garantileyelim
        return String.format(Locale.US, "%s|%.5f|%.5f|%s",
                keySymbol,
                rate.getBid(),
                rate.getAsk(),
                rate.getTimestamp().toString()); // Instant.toString() ISO-8601 formatındadır
    }

    // Kafka'ya mesaj gönderen asıl metot (asenkron gönderim ve sonuç loglama)
    private void sendMessage(String topic, String key, String message) {
        try {
            log.trace("Sending message to Kafka -> Topic: {}, Key: {}, Message: {}", topic, key, message);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);

            // Gönderim sonucunu asenkron olarak işle (opsiyonel ama iyi pratik)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.trace("Message sent successfully to Kafka topic {} with offset {}", topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message to Kafka topic {}: {}", topic, ex.getMessage());
                    // TODO: Kalıcı hata yönetimi (örn: başka bir yere loglama, retry mekanizması?)
                }
            });
        } catch (Exception e) {
            // kafkaTemplate.send anında da hata fırlatabilir (örn: Producer kapalıysa)
            log.error("Exception occurred while sending message to Kafka topic {}: {}", topic, e.getMessage(), e);
        }
    }
}