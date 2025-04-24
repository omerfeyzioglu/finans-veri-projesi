package com.findata;

import java.util.concurrent.atomic.AtomicReference;

// Kur bilgilerini ve thread-safe güncellemeyi yöneten sınıf
public class Rate {
    private final String name;
    private final double volatility;
    // Bid/Ask değerlerini atomik olarak güncellemek için AtomicReference kullanıyoruz
    private final AtomicReference<Double[]> currentValues;

    public Rate(String name, double initialBid, double initialAsk, double volatility) {
        this.name = name;
        this.volatility = volatility;
        this.currentValues = new AtomicReference<>(new Double[]{initialBid, initialAsk});
    }

    public String getName() {
        return name;
    }

    public Double[] getCurrentValues() {
        return currentValues.get();
    }

    // Kur değerlerini simüle edilen dalgalanma ile günceller
    public void update() {
        currentValues.updateAndGet(current -> {
            // Negatif değerleri önlemek için basit kontrol
            double newBid = Math.max(0.0, current[0] + (Math.random() - 0.5) * volatility);
            double newAsk = Math.max(0.0, current[1] + (Math.random() - 0.5) * volatility);
            // Genellikle Ask > Bid olmalı, basit bir ayarlama yapılabilir (opsiyonel)
            if (newAsk <= newBid) {
                newAsk = newBid + Math.abs((Math.random() - 0.5) * volatility * 0.1); // Küçük bir fark ekle
            }
            return new Double[]{newBid, newAsk};
        });
    }
}