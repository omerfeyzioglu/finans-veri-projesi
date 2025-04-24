package com.findata.mainapplication.Abstract;
import com.findata.mainapplication.model.Rate;

/**
 * Formatlanmış kur verilerini Kafka'ya gönderen servis arayüzü.
 */
public interface KafkaProducerService {

    /**
     * Geçerli bulunan ham kur verisini ilgili Kafka topic'ine gönderir.
     * @param rate Gönderilecek ham Rate nesnesi.
     */
    void sendRawRate(Rate rate);

    /**
     * Hesaplanan kur verisini ilgili Kafka topic'ine gönderir.
     * @param rate Gönderilecek hesaplanmış Rate nesnesi.
     */
    void sendCalculatedRate(Rate rate);
}