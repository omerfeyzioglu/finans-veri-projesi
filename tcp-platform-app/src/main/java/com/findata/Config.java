package com.findata;

import java.util.List;
import java.util.Map;

// rates-config.json dosyasındaki yapılandırmayı temsil eden sınıf
public class Config {
    private int port;
    private List<Map<String, Object>> rates; // Geçici, Rate nesnelerine dönüştürülecek
    private int broadcastIntervalMs;

    // Getters and Setters (Gson tarafından kullanılacak)
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public List<Map<String, Object>> getRates() { return rates; }
    public void setRates(List<Map<String, Object>> rates) { this.rates = rates; }
    public int getBroadcastIntervalMs() { return broadcastIntervalMs; }
    public void setBroadcastIntervalMs(int broadcastIntervalMs) { this.broadcastIntervalMs = broadcastIntervalMs; }
}