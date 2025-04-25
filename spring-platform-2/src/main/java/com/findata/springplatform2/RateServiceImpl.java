package com.findata.springplatform2;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateService arayüzünün implementasyonu.
 * <p>
 * Bu sınıf, kur verilerinin yönetimini, düzenli güncellemeleri ve
 * sorgulanmasını sağlar. Belirli aralıklarla kur verilerini
 * rastgele dalgalanmalarla günceller ve güncel verilere erişim
 * sağlar.
 * </p>
 * <p>
 * Kur verileri thread-safe bir ConcurrentHashMap'te tutulur ve
 * zamanlanmış görevlerle düzenli olarak güncellenir.
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@Service
public class RateServiceImpl implements RateService {

    /** Loglama için kullanılan Logger nesnesi */
    private static final Logger log = LoggerFactory.getLogger(RateServiceImpl.class);

    /** Tüm kurların thread-safe haritası */
    private final Map<String, Rate> rates = new ConcurrentHashMap<>();
    
    /** Rastgele değerler üretmek için kullanılan Random nesnesi */
    private final Random random = new Random();

    /** Platformun benzersiz tanımlayıcısı */
    @Value("${platform.id:PF2}")
    private String platformId;

    /** Kur güncelleme sıklığı (milisaniye) */
    @Value("${rate.update.frequency:5000}")
    private long updateFrequency;

    /** Kur dalgalanma yüzdesi */
    @Value("${rate.fluctuation.percentage:0.5}")
    private double fluctuationPercentage;

    /** Başlangıç kur değerleri */
    @Value("${rate.initial.usdtry.bid:34.80}") private double initialUsdTryBid;
    @Value("${rate.initial.usdtry.ask:35.10}") private double initialUsdTryAsk;
    @Value("${rate.initial.eurusd.bid:1.0370}") private double initialEurUsdBid;
    @Value("${rate.initial.eurusd.ask:1.0410}") private double initialEurUsdAsk;
    @Value("${rate.initial.gbpusd.bid:1.2590}") private double initialGbpUsdBid;
    @Value("${rate.initial.gbpusd.ask:1.2615}") private double initialGbpUsdAsk;

    /**
     * Kurları ilk değerleriyle başlatır.
     * <p>
     * Spring tarafından, bu servis bean'i oluşturulduktan sonra
     * otomatik olarak çağrılır. Desteklenen kurları varsayılan
     * değerleriyle başlatır.
     * </p>
     */
    @PostConstruct
    public void initializeRates() {
        log.info("Initializing rates...");
        addInitialRate("USDTRY", initialUsdTryBid, initialUsdTryAsk);
        addInitialRate("EURUSD", initialEurUsdBid, initialEurUsdAsk);
        addInitialRate("GBPUSD", initialGbpUsdBid, initialGbpUsdAsk);
        log.info("Rate service initialized with rates: {}", rates.keySet());
    }

    /**
     * Başlangıç kur değerlerini haritaya ekler.
     *
     * @param baseName Kur temel ismi (örn: "USDTRY")
     * @param bid Başlangıç alış fiyatı
     * @param ask Başlangıç satış fiyatı
     */
    private void addInitialRate(String baseName, double bid, double ask) {
        String fullRateName = platformId + "_" + baseName;
        rates.put(fullRateName, new Rate(fullRateName, bid, ask, Instant.now()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rate getRate(String fullRateName) {
        return rates.get(fullRateName);
    }

    /**
     * Tüm kurları günceller.
     * <p>
     * Bu metod, belirtilen sıklıkta otomatik olarak çalışır ve
     * tüm kurları rastgele dalgalanmalarla günceller.
     * </p>
     */
    @Scheduled(fixedRateString = "${rate.update.frequency:5000}")
    public void scheduledRateUpdateTask() {
        log.debug("Running scheduled rate update...");
        rates.values().forEach(this::applyFluctuation);
        log.info("All rates updated internally at {}", Instant.now());
    }

    /**
     * Bir kur nesnesine rastgele dalgalanma uygular.
     * <p>
     * Bu metod, belirtilen kur nesnesinin bid ve ask değerlerini
     * ayarlanmış dalgalanma yüzdesine göre rastgele değiştirir.
     * Ayrıca zaman damgasını günceller.
     * </p>
     *
     * @param rate Dalgalanma uygulanacak kur nesnesi
     */
    private void applyFluctuation(Rate rate) {
        double currentBid = rate.getBid();
        double currentAsk = rate.getAsk();

        // Dalgalanma faktörünü hesapla
        double fluctuationFactorBid = 1 + (random.nextDouble() - 0.5) * (fluctuationPercentage / 100.0) * 2;
        double fluctuationFactorAsk = 1 + (random.nextDouble() - 0.5) * (fluctuationPercentage / 100.0) * 2;

        // Yeni değerleri uygula (negatif olmamasını kontrol et)
        double newBid = Math.max(0.0001, currentBid * fluctuationFactorBid);
        double newAsk = Math.max(0.0001, currentAsk * fluctuationFactorAsk);

        // Ask'ın Bid'den yüksek olmasını ve minimum bir spread olmasını sağla
        double minSpread = newBid * (fluctuationPercentage / 400.0);
        if (newAsk <= newBid + minSpread) {
            newAsk = newBid + minSpread;
        }

        // Rate nesnesinin değerlerini güncelle
        rate.setBid(newBid);
        rate.setAsk(newAsk);
        rate.setTimestamp(Instant.now());
        log.trace("Updated rate {} to Bid: {}, Ask: {}", rate.getRateName(), newBid, newAsk);
    }
}