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

@Service // Spring Bean olarak işaretle
public class RateServiceImpl implements RateService { // Interface'i implement et

    private static final Logger log = LoggerFactory.getLogger(RateServiceImpl.class);

    // Map anahtarı olarak tam rate ismini (örn: "PF2_USDTRY") kullanıyoruz
    private final Map<String, Rate> rates = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Configuration values injected from application.properties
    @Value("${platform.id:PF2}")
    private String platformId;

    @Value("${rate.update.frequency:5000}")
    private long updateFrequency;

    @Value("${rate.fluctuation.percentage:0.5}")
    private double fluctuationPercentage;

    // Initial rate values from properties
    @Value("${rate.initial.usdtry.bid:34.80}") private double initialUsdTryBid;
    @Value("${rate.initial.usdtry.ask:35.10}") private double initialUsdTryAsk;
    @Value("${rate.initial.eurusd.bid:1.0370}") private double initialEurUsdBid;
    @Value("${rate.initial.eurusd.ask:1.0410}") private double initialEurUsdAsk;
    @Value("${rate.initial.gbpusd.bid:1.2590}") private double initialGbpUsdBid;
    @Value("${rate.initial.gbpusd.ask:1.2615}") private double initialGbpUsdAsk;

    @PostConstruct // Bean oluşturulduktan sonra çalışacak metot
    public void initializeRates() {
        log.info("Initializing rates...");
        addInitialRate("USDTRY", initialUsdTryBid, initialUsdTryAsk);
        addInitialRate("EURUSD", initialEurUsdBid, initialEurUsdAsk);
        addInitialRate("GBPUSD", initialGbpUsdBid, initialGbpUsdAsk);
        log.info("Rate service initialized with rates: {}", rates.keySet());
    }

    private void addInitialRate(String baseName, double bid, double ask) {
        String fullRateName = platformId + "_" + baseName;
        rates.put(fullRateName, new Rate(fullRateName, bid, ask, Instant.now()));
    }

    @Override
    public Rate getRate(String fullRateName) {
        // Map'ten doğrudan güncel Rate nesnesini döndür
        return rates.get(fullRateName);
    }

    @Scheduled(fixedRateString = "${rate.update.frequency:5000}") // application.properties'den okunan sıklıkta çalış
    public void scheduledRateUpdateTask() {
        log.debug("Running scheduled rate update...");
        rates.values().forEach(this::applyFluctuation); // Her bir rate için dalgalanmayı uygula
        log.info("All rates updated internally at {}", Instant.now());
    }

    // Rate nesnesini güncelleyen metot (thread-safe olmalı - Rate nesnesi içindeki değerler güncelleniyor)
    private void applyFluctuation(Rate rate) {
        double currentBid = rate.getBid();
        double currentAsk = rate.getAsk();

        // Dalgalanma faktörünü hesapla
        double fluctuationFactorBid = 1 + (random.nextDouble() - 0.5) * (fluctuationPercentage / 100.0) * 2;
        double fluctuationFactorAsk = 1 + (random.nextDouble() - 0.5) * (fluctuationPercentage / 100.0) * 2;

        // Yeni değerleri uygula (negatif olmamasını kontrol et)
        double newBid = Math.max(0.0001, currentBid * fluctuationFactorBid); // Sıfır olmasını engelle
        double newAsk = Math.max(0.0001, currentAsk * fluctuationFactorAsk);

        // Ask'ın Bid'den yüksek olmasını ve minimum bir spread olmasını sağla
        double minSpread = newBid * (fluctuationPercentage / 400.0); // Spread, dalgalanmanın bir kısmı kadar olsun
        if (newAsk <= newBid + minSpread) {
            newAsk = newBid + minSpread;
        }

        // Rate nesnesinin değerlerini güncelle
        rate.setBid(newBid);
        rate.setAsk(newAsk);
        rate.setTimestamp(Instant.now()); // Güncelleme zamanı
        log.trace("Updated rate {} to Bid: {}, Ask: {}", rate.getRateName(), newBid, newAsk);
    }
}