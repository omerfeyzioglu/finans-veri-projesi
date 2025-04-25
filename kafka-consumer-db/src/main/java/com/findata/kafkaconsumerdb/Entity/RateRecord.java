package com.findata.kafkaconsumerdb.Entity;

import jakarta.persistence.*; // JPA anotasyonları için
import java.math.BigDecimal; // Para birimleri için BigDecimal daha uygundur
import java.time.Instant;    // Zaman damgası için Instant (UTC)
import java.sql.Timestamp;

/**
 * Kur verileri için veritabanı entity sınıfı.
 * <p>
 * Bu sınıf, döviz kurlarının anlık değerlerini temsil eder ve veritabanında "rate_records" 
 * tablosuna kaydedilir. Her kayıt, belirli bir zaman noktasında bir döviz kuru çiftinin 
 * alış ve satış değerlerini içerir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 */
@Entity // Bu sınıfın bir JPA entity'si olduğunu belirtir
@Table(name = "tbl_rates") // Veritabanındaki tablo adıyla eşleştirir
public class RateRecord {

    /**
     * Kaydın benzersiz tanımlayıcısı (primary key).
     * Otomatik olarak oluşturulur.
     */
    @Id // Bu alanın primary key olduğunu belirtir
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID'nin otomatik artan olacağını belirtir (PostgreSQL için IDENTITY uygundur)
    private Long id; // Primary key (Long veya Integer olabilir)

    /**
     * Döviz kuru çiftinin adı (örneğin, "USDTRY", "EURTRY").
     */
    @Column(name = "rate_name", length = 20, nullable = false) // Sütun adı, uzunluk ve boş olamaz kısıtlaması (varchar(10) biraz kısa olabilir, 20 yaptım)
    private String rateName; // PF1_USDTRY veya USDTRY gibi

    /**
     * Döviz kurunun alış (bid) değeri.
     */
    @Column(name = "bid", precision = 19, scale = 8, nullable = false) // Decimal tipi için hassasiyet ve ölçek
    private BigDecimal bid;

    /**
     * Döviz kurunun satış (ask) değeri.
     */
    @Column(name = "ask", precision = 19, scale = 8, nullable = false) // Decimal tipi için hassasiyet ve ölçek
    private BigDecimal ask;

    /**
     * Kur verisinin zaman damgası.
     * Bu, verilerin alındığı veya oluşturulduğu zamanı gösterir.
     */
    @Column(name = "rate_updatetime", nullable = false) // Kaynaktaki (Kafka mesajındaki) zaman damgası
    private Instant rateUpdatetime;

    /**
     * Kaydın DB'ye eklendiği/güncellendiği zaman damgası
     */
    @Column(name = "db_updatetime", nullable = false)
    private Instant dbUpdatetime;

    /**
     * Varsayılan yapıcı metot.
     */
    public RateRecord() {
    }

    /**
     * Parametreli yapıcı metot.
     * 
     * @param rateName Kur çiftinin adı
     * @param bid Alış değeri
     * @param ask Satış değeri
     * @param rateUpdatetime Zaman damgası
     * @param dbUpdatetime Kayıt/güncelleme anının zaman damgası
     */
    public RateRecord(String rateName, BigDecimal bid, BigDecimal ask, Instant rateUpdatetime, Instant dbUpdatetime) {
        this.rateName = rateName;
        this.bid = bid;
        this.ask = ask;
        this.rateUpdatetime = rateUpdatetime;
        this.dbUpdatetime = dbUpdatetime;
    }

    // --- Standart Getter ve Setter Metotları ---

    /**
     * ID getter metodu.
     * 
     * @return Kaydın ID'si
     */
    public Long getId() {
        return id;
    }

    /**
     * ID setter metodu.
     * 
     * @param id Ayarlanacak ID değeri
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Kur adı getter metodu.
     * 
     * @return Kur çiftinin adı
     */
    public String getRateName() {
        return rateName;
    }

    /**
     * Kur adı setter metodu.
     * 
     * @param rateName Ayarlanacak kur adı
     */
    public void setRateName(String rateName) {
        this.rateName = rateName;
    }

    /**
     * Alış değeri getter metodu.
     * 
     * @return Alış değeri
     */
    public BigDecimal getBid() {
        return bid;
    }

    /**
     * Alış değeri setter metodu.
     * 
     * @param bid Ayarlanacak alış değeri
     */
    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    /**
     * Satış değeri getter metodu.
     * 
     * @return Satış değeri
     */
    public BigDecimal getAsk() {
        return ask;
    }

    /**
     * Satış değeri setter metodu.
     * 
     * @param ask Ayarlanacak satış değeri
     */
    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    /**
     * Zaman damgası getter metodu.
     * 
     * @return Kaydın zaman damgası
     */
    public Instant getRateUpdatetime() {
        return rateUpdatetime;
    }

    /**
     * Zaman damgası setter metodu.
     * 
     * @param rateUpdatetime Ayarlanacak zaman damgası
     */
    public void setRateUpdatetime(Instant rateUpdatetime) {
        this.rateUpdatetime = rateUpdatetime;
    }

    /**
     * Kayıt/güncelleme anının zaman damgası getter metodu.
     * 
     * @return Kayıt/güncelleme anının zaman damgası
     */
    public Instant getDbUpdatetime() {
        return dbUpdatetime;
    }

    public void setDbUpdatetime(Instant dbUpdatetime) {
        this.dbUpdatetime = dbUpdatetime;
    }

    @Override
    public String toString() {
        return "RateRecord{" +
                "id=" + id +
                ", rateName='" + rateName + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", rateUpdatetime=" + rateUpdatetime +
                ", dbUpdatetime=" + dbUpdatetime +
                '}';
    }

    // Veritabanına kaydetmeden hemen önce dbUpdatetime'ı ayarlamak için metot
    @PrePersist // Yeni kayıt eklenmeden önce çalışır
    @PreUpdate  // Kayıt güncellenmeden önce çalışır
    protected void onUpdate() {
        dbUpdatetime = Instant.now(); // Kayıt/güncelleme anının zaman damgasını set et
    }
}