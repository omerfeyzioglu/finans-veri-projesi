package com.findata.springplatform2;

import java.time.Instant; // LocalDateTime yerine Instant

/**
 * Döviz kuru bilgilerini taşıyan model sınıfı.
 * <p>
 * Bu sınıf, Platform 2 (PF2) uygulamasının işlediği kur verilerini temsil eder.
 * Her kur nesnesi, bir sembol ismi, alış (bid) ve satış (ask) fiyatları ve
 * bir zaman damgası (timestamp) içerir.
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public class Rate {
    /** Kur ismi, platform öneki ile birlikte (örn: "PF2_USDTRY") */
    private String rateName;
    
    /** Alış fiyatı */
    private double bid;
    
    /** Satış fiyatı */
    private double ask;
    
    /** Verinin zaman damgası (UTC, ISO-8601 formatında) */
    private Instant timestamp; // ISO-8601 uyumlu zaman damgası (UTC)

    /**
     * Varsayılan (boş) kurucu metod.
     * <p>
     * JSON serileştirme/deserileştirme işlemleri için gereklidir.
     * </p>
     */
    public Rate() {
    }

    /**
     * Tüm alanları içeren kurucu metod.
     *
     * @param rateName Kur ismi (platform öneki dahil)
     * @param bid Alış fiyatı
     * @param ask Satış fiyatı
     * @param timestamp Zaman damgası
     */
    public Rate(String rateName, double bid, double ask, Instant timestamp) {
        this.rateName = rateName;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    /**
     * Kur ismini döndürür.
     * @return Kur ismi
     */
    public String getRateName() { return rateName; }
    
    /**
     * Kur ismini ayarlar.
     * @param rateName Kur ismi
     */
    public void setRateName(String rateName) { this.rateName = rateName; }
    
    /**
     * Alış fiyatını döndürür.
     * @return Alış fiyatı
     */
    public double getBid() { return bid; }
    
    /**
     * Alış fiyatını ayarlar.
     * @param bid Alış fiyatı
     */
    public void setBid(double bid) { this.bid = bid; }
    
    /**
     * Satış fiyatını döndürür.
     * @return Satış fiyatı
     */
    public double getAsk() { return ask; }
    
    /**
     * Satış fiyatını ayarlar.
     * @param ask Satış fiyatı
     */
    public void setAsk(double ask) { this.ask = ask; }
    
    /**
     * Zaman damgasını döndürür.
     * @return Zaman damgası (UTC)
     */
    public Instant getTimestamp() { return timestamp; }
    
    /**
     * Zaman damgasını ayarlar.
     * @param timestamp Zaman damgası (UTC)
     */
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    /**
     * Kur nesnesinin String temsilini döndürür.
     *
     * @return Nesnenin okunabilir String temsili
     */
    @Override
    public String toString() {
        return "Rate{" +
                "rateName='" + rateName + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", timestamp=" + timestamp +
                '}';
    }
}