package com.findata.springplatform2;

import java.time.Instant; // LocalDateTime yerine Instant

public class Rate {
    private String rateName;
    private double bid;
    private double ask;
    private Instant timestamp; // ISO-8601 uyumlu zaman damgası (UTC)

    // Default constructor (JSON serialization için gerekli olabilir)
    public Rate() {
    }

    public Rate(String rateName, double bid, double ask, Instant timestamp) {
        this.rateName = rateName;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    // Standard Getters and Setters
    public String getRateName() { return rateName; }
    public void setRateName(String rateName) { this.rateName = rateName; }
    public double getBid() { return bid; }
    public void setBid(double bid) { this.bid = bid; }
    public double getAsk() { return ask; }
    public void setAsk(double ask) { this.ask = ask; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

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