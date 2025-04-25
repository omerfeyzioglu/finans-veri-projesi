package com.findata.mainapplication.Abstract;
import com.findata.mainapplication.model.Rate;

/**
 * Formatlanmış kur verilerini Kafka'ya gönderen servis arayüzü.
 * <p>
 * Bu arayüz, hesaplanan ve ham kur verilerini Kafka mesaj sistemine göndermek için
 * gerekli metodları tanımlar. Kafka üzerinde iki farklı topic kullanılır:
 * raw-rates (ham veriler için) ve calculated-rates (hesaplanmış veriler için).
 * </p>
 * <p>
 * Gönderilen veriler, downstream sistemler tarafından (örn: veritabanı yazıcılar, 
 * analitik işlemciler) tüketilir. Kafka'nın sağladığı güvenilir mesaj iletimi ve 
 * dağıtım özellikleri, veri akışının güvenli bir şekilde işlenmesini sağlar.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public interface KafkaProducerService {

    /**
     * Geçerli bulunan ham kur verisini ilgili Kafka topic'ine gönderir.
     * <p>
     * Bu metod, platformlardan gelen ham kur verilerini 'raw-rates' Kafka topic'ine
     * gönderir. Kafka mesajının anahtarı, "{platform}_{symbol}" formatında olur.
     * </p>
     * 
     * @param rate Gönderilecek ham Rate nesnesi.
     */
    void sendRawRate(Rate rate);

    /**
     * Hesaplanan kur verisini ilgili Kafka topic'ine gönderir.
     * <p>
     * Bu metod, hesaplama servisi tarafından işlenmiş kur verilerini 'calculated-rates'
     * Kafka topic'ine gönderir. Kafka mesajının anahtarı, sembol değeridir.
     * </p>
     * 
     * @param rate Gönderilecek hesaplanmış Rate nesnesi.
     */
    void sendCalculatedRate(Rate rate);
}