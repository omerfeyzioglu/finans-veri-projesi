package com.findata;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Döviz kuru bilgilerini ve thread-safe güncellemeleri yöneten sınıf.
 * <p>
 * Bu sınıf, bir döviz çiftinin (örn: USDTRY) alım-satım fiyatlarını temsil eder ve
 * değerlerini belirli bir volatilite ile güncellemek için bir mekanizma sağlar. 
 * Thread-safe olarak tasarlanmıştır, yani birden fazla thread tarafından
 * eşzamanlı olarak kullanılabilir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public class Rate {
    /** Kur ismi (örn: "USDTRY", "EURUSD") */
    private final String name;
    
    /** Kurun dalgalanma miktarını belirleyen volatilite değeri */
    private final double volatility;
    
    /** Alım (bid) ve satım (ask) değerlerini atomik olarak tutan referans */
    private final AtomicReference<Double[]> currentValues;

    /**
     * Belirtilen başlangıç değerleri ve volatilite ile yeni bir Rate nesnesi oluşturur.
     *
     * @param name Kur ismi (örn: "USDTRY")
     * @param initialBid Başlangıç alım fiyatı
     * @param initialAsk Başlangıç satım fiyatı
     * @param volatility Dalgalanma miktarını belirleyen volatilite değeri
     */
    public Rate(String name, double initialBid, double initialAsk, double volatility) {
        this.name = name;
        this.volatility = volatility;
        this.currentValues = new AtomicReference<>(new Double[]{initialBid, initialAsk});
    }

    /**
     * Kur ismini döndürür.
     *
     * @return Kur ismi
     */
    public String getName() {
        return name;
    }

    /**
     * Mevcut alım (bid) ve satım (ask) değerlerini içeren diziyi döndürür.
     * <p>
     * Dönen dizinin ilk elemanı (index 0) alım fiyatını (bid),
     * ikinci elemanı (index 1) satım fiyatını (ask) temsil eder.
     * </p>
     *
     * @return [bid, ask] değerlerini içeren Double dizisi
     */
    public Double[] getCurrentValues() {
        return currentValues.get();
    }

    /**
     * Kur değerlerini simüle edilen dalgalanma ile günceller.
     * <p>
     * Bu metod, mevcut alım ve satım fiyatlarını volatilite değerine göre
     * rastgele olarak değiştirir. Thread-safe bir şekilde çalışır ve her zaman
     * satım fiyatının (ask) alım fiyatından (bid) büyük olmasını sağlar.
     * </p>
     */
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