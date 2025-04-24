package com.findata.mainapplication.model;

import java.time.Instant;

// Lombok kullanıyorsak @Data @NoArgsConstructor @AllArgsConstructor eklenebilir
public class Rate {
    private String platform; // Verinin hangi platformdan geldiği (örn: "PF1", "PF2")
    private String symbol; // Kur sembolü (örn: "USDTRY", "EURUSD")
    private double bid;
    private double ask;
    private Instant timestamp; // Verinin platformdaki zaman damgası (UTC)

    // Constructor, Getter, Setter, toString...
    public Rate() {
    }

    public Rate(String platform, String symbol, double bid, double ask, Instant timestamp) {
        this.platform = platform;
        this.symbol = symbol;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public double getBid() { return bid; }
    public void setBid(double bid) { this.bid = bid; }
    public double getAsk() { return ask; }
    public void setAsk(double ask) { this.ask = ask; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        // Daha okunaklı bir toString
        return String.format("Rate[Platform=%s, Symbol=%s, Bid=%.5f, Ask=%.5f, Timestamp=%s]",
                platform, symbol, bid, ask, timestamp);
    }
}