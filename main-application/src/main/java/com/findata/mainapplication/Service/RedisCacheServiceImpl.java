package com.findata.mainapplication.Service;

import com.findata.mainapplication.Abstract.CacheService;
import com.findata.mainapplication.model.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service // Spring Bean olarak işaretle
public class RedisCacheServiceImpl implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheServiceImpl.class);

    // Redis ile etkileşim için Spring Data Redis'in template'i
    // RedisConfig'de tanımladığımız bean buraya enjekte edilecek.
    private final RedisTemplate<String, Object> redisTemplate;
    // Hash operasyonları için kısayol
    private final HashOperations<String, String, Rate> hashOperations;

    private static final String RAW_RATE_KEY_PREFIX = "RAW_RATE:"; // Ham kurlar için anahtar ön eki
    private static final String CALC_RATE_KEY_PREFIX = "CALC_RATE:"; // Hesaplanan kurlar için anahtar ön eki
    private static final long CACHE_TTL_MINUTES = 60; // Cache verisi ne kadar süre tutulsun (opsiyonel)

    @Autowired
    public RedisCacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // HashOperations'ı burada başlatamayız, String, Object değil String, Rate olmalı.
        // Her kullanımda redisTemplate üzerinden alacağız.
        this.hashOperations = redisTemplate.opsForHash();
    }

    // Ham kur verisini platform ve sembol bazında ayrı ayrı saklayalım
    // veya tek bir hash içinde tutalım. Hash daha mantıklı olabilir.
    // Ana Hash Key: RAW_RATE:SYMBOL (örn: RAW_RATE:USDTRY)
    // Hash Field Key: Platform (örn: PF1, PF2)
    // Hash Field Value: Rate nesnesi (JSON'a çevrilmiş)
    @Override
    public void saveRawRate(Rate rate) {
        if (rate == null || rate.getSymbol() == null || rate.getPlatform() == null) {
            log.warn("Cannot save null rate or rate with null symbol/platform.");
            return;
        }
        String hashKey = RAW_RATE_KEY_PREFIX + rate.getSymbol();
        String fieldKey = rate.getPlatform();
        try {
            // RedisTemplate<String, Rate> kullanmak daha type-safe olurdu ama Object ile de çalışır.
            // HashOperations<String, String, Object> tipinde alıp Rate'e cast etmeyi deneyelim.
            HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
            ops.put(hashKey, fieldKey, rate);
            // Opsiyonel: Cache'e TTL (Time-To-Live) ekleyebiliriz
            redisTemplate.expire(hashKey, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.trace("Saved raw rate to cache: Key={}, Field={}, Value={}", hashKey, fieldKey, rate);
        } catch (Exception e) {
            log.error("Failed to save raw rate to Redis cache for key={}, field={}: {}", hashKey, fieldKey, e.getMessage(), e);
        }
    }

    @Override
    public Optional<Rate> getRawRate(String platform, String symbol) {
        if (symbol == null || platform == null) return Optional.empty();
        String hashKey = RAW_RATE_KEY_PREFIX + symbol;
        try {
            HashOperations<String, String, Object> ops = redisTemplate.opsForHash();
            Object cachedValue = ops.get(hashKey, platform);
            // Gelen Object'i Rate'e cast etmemiz lazım (JSON serializer bunu yapmalı)
            if (cachedValue instanceof Rate) {
                return Optional.of((Rate) cachedValue);
            } else if (cachedValue != null) {
                // Yanlış tip geldiyse logla
                log.warn("Unexpected type found in raw rate cache for {}/{}: {}", platform, symbol, cachedValue.getClass().getName());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get raw rate from Redis cache for key={}, field={}: {}", hashKey, platform, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Map<String, Rate> getLatestRawRatesForSymbol(String symbol) {
        if (symbol == null) return Map.of(); // Boş map döndür
        String hashKey = RAW_RATE_KEY_PREFIX + symbol;
        try {
            HashOperations<String, String, Rate> ops = redisTemplate.opsForHash(); // Rate'e cast etmeyi dene
            return ops.entries(hashKey);
        } catch (Exception e) {
            log.error("Failed to get all raw rates for symbol {} from Redis cache: {}", symbol, e.getMessage(), e);
            return Map.of(); // Hata durumunda boş map
        }
    }


    // Hesaplanan kurları basit key-value olarak saklayalım
    // Key: CALC_RATE:SYMBOL (örn: CALC_RATE:USDTRY)
    // Value: Rate nesnesi (JSON'a çevrilmiş)
    @Override
    public void saveCalculatedRate(Rate rate) {
        if (rate == null || rate.getSymbol() == null) {
            log.warn("Cannot save null calculated rate or rate with null symbol.");
            return;
        }
        String key = CALC_RATE_KEY_PREFIX + rate.getSymbol();
        try {
            redisTemplate.opsForValue().set(key, rate, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.trace("Saved calculated rate to cache: Key={}, Value={}", key, rate);
        } catch (Exception e) {
            log.error("Failed to save calculated rate to Redis cache for key={}: {}", key, e.getMessage(), e);
        }
    }

    @Override
    public Optional<Rate> getCalculatedRate(String symbol) {
        if (symbol == null) return Optional.empty();
        String key = CALC_RATE_KEY_PREFIX + symbol;
        try {
            Object cachedValue = redisTemplate.opsForValue().get(key);
            if (cachedValue instanceof Rate) {
                return Optional.of((Rate) cachedValue);
            } else if (cachedValue != null) {
                log.warn("Unexpected type found in calculated rate cache for {}: {}", symbol, cachedValue.getClass().getName());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get calculated rate from Redis cache for key={}: {}", key, e.getMessage(), e);
            return Optional.empty();
        }
    }
}