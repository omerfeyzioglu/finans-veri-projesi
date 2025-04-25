package com.findata;

import java.util.List;
import java.util.Map;

/**
 * JSON yapılandırma dosyasındaki yapıyı temsil eden model sınıfı.
 * <p>
 * Bu sınıf, rates-config.json dosyasından yüklenen yapılandırma verilerini
 * tutmak için kullanılır. TCP sunucusu için port, başlangıç kur değerleri ve
 * yayın aralığı gibi gerekli bilgileri içerir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public class Config {
    /** TCP sunucusunun dinleyeceği port numarası */
    private int port;
    
    /** JSON dosyasından yüklenen kur yapılandırmaları. Rate nesnelerine dönüştürülecek */
    private List<Map<String, Object>> rates;
    
    /** Kur güncellemelerinin yayınlanma aralığı (milisaniye cinsinden) */
    private int broadcastIntervalMs;

    /**
     * TCP sunucusunun dinleyeceği port numarasını döndürür.
     * @return Port numarası
     */
    public int getPort() { return port; }
    
    /**
     * TCP sunucusunun dinleyeceği port numarasını ayarlar.
     * @param port Port numarası
     */
    public void setPort(int port) { this.port = port; }
    
    /**
     * JSON dosyasından yüklenen kur yapılandırmalarını döndürür.
     * @return Kur yapılandırmalarını içeren liste
     */
    public List<Map<String, Object>> getRates() { return rates; }
    
    /**
     * Kur yapılandırmalarını ayarlar.
     * @param rates Kur yapılandırmalarını içeren liste
     */
    public void setRates(List<Map<String, Object>> rates) { this.rates = rates; }
    
    /**
     * Kur güncellemelerinin yayınlanma aralığını döndürür.
     * @return Yayın aralığı (milisaniye cinsinden)
     */
    public int getBroadcastIntervalMs() { return broadcastIntervalMs; }
    
    /**
     * Kur güncellemelerinin yayınlanma aralığını ayarlar.
     * @param broadcastIntervalMs Yayın aralığı (milisaniye cinsinden)
     */
    public void setBroadcastIntervalMs(int broadcastIntervalMs) { this.broadcastIntervalMs = broadcastIntervalMs; }
}