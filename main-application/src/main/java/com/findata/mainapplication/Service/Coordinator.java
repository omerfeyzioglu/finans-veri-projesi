package com.findata.mainapplication.Service;


import com.findata.mainapplication.Abstract.*;
import com.findata.mainapplication.model.Rate;
import jakarta.annotation.PostConstruct; // Spring Boot 3 ile jakarta.* kullanılır
import jakarta.annotation.PreDestroy;   // Spring Boot 3 ile jakarta.* kullanılır
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Gerekirse
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service; // Spring Bean olarak işaretleyelim

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finans Veri Projesi'nin ana koordinasyon bileşeni.
 * <p>
 * Bu sınıf, farklı platformlardan (TCP, REST API vb.) gelen finans verilerini
 * koordine eder, işler ve dağıtır. Temel sorumlulukları:
 * <ul>
 *   <li>Platform bağlantılarını yönetme</li>
 *   <li>Gelen verileri doğrulama ve filtreleme</li>
 *   <li>Verileri önbelleğe alma</li>
 *   <li>Kur hesaplamalarını tetikleme</li>
 *   <li>Verileri Kafka'ya iletme</li>
 * </ul>
 * </p>
 * <p>
 * Coordinator, Observer tasarım deseni kullanarak platform bağlantılarından gelen
 * olayları dinler ve işler. Spring Boot çerçevesi üzerinde çalışır ve PlatformConnector
 * implementasyonlarını otomatik olarak keşfeder ve yönetir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@Service
public class Coordinator implements CoordinatorCallback {

    @Value("${coordinator.tolerance.percentage:1.0}") // Yüzde olarak tolerans değeri
    private double tolerancePercentage;
    private static final Logger log = LoggerFactory.getLogger(Coordinator.class);
    private final CacheService cacheService; // HashMap yerine CacheService
    private final CalculationService calculationService; // Yeni alan
    private final KafkaProducerService kafkaProducerService; // Yeni alan

    // Yönetilecek Platform Connector'lar. Spring bunları otomatik bulup enjekte edebilir.
    private final List<PlatformConnector> connectors;




    /**
     * Spring tarafından bağımlılıkların enjekte edildiği constructor.
     * <p>
     * Tüm PlatformConnector implementasyonları Spring tarafından bir liste olarak
     * otomatik enjekte edilir. Ayrıca gerekli diğer servisler de alınır.
     * </p>
     * 
     * @param connectors Kullanılacak platform bağlantı nesneleri listesi
     * @param cacheService Önbellekleme servisi
     * @param calculationService Kur hesaplama servisi
     * @param kafkaProducerService Kafka mesaj gönderim servisi
     */
    @Autowired
    public Coordinator(List<PlatformConnector> connectors,
                       CacheService cacheService,
                       CalculationService calculationService,
                       KafkaProducerService kafkaProducerService) {
        this.connectors = connectors != null ? new ArrayList<>(connectors) : new ArrayList<>();
        this.cacheService = cacheService;
        this.calculationService = calculationService;
        this.kafkaProducerService = kafkaProducerService;
    }


    /**
     * Servis başlatıldığında çalışan metod.
     * <p>
     * Tüm platform connector'ları başlatır, kendisini callback olarak tanımlar
     * ve varsayılan kur aboneliklerini ayarlar.
     * </p>
     */
    @PostConstruct
    public void start() {
        log.info("Coordinator starting... Found {} platform connectors.", connectors.size());
        if (connectors.isEmpty()) {
            log.warn("No PlatformConnectors found. Please ensure implementations are available as Spring beans.");
        }
        connectors.forEach(connector -> {
            connector.setCallback(this); // Kendimizi callback olarak ver
            try {
                log.info("Connecting to platform: {}", connector.getPlatformName());
                connector.connect(); // Bağlanmayı dene (asenkron olabilir)
            } catch (Exception e) {
                log.error("Failed to initiate connection for platform {}: {}", connector.getPlatformName(), e.getMessage(), e);
            }
        });

        // TODO: Hangi kurlara abone olunacağını konfigürasyondan oku
        List<String> symbolsToSubscribe = List.of("USDTRY", "EURUSD", "GBPUSD"); // Örnek
        subscribeToRates(symbolsToSubscribe);
    }

    /**
     * Servis durdurulduğunda çalışan metod.
     * <p>
     * Tüm platform bağlantılarını kapatır ve kaynakları serbest bırakır.
     * </p>
     */
    @PreDestroy
    public void stop() {
        log.info("Coordinator shutting down...");
        connectors.forEach(connector -> {
            try {
                log.info("Disconnecting from platform: {}", connector.getPlatformName());
                connector.disconnect();
            } catch (Exception e) {
                log.error("Error disconnecting from platform {}: {}", connector.getPlatformName(), e.getMessage(), e);
            }
        });
        log.info("Coordinator stopped.");
    }

    /**
     * Belirtilen sembollere abone olunmasını tüm platformlar için ayarlar.
     * <p>
     * Her platform connectoru için belirtilen kurları izlemeye başlar.
     * </p>
     * 
     * @param symbolsToSubscribe Abone olunacak kur sembolleri listesi
     */
    private void subscribeToRates(List<String> symbolsToSubscribe) {
        // Bağlantının kurulmasını beklemek gerekebilir (özellikle TCP için)
        // Şimdilik basitçe deneme yapalım
        // TODO: Bağlantı durumunu kontrol et veya onConnect içinde subscribe yap
        log.info("Attempting to subscribe to symbols: {}", symbolsToSubscribe);
        connectors.forEach(connector -> {
            // if (connector.isConnected()) { // Bağlantı kontrolü eklenebilir
            symbolsToSubscribe.forEach(symbol -> {
                try {
                    log.info("Subscribing to {} on platform {}", symbol, connector.getPlatformName());
                    connector.subscribe(symbol);
                } catch (Exception e) {
                    log.error("Failed to subscribe to {} on platform {}: {}", symbol, connector.getPlatformName(), e.getMessage());
                }
            });
            // } else {
            //    log.warn("Platform {} not connected yet, skipping subscription.", connector.getPlatformName());
            // }
        });
    }

    // --- CoordinatorCallback Implementation ---

    /**
     * Bir platform bağlantısı kurulduğunda çağrılan callback metodu.
     * 
     * @param platformName Bağlantı kurulan platformun adı
     */
    @Override
    public void onConnect(String platformName) {
        log.info("Callback: Successfully connected to platform: {}", platformName);
        // TODO: Bağlantı kurulduktan sonra yapılacak işlemler (örn: abonelikleri başlatmak)
        // Belki burada subscribeToRates çağrılabilir? Veya connector'lar kendi içinde yönetir.
    }

    /**
     * Bir platform bağlantısı kesildiğinde çağrılan callback metodu.
     * 
     * @param platformName Bağlantısı kesilen platformun adı
     */
    @Override
    public void onDisconnect(String platformName) {
        log.warn("Callback: Disconnected from platform: {}", platformName);
        // TODO: Alarm üretme, tekrar bağlanmayı deneme vb.
    }

    /**
     * Bir platformdan yeni kur verisi geldiğinde çağrılan callback metodu.
     * <p>
     * Bu metod, gelen kur verisini doğrular, önbelleğe kaydeder, tolerans
     * kontrolünden geçirir ve gerekirse hesaplamalara dahil eder.
     * </p>
     * 
     * @param newRate Gelen yeni kur verisi
     */
    @Override
    public void onRateUpdate(Rate newRate) {
        log.debug("Callback: Received rate update: {}", newRate);
        if (newRate == null || newRate.getPlatform() == null || newRate.getSymbol() == null || newRate.getTimestamp() == null) {
            log.warn("Received invalid rate update (null or missing fields): {}", newRate);
            return;
        }

        // 1. Ham Veriyi Cache'e Kaydet
        cacheService.saveRawRate(newRate);
        log.trace("Raw rate saved to cache via CacheService for {}/{}", newRate.getPlatform(), newRate.getSymbol());

        // 2. Veri Temizleme (%1 Tolerans)
        boolean isRateValid = checkRateTolerance(newRate);
        log.debug("Tolerance check result for {}/{}: {}", newRate.getPlatform(), newRate.getSymbol(), isRateValid);

        if (isRateValid) {
            // 3. Ham veriyi (geçerli ise) Kafka'ya gönder
            kafkaProducerService.sendRawRate(newRate);

            // 4. Hesaplamaları Tetikle
            triggerCalculations(newRate.getSymbol());

        } else {
            log.warn("Rate tolerance check failed for {}/{}. Update ignored for calculations/publishing.", newRate.getPlatform(), newRate.getSymbol());
        }
    }


    /**
     * Bir platformda hata oluştuğunda çağrılan callback metodu.
     * 
     * @param platformName Hata oluşan platformun adı
     * @param error Hata mesajı
     */
    @Override
    public void onError(String platformName, String error) {
        log.error("Callback: Error reported from platform {}: {}", platformName, error);
    }

    // --- Internal Logic Methods (Stubs/Updates) ---

    /**
     * Gelen kur verisinin tolerans kontrolünü yapar.
     * <p>
     * İki platformdan gelen aynı kurlar arasında, belirtilen tolerans yüzdesinden
     * fazla bir fark varsa veriyi geçersiz kabul eder. Bu, anormal verilerin
     * sisteme girmesini engeller.
     * </p>
     * 
     * @param newRate Kontrol edilecek yeni kur verisi
     * @return Kur verisi geçerliyse true, değilse false
     */
    private boolean checkRateTolerance(Rate newRate) {
        String symbol = newRate.getSymbol();
        // Yeni gelen kurun platformu Dışındaki platformun adını bul
        String otherPlatform = newRate.getPlatform().equals("PF1") ? "PF2" : "PF1";

        // Cache'den her iki platform için de SON kayıtlı değerleri alalım
        Optional<Rate> rateFromPlatform1Opt = cacheService.getRawRate("PF1", symbol);
        Optional<Rate> rateFromPlatform2Opt = cacheService.getRawRate("PF2", symbol);

        // Eğer her iki platformdan da henüz veri cache'de yoksa, karşılaştırma yapamayız.
        // Doküman bu durum için bir şey belirtmiyor, ilk gelen verileri geçerli kabul etmek mantıklı.
        if (rateFromPlatform1Opt.isEmpty() || rateFromPlatform2Opt.isEmpty()) {
            log.debug("Tolerance check skipped for {}/{}: Both platforms must have data in cache for comparison. Accepting rate.",
                    newRate.getPlatform(), symbol);
            return true;
        }

        // Cache'deki son değerlerin mid fiyatlarını hesapla
        Rate cachedRatePF1 = rateFromPlatform1Opt.get();
        Rate cachedRatePF2 = rateFromPlatform2Opt.get();

        double midPricePF1 = calculateMidPrice(cachedRatePF1);
        double midPricePF2 = calculateMidPrice(cachedRatePF2);

        // Eğer cache'deki değerlerden biri geçersizse (örn: bid/ask <= 0), kontrolü atla
        if (midPricePF1 <= 0 || midPricePF2 <= 0) {
            log.warn("Tolerance check skipped for {}/{}: Invalid mid price(s) found in cache (PF1: {}, PF2: {}). Accepting rate.",
                    newRate.getPlatform(), symbol, midPricePF1, midPricePF2);
            return true;
        }

        // 1. "iki platformda aynı kur için ulaşılan son veri ortalaması"nı hesapla
        double averageMidPriceFromCache = (midPricePF1 + midPricePF2) / 2.0;

        // 2. "son yayınlanan kur"un (yani yeni gelen kurun) mid fiyatını hesapla
        double newMidPrice = calculateMidPrice(newRate);

        // Yeni gelen kur geçersizse ne yapmalı? Şimdilik kabul edelim.
        if (newMidPrice <= 0) {
            log.warn("Tolerance check skipped for {}/{}: Invalid mid price for the newly received rate ({}). Accepting rate.",
                    newRate.getPlatform(), symbol, newMidPrice);
            return true;
        }

        // 3. Aradaki farkı kontrol et
        double differencePercentage = Math.abs(newMidPrice - averageMidPriceFromCache) / averageMidPriceFromCache * 100.0;

        log.debug("Tolerance Check for {}/{}: NewMid={}, AvgMidFromCache={}, Diff%={:.4f}, Tolerance%={}",
                newRate.getPlatform(), symbol, newMidPrice, averageMidPriceFromCache, differencePercentage, tolerancePercentage);

        boolean isValid = differencePercentage <= tolerancePercentage;

        if (!isValid) {
            log.warn("RATE TOLERANCE FAILED for {}/{}: NewMid={}, AvgMidFromCache={}, Diff%={:.4f} > Tolerance%={}",
                    newRate.getPlatform(), symbol, newMidPrice, averageMidPriceFromCache, differencePercentage, tolerancePercentage);
        }

        return isValid;
    }

    // Mid price hesaplamak için yardımcı metot (Aynı kalabilir)
    private double calculateMidPrice(Rate rate) {
        if (rate == null || rate.getBid() <= 0 || rate.getAsk() <= 0) {
            return 0; // Geçersiz veya eksik veri durumu
        }
        return (rate.getBid() + rate.getAsk()) / 2.0;
    }


    // Hesaplamaları tetikleyen metot (İçi dolduruldu)
    private void triggerCalculations(String updatedRawSymbol) {
        log.debug("Triggering calculations potentially affected by update in {}", updatedRawSymbol);

        List<String> targetSymbolsToRecalculate = new ArrayList<>();
        switch (updatedRawSymbol) {
            case "USDTRY":
                targetSymbolsToRecalculate.add("USDTRY"); // USDTRY doğrudan hesaplanır
                targetSymbolsToRecalculate.add("EURTRY"); // USDTRY, EURTRY'yi etkiler
                targetSymbolsToRecalculate.add("GBPTRY"); // USDTRY, GBPTRY'yi etkiler
                break;
            case "EURUSD":
                targetSymbolsToRecalculate.add("EURTRY"); // EURUSD, EURTRY'yi etkiler
                break;
            case "GBPUSD":
                targetSymbolsToRecalculate.add("GBPTRY"); // GBPUSD, GBPTRY'yi etkiler
                break;
            default:
                // Diğer semboller (örn: XAUUSD vs.) gelirse ve hesaplamaları etkilemiyorsa logla/geç
                log.debug("No calculation defined for updated raw symbol: {}", updatedRawSymbol);
                return;
        }

        // İlgili hedef kurları hesaplamayı dene
        targetSymbolsToRecalculate.forEach(this::calculateAndPublish);
    }

    // Belirli bir hedef kuru hesaplayıp yayınlayan metot (İçi dolduruldu)
    private void calculateAndPublish(String targetSymbol) {
        log.debug("Attempting to calculate and publish target symbol: {}", targetSymbol);

        // 1. Hesaplama için gerekli tüm ham kurları Cache'den topla
        Map<String, Rate> requiredRawRates = gatherRequiredRawRates(targetSymbol);

        // 2. Gerekli tüm ham kurların cache'de olup olmadığını kontrol et
        if (!areAllRatesAvailable(targetSymbol, requiredRawRates)) {
            log.warn("Skipping calculation for {}: Required raw rates not available in cache. Needed: {} rates from PF1/PF2.",
                    targetSymbol, getNeededSymbolsForTarget(targetSymbol));
            return;
        }

        // 3. CalculationService ile hesaplamayı yap
        Optional<Rate> calculatedRateOpt = calculationService.calculateTargetRate(targetSymbol, requiredRawRates);

        if (calculatedRateOpt.isPresent()) {
            Rate calculatedRate = calculatedRateOpt.get();
            log.info("Calculation successful for {}: {}", targetSymbol, calculatedRate); // INFO seviyesine çektim

            // 4. Hesaplanan kuru Cache'e kaydet
            cacheService.saveCalculatedRate(calculatedRate);
            log.trace("Saved calculated rate to cache via CacheService for {}", targetSymbol);

            // 5. Hesaplanan kuru Kafka'ya gönder
            kafkaProducerService.sendCalculatedRate(calculatedRate);

        } else {
            log.warn("Calculation failed or returned empty for target symbol: {}", targetSymbol);
        }
    }

    // Hesaplama için gerekli ham kurları cache'den getiren yardımcı metot (Aynı kalmalı)
    private Map<String, Rate> gatherRequiredRawRates(String targetSymbol) {
        Map<String, Rate> requiredRates = new ConcurrentHashMap<>();
        List<String> symbolsNeeded = getNeededSymbolsForTarget(targetSymbol);

        for (String symbol : symbolsNeeded) {
            cacheService.getRawRate("PF1", symbol).ifPresent(rate -> requiredRates.put("PF1_" + symbol, rate));
            cacheService.getRawRate("PF2", symbol).ifPresent(rate -> requiredRates.put("PF2_" + symbol, rate));
        }
        log.trace("Gathered raw rates for {} calculation: {}", targetSymbol, requiredRates.keySet());
        return requiredRates;
    }

    // Belirli bir hedef kur için hangi ham kurların gerektiğini döndüren yardımcı metot (Aynı kalmalı)
    private List<String> getNeededSymbolsForTarget(String targetSymbol) {
        return switch (targetSymbol) {
            case "USDTRY" -> List.of("USDTRY");
            case "EURTRY" -> List.of("USDTRY", "EURUSD");
            case "GBPTRY" -> List.of("USDTRY", "GBPUSD");
            default -> List.of();
        };
    }

    // Gerekli tüm ham kurların (her iki platformdan da) cache'de olup olmadığını kontrol eder (Aynı kalmalı)
    private boolean areAllRatesAvailable(String targetSymbol, Map<String, Rate> gatheredRates) {
        List<String> symbolsNeeded = getNeededSymbolsForTarget(targetSymbol);
        if (symbolsNeeded.isEmpty()) {
            log.error("Cannot check rate availability: No needed symbols defined for target {}", targetSymbol);
            return false;
        }

        for (String symbol : symbolsNeeded) {
            // Hem PF1 hem de PF2 için veri var mı kontrol et
            if (!gatheredRates.containsKey("PF1_" + symbol) || !gatheredRates.containsKey("PF2_" + symbol)) {
                log.debug("Rate availability check failed for {}: Missing {} from PF1 or PF2.", targetSymbol, symbol);
                return false; // Gerekli kurlardan biri eksik
            }
        }
        return true; // Tüm gerekli kurlar mevcut
    }
}