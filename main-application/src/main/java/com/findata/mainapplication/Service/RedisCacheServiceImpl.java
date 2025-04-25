package com.findata.mainapplication.Service;

import com.findata.mainapplication.Abstract.CacheService;
import com.findata.mainapplication.model.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis tabanlı önbellekleme servisi implementasyonu.
 * <p>
 * Bu sınıf, CacheService arayüzünü Redis kullanarak implemente eder.
 * Ham kur verileri ve hesaplanmış kur verileri Redis'te farklı anahtarlar
 * altında saklanır ve yönetilir.
 * </p>
 * <p>
 * Ham veriler için kullanılan anahtar formatı: 
 * "raw:{platform}:{symbol}" (örn: "raw:PF1:USDTRY")
 * </p>
 * <p>
 * Hesaplanmış veriler için kullanılan anahtar formatı: 
 * "calc:{symbol}" (örn: "calc:USDTRY")
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@Service
public class RedisCacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheServiceImpl.class);

    // Sabit anahtar ön ekleri
    private static final String RAW_KEY_PREFIX = "raw:";
    private static final String CALC_KEY_PREFIX = "calc:";

    // Redis istemcisi
    private final RedisTemplate<String, Object> redisTemplate;

    // Önbellek süresi (saniye) - varsayılan 1 saat
    @Value("${cache.ttl.seconds:3600}")
    private long cacheTtlSeconds;

    /**
     * Bağımlılıkların Spring tarafından enjekte edildiği constructor.
     *
     * @param redisTemplate Redis işlemleri için kullanılacak template
     */
    @Autowired
    public RedisCacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Ham kur verisi için Redis anahtarı oluşturur.
     *
     * @param platform Platform adı
     * @param symbol Kur sembolü
     * @return Oluşturulan Redis anahtarı
     */
    private String getRawRateKey(String platform, String symbol) {
        return RAW_KEY_PREFIX + platform + ":" + symbol;
    }

    /**
     * Hesaplanmış kur verisi için Redis anahtarı oluşturur.
     *
     * @param symbol Kur sembolü
     * @return Oluşturulan Redis anahtarı
     */
    private String getCalculatedRateKey(String symbol) {
        return CALC_KEY_PREFIX + symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRawRate(Rate rate) {
        if (rate == null || rate.getPlatform() == null || rate.getSymbol() == null) {
            log.warn("Attempting to save invalid raw rate to cache: {}", rate);
            return;
        }

        String key = getRawRateKey(rate.getPlatform(), rate.getSymbol());
        try {
            redisTemplate.opsForValue().set(key, rate, Duration.ofSeconds(cacheTtlSeconds));
            log.debug("Raw rate saved to Redis: {}", key);
        } catch (Exception e) {
            log.error("Error saving raw rate to Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Rate> getRawRate(String platform, String symbol) {
        if (platform == null || symbol == null) {
            log.warn("Attempting to get raw rate with null platform or symbol: platform={}, symbol={}", platform, symbol);
            return Optional.empty();
        }

        String key = getRawRateKey(platform, symbol);
        try {
            Rate rate = (Rate) redisTemplate.opsForValue().get(key);
            log.debug("Raw rate lookup from Redis: key={}, found={}", key, rate != null);
            return Optional.ofNullable(rate);
        } catch (Exception e) {
            log.error("Error getting raw rate from Redis: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Rate> getLatestRawRatesForSymbol(String symbol) {
        if (symbol == null) {
            log.warn("Attempting to get latest raw rates with null symbol");
            return Map.of();
        }

        Map<String, Rate> result = new HashMap<>();
        // Şu an için sadece PF1 ve PF2 platformlarını destekliyoruz
        // TODO: Dinamik platform listesi eklenebilir
        try {
            getRawRate("PF1", symbol).ifPresent(rate -> result.put("PF1", rate));
            getRawRate("PF2", symbol).ifPresent(rate -> result.put("PF2", rate));
        } catch (Exception e) {
            log.error("Error getting latest raw rates for symbol {}: {}", symbol, e.getMessage(), e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCalculatedRate(Rate rate) {
        if (rate == null || rate.getSymbol() == null) {
            log.warn("Attempting to save invalid calculated rate to cache: {}", rate);
            return;
        }

        String key = getCalculatedRateKey(rate.getSymbol());
        try {
            redisTemplate.opsForValue().set(key, rate, Duration.ofSeconds(cacheTtlSeconds));
            log.debug("Calculated rate saved to Redis: {}", key);
        } catch (Exception e) {
            log.error("Error saving calculated rate to Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Rate> getCalculatedRate(String symbol) {
        if (symbol == null) {
            log.warn("Attempting to get calculated rate with null symbol");
            return Optional.empty();
        }

        String key = getCalculatedRateKey(symbol);
        try {
            Rate rate = (Rate) redisTemplate.opsForValue().get(key);
            log.debug("Calculated rate lookup from Redis: key={}, found={}", key, rate != null);
            return Optional.ofNullable(rate);
        } catch (Exception e) {
            log.error("Error getting calculated rate from Redis: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}