package com.findata.mainapplication.Service;

import com.findata.mainapplication.Abstract.CalculationService;
import com.findata.mainapplication.model.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Kur hesaplamalarını gerçekleştiren servis implementasyonu.
 * <p>
 * Bu sınıf, ham kur verilerini (raw rates) kullanarak hesaplanmış kur verilerini
 * (calculated rates) üretir. Farklı hesaplama stratejileri uygular:
 * <ul>
 *   <li>Doğrudan kur verileri için ağırlıklı ortalama (weighted average)</li>
 *   <li>Çapraz kur hesaplamaları (cross rate calculations)</li>
 * </ul>
 * </p>
 * <p>
 * Örneğin, EURTRY kuru doğrudan platformlardan gelen veri varsa platform verilerinin
 * ortalaması, yoksa EURUSD ve USDTRY kurları kullanılarak hesaplanır.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@Service
public class CalculationServiceImpl implements CalculationService {

    private static final Logger log = LoggerFactory.getLogger(CalculationServiceImpl.class);

    // Dinamik hesaplama metodları için kullanılacak sabitler
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final String GBP = "GBP";
    private static final String TRY = "TRY";
    private static final String CALC = "CALC"; // Hesaplanmış veriler için platform adı

    /**
     * {@inheritDoc}
     * <p>
     * Bu metod, hedef kur için uygun hesaplama stratejisini belirler ve uygular.
     * Örneğin:
     * <ul>
     *   <li>USDTRY için doğrudan kur verileri kullanılır</li>
     *   <li>EURTRY için doğrudan veriler yoksa, EURUSD ve USDTRY üzerinden hesaplama yapılır</li>
     * </ul>
     * </p>
     */
    @Override
    public Optional<Rate> calculateTargetRate(String targetSymbol, Map<String, Rate> rawRates) {
        log.debug("Calculating rate for: {}", targetSymbol);
        log.trace("Available raw rates for calculation: {}", rawRates);
        if (targetSymbol == null || rawRates == null || rawRates.isEmpty()) {
            log.warn("Invalid input for calculation: targetSymbol={}, rawRates={}", targetSymbol, rawRates);
            return Optional.empty();
        }

        // ADIM 1: Platformlara göre ilgili sembol için veri var mı, bul
        Set<String> directRateKeys = findDirectRateKeys(targetSymbol, rawRates.keySet());
        log.debug("Found direct rate keys for {}: {}", targetSymbol, directRateKeys);

        // ADIM 2: Doğrudan kur verisi varsa, ağırlıklı ortalama hesapla
        if (!directRateKeys.isEmpty()) {
            log.debug("Using direct rate calculation for {}", targetSymbol);
            return calculateFromDirectRates(targetSymbol, directRateKeys, rawRates);
        }

        // ADIM 3: Dolaylı hesaplama stratejilerini dene
        log.debug("No direct rates found for {}, trying cross rate calculations", targetSymbol);

        // a) Para birimlerini ayır (USDTRY -> USD ve TRY, EURUSD -> EUR ve USD)
        String[] currencies = extractCurrencies(targetSymbol);
        if (currencies.length != 2) {
            log.warn("Invalid symbol format for cross calculation: {}", targetSymbol);
            return Optional.empty();
        }

        // b) Bilinen çapraz kur hesaplama algoritmalarını dene
        if (isEURTRYCrossRate(targetSymbol)) {
            log.debug("Using EURTRY cross rate calculation via EUR/USD and USD/TRY");
            return calculateEURTRYCrossRate(rawRates);
        } else if (isGBPTRYCrossRate(targetSymbol)) {
            log.debug("Using GBPTRY cross rate calculation via GBP/USD and USD/TRY");
            return calculateGBPTRYCrossRate(rawRates);
        }

        // c) Diğer semboller için başka hesaplama stratejileri
        // TODO: Gerekirse daha fazla sembol için hesaplama metodları eklenebilir.
        log.warn("No calculation strategy available for symbol: {}", targetSymbol);
        return Optional.empty();
    }

    /**
     * Sembol içindeki iki para birimini ayırır.
     * Örnek: "USDTRY" -> ["USD", "TRY"]
     *
     * @param symbol Ayrıştırılacak sembol
     * @return Para birimleri dizisi, genellikle 2 elemanlı
     */
    private String[] extractCurrencies(String symbol) {
        // Bu metod, sembolü para birimlerine ayırır
        // Örneğin: "USDTRY" -> ["USD", "TRY"]
        // TODO: Farklı uzunlukta para birimleri için daha gelişmiş bir algoritma gerekebilir.
        if (symbol == null || symbol.length() < 6) {
            return new String[0];
        }
        return new String[]{symbol.substring(0, 3), symbol.substring(3)};
    }

    /**
     * Sembolün EURTRY çapraz kur hesaplaması gerektirip gerektirmediğini kontrol eder.
     *
     * @param symbol Kontrol edilecek sembol
     * @return EURTRY hesaplaması gerekiyorsa true
     */
    private boolean isEURTRYCrossRate(String symbol) {
        return "EURTRY".equals(symbol);
    }

    /**
     * Sembolün GBPTRY çapraz kur hesaplaması gerektirip gerektirmediğini kontrol eder.
     *
     * @param symbol Kontrol edilecek sembol
     * @return GBPTRY hesaplaması gerekiyorsa true
     */
    private boolean isGBPTRYCrossRate(String symbol) {
        return "GBPTRY".equals(symbol);
    }

    /**
     * Ham kurlardan doğrudan bir sembol için kur hesaplar.
     * <p>
     * Bu metod, aynı sembol için farklı platformlardan gelen
     * verileri ağırlıklı ortalama kullanarak birleştirir.
     * </p>
     *
     * @param symbol Hesaplanacak sembol
     * @param rateKeys Bu sembol için kullanılacak platform_sembol anahtarları
     * @param rawRates Tüm ham kurları içeren harita
     * @return Hesaplanmış kur nesnesi
     */
    private Optional<Rate> calculateFromDirectRates(String symbol, Set<String> rateKeys, Map<String, Rate> rawRates) {
        // TODO: İleriki aşamalarda farklı platformlara farklı ağırlıklar atanabilir.
        double totalBid = 0.0;
        double totalAsk = 0.0;
        int count = 0;
        Instant latestTimestamp = null;

        for (String key : rateKeys) {
            Rate rate = rawRates.get(key);
            if (rate != null) {
                totalBid += rate.getBid();
                totalAsk += rate.getAsk();
                count++;

                // En yeni timestamp'i bul
                if (latestTimestamp == null || rate.getTimestamp().isAfter(latestTimestamp)) {
                    latestTimestamp = rate.getTimestamp();
                }
            }
        }

        if (count == 0 || latestTimestamp == null) {
            log.warn("No valid rates found for direct calculation of {}", symbol);
            return Optional.empty();
        }

        // Ortalama hesapla
        double avgBid = totalBid / count;
        double avgAsk = totalAsk / count;

        // Yeni Rate nesnesi oluştur (platform = "CALC" ile)
        Rate calculatedRate = new Rate(CALC, symbol, avgBid, avgAsk, latestTimestamp);
        log.debug("Calculated direct rate for {}: {}", symbol, calculatedRate);
        return Optional.of(calculatedRate);
    }

    /**
     * EURUSD ve USDTRY kurlarını kullanarak EURTRY kurunu hesaplar.
     *
     * @param rawRates Tüm ham kurları içeren harita
     * @return Hesaplanmış EURTRY kur nesnesi
     */
    private Optional<Rate> calculateEURTRYCrossRate(Map<String, Rate> rawRates) {
        // Gerekli kurları bul
        Optional<Rate> eurusdOpt = findBestRate("EURUSD", rawRates);
        Optional<Rate> usdtryOpt = findBestRate("USDTRY", rawRates);

        if (eurusdOpt.isEmpty() || usdtryOpt.isEmpty()) {
            log.warn("Cannot calculate EURTRY: missing required rates");
            return Optional.empty();
        }

        Rate eurusd = eurusdOpt.get();
        Rate usdtry = usdtryOpt.get();

        // EURTRY = EURUSD * USDTRY
        double bidRate = eurusd.getBid() * usdtry.getBid();
        double askRate = eurusd.getAsk() * usdtry.getAsk();

        // En yeni timestamp'i kullan
        Instant timestamp = eurusd.getTimestamp().isAfter(usdtry.getTimestamp()) ? 
                            eurusd.getTimestamp() : usdtry.getTimestamp();

        Rate calculatedRate = new Rate(CALC, "EURTRY", bidRate, askRate, timestamp);
        log.debug("Calculated EURTRY cross rate: {}", calculatedRate);
        return Optional.of(calculatedRate);
    }

    /**
     * GBPUSD ve USDTRY kurlarını kullanarak GBPTRY kurunu hesaplar.
     *
     * @param rawRates Tüm ham kurları içeren harita
     * @return Hesaplanmış GBPTRY kur nesnesi
     */
    private Optional<Rate> calculateGBPTRYCrossRate(Map<String, Rate> rawRates) {
        // Gerekli kurları bul
        Optional<Rate> gbpusdOpt = findBestRate("GBPUSD", rawRates);
        Optional<Rate> usdtryOpt = findBestRate("USDTRY", rawRates);

        if (gbpusdOpt.isEmpty() || usdtryOpt.isEmpty()) {
            log.warn("Cannot calculate GBPTRY: missing required rates");
            return Optional.empty();
        }

        Rate gbpusd = gbpusdOpt.get();
        Rate usdtry = usdtryOpt.get();

        // GBPTRY = GBPUSD * USDTRY
        double bidRate = gbpusd.getBid() * usdtry.getBid();
        double askRate = gbpusd.getAsk() * usdtry.getAsk();

        // En yeni timestamp'i kullan
        Instant timestamp = gbpusd.getTimestamp().isAfter(usdtry.getTimestamp()) ? 
                            gbpusd.getTimestamp() : usdtry.getTimestamp();

        Rate calculatedRate = new Rate(CALC, "GBPTRY", bidRate, askRate, timestamp);
        log.debug("Calculated GBPTRY cross rate: {}", calculatedRate);
        return Optional.of(calculatedRate);
    }

    /**
     * Verilen sembol için en iyi kur bilgisini bulur.
     * <p>
     * Eğer birden fazla platform varsa, platformlardan ortalama alır.
     * </p>
     *
     * @param symbol Aranan sembol
     * @param rawRates Tüm ham kurları içeren harita
     * @return En iyi kur bilgisi
     */
    private Optional<Rate> findBestRate(String symbol, Map<String, Rate> rawRates) {
        Set<String> directRateKeys = findDirectRateKeys(symbol, rawRates.keySet());
        return calculateFromDirectRates(symbol, directRateKeys, rawRates);
    }

    /**
     * Bir sembol için doğrudan kullanılabilecek ham kur anahtarlarını bulur.
     *
     * @param symbol Aranan sembol
     * @param allKeys Tüm mevcut anahtarlar
     * @return İlgili sembol için kullanılabilecek anahtar kümesi
     */
    private Set<String> findDirectRateKeys(String symbol, Set<String> allKeys) {
        // Platform_Symbol formatında anahtarları filtrele
        return allKeys.stream()
                .filter(key -> key.endsWith("_" + symbol))
                .collect(java.util.stream.Collectors.toSet());
    }
}