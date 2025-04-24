package com.findata.mainapplication.Abstract;

/**
 * Farklı veri platformlarına bağlanmak için genel arayüz.
 * Her platform (TCP, REST, FIX vb.) bu arayüzü implemente etmelidir.
 */
public interface PlatformConnector {

    void connect();

    void disconnect();

    void subscribe(String symbol);

    void unsubscribe(String symbol);

    /**
     * Bu connector'dan gelen bildirimleri alacak olan Coordinator örneğini ayarlar.
     */
    void setCallback(CoordinatorCallback callback);

    boolean isConnected();

    /**
     * Bu connector'ın temsil ettiği platformun adını döndürür (Konfigürasyondan alınabilir).
     */
    String getPlatformName();
}