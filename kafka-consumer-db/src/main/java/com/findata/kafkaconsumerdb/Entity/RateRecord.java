package com.findata.kafkaconsumerdb.Entity;

import jakarta.persistence.*; // JPA anotasyonları için
import java.math.BigDecimal; // Para birimleri için BigDecimal daha uygundur
import java.time.Instant;    // Zaman damgası için Instant (UTC)

@Entity // Bu sınıfın bir JPA entity'si olduğunu belirtir
@Table(name = "tbl_rates") // Veritabanındaki tablo adıyla eşleştirir
public class RateRecord {

    @Id // Bu alanın primary key olduğunu belirtir
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID'nin otomatik artan olacağını belirtir (PostgreSQL için IDENTITY uygundur)
    private Long id; // Primary key (Long veya Integer olabilir)

    @Column(name = "rate_name", length = 20, nullable = false) // Sütun adı, uzunluk ve boş olamaz kısıtlaması (varchar(10) biraz kısa olabilir, 20 yaptım)
    private String rateName; // PF1_USDTRY veya USDTRY gibi

    @Column(name = "bid", precision = 19, scale = 8, nullable = false) // Decimal tipi için hassasiyet ve ölçek
    private BigDecimal bid;

    @Column(name = "ask", precision = 19, scale = 8, nullable = false) // Decimal tipi için hassasiyet ve ölçek
    private BigDecimal ask;

    @Column(name = "rate_updatetime", nullable = false) // Kaynaktaki (Kafka mesajındaki) zaman damgası
    private Instant rateUpdatetime;

    @Column(name = "db_updatetime", nullable = false) // Kaydın DB'ye eklendiği/güncellendiği zaman damgası
    private Instant dbUpdatetime;

    // JPA için gerekli olan boş constructor
    public RateRecord() {
    }

    // Tüm alanları içeren constructor (isteğe bağlı, kolaylık için)
    public RateRecord(String rateName, BigDecimal bid, BigDecimal ask, Instant rateUpdatetime, Instant dbUpdatetime) {
        this.rateName = rateName;
        this.bid = bid;
        this.ask = ask;
        this.rateUpdatetime = rateUpdatetime;
        this.dbUpdatetime = dbUpdatetime;
    }

    // --- Standart Getter ve Setter Metotları ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRateName() {
        return rateName;
    }

    public void setRateName(String rateName) {
        this.rateName = rateName;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public Instant getRateUpdatetime() {
        return rateUpdatetime;
    }

    public void setRateUpdatetime(Instant rateUpdatetime) {
        this.rateUpdatetime = rateUpdatetime;
    }

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