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

/**
 * Kafka'ya kur verilerini gönderen KafkaProducerService implementasyonu.
 * <p>
 * Bu sınıf, ham ve hesaplanmış kur verilerini ilgili Kafka topic'lerine gönderir.
 * Spring Kafka kütüphanesini kullanarak asenkron mesaj gönderimi ve sonuç işleme
 * özelliklerini sağlar.
 * </p>
 * <p>
 * Gönderilen mesajlar için tutarlı bir format kullanılır: "SYMBOL|BID|ASK|TIMESTAMP".
 * Bu, downstream sistemlerin verileri kolayca işlemesini sağlar.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@Service // Bu sınıfın bir Spring Bean olduğunu belirtir
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);

    /**
     * Ham kur verilerinin gönderileceği Kafka topic adı.
     * application.properties'den alınır, varsayılan değeri "raw-rates"
     */
    @Value("${kafka.topic.raw-rates:raw-rates}")
    private String rawRatesTopic;

    /**
     * Hesaplanmış kur verilerinin gönderileceği Kafka topic adı.
     * application.properties'den alınır, varsayılan değeri "calculated-rates"
     */
    @Value("${kafka.topic.calculated-rates:calculated-rates}")
    private String calculatedRatesTopic;

    /**
     * Kafka'ya mesaj göndermek için kullanılan template.
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Bağımlılıkların Spring tarafından enjekte edildiği constructor.
     *
     * @param kafkaTemplate Kafka mesaj gönderimi için kullanılacak template
     */
    @Autowired
    public KafkaProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * Dokümandaki formata uygun String oluşturma: SYMBOL|BID|ASK|TIMESTAMP
     *
     * @param keySymbol Mesaj anahtarı/sembolü
     * @param rate Formata dönüştürülecek kur nesnesi
     * @return Format: "SYMBOL|BID|ASK|TIMESTAMP"
     */
    private String formatRateForKafka(String keySymbol, Rate rate) {
        // Locale.US kullanarak ondalık ayırıcının nokta olmasını garantileyelim
        return String.format(Locale.US, "%s|%.5f|%.5f|%s",
                keySymbol,
                rate.getBid(),
                rate.getAsk(),
                rate.getTimestamp().toString()); // Instant.toString() ISO-8601 formatındadır
    }

    /**
     * Kafka'ya mesaj gönderen asıl metod.
     * <p>
     * Asenkron gönderim yapar ve sonucu log'lar. Hata durumlarını yakalar ve log'lar.
     * </p>
     *
     * @param topic Mesajın gönderileceği Kafka topic'i
     * @param key Mesaj anahtarı
     * @param message Gönderilecek mesaj içeriği
     */
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