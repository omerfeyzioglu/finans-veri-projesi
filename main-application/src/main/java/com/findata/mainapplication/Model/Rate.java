package com.findata.mainapplication.model;

import java.time.Instant;

/**
 * Döviz kuru bilgilerini temsil eden model sınıfı.
 * <p>
 * Bu sınıf, farklı platformlardan gelen döviz kuru verilerini standart bir formatta tutar.
 * Her bir kur için sembol, alış (bid), satış (ask) değerleri ve ilgili zaman bilgisi 
 * (timestamp) içerir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
// Lombok kullanıyorsak @Data @NoArgsConstructor @AllArgsConstructor eklenebilir
public class Rate {
    /** Verinin hangi platformdan geldiği (örn: "PF1", "PF2") */
    private String platform;
    
    /** Kur sembolü (örn: "USDTRY", "EURUSD") */
    private String symbol;
    
    /** Alış fiyatı */
    private double bid;
    
    /** Satış fiyatı */
    private double ask;
    
    /** Verinin platformdaki zaman damgası (UTC) */
    private Instant timestamp;

    /**
     * Boş constructor.
     */
    public Rate() {
    }

    /**
     * Tüm alanları belirterek bir Rate nesnesi oluşturur.
     *
     * @param platform Verinin geldiği platform tanımlayıcısı
     * @param symbol Döviz kur sembolü
     * @param bid Alış fiyatı
     * @param ask Satış fiyatı
     * @param timestamp Zaman damgası
     */
    public Rate(String platform, String symbol, double bid, double ask, Instant timestamp) {
        this.platform = platform;
        this.symbol = symbol;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    /**
     * Verinin geldiği platform bilgisini döndürür.
     * @return Platform tanımlayıcısı
     */
    public String getPlatform() { return platform; }
    
    /**
     * Verinin geldiği platformu ayarlar.
     * @param platform Platform tanımlayıcısı
     */
    public void setPlatform(String platform) { this.platform = platform; }
    
    /**
     * Döviz kur sembolünü döndürür.
     * @return Döviz kur sembolü (ör. "USDTRY")
     */
    public String getSymbol() { return symbol; }
    
    /**
     * Döviz kur sembolünü ayarlar.
     * @param symbol Döviz kur sembolü
     */
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
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
     * Rate nesnesinin insan tarafından okunabilir string temsilini döndürür.
     * @return Formatlanmış dize
     */
    @Override
    public String toString() {
        // Daha okunaklı bir toString
        return String.format("Rate[Platform=%s, Symbol=%s, Bid=%.5f, Ask=%.5f, Timestamp=%s]",
                platform, symbol, bid, ask, timestamp);
    }
}