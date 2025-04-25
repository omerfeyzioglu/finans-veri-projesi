package com.findata.springplatform2;


import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kur verilerinin yönetimini sağlayan servis arayüzü.
 * <p>
 * Bu arayüz, Platform 2 (PF2) uygulamasının sağladığı kur verilerine
 * erişim için gerekli metotları tanımlar. Kur verilerinin
 * depolanması, güncellenmesi ve sorgulanması işlemlerini
 * soyutlar.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public interface RateService {

    /**
     * Belirtilen tam kur ismi için güncel kur verisini getirir.
     * <p>
     * Bu metod, platform önekini içeren tam kur ismiyle (örn: "PF2_USDTRY")
     * eşleşen en güncel kur verisini döndürür. Eğer belirtilen isimde bir
     * kur verisi yoksa, null değeri döner.
     * </p>
     *
     * @param fullRateName Platform önekli kur ismi (örn: "PF2_USDTRY")
     * @return Güncel kur verisi nesnesi, veya bulunamazsa null
     */
    Rate getRate(String fullRateName);

    // İleride başka servis metodları eklenirse buraya tanımlanabilir.
    // Örneğin, desteklenen tüm kurları listeleme gibi.
    // Map<String, Rate> getAllRates();
}