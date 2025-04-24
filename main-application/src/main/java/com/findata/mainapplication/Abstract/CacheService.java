package com.findata.mainapplication.Abstract;

import com.findata.mainapplication.model.Rate;

import java.util.Optional;
import java.util.Map;

public interface CacheService {

    /**
     * Gelen ham kur verisini cache'e kaydeder.
     * @param rate Kaydedilecek Rate nesnesi (platform bilgisini içermeli).
     */
    void saveRawRate(Rate rate);

    /**
     * Belirli bir platform ve sembol için cache'deki son ham kuru getirir.
     * @param platform Platform adı (örn: "PF1")
     * @param symbol Sembol (örn: "USDTRY")
     * @return Rate nesnesi varsa Optional içinde döner, yoksa Optional.empty()
     */
    Optional<Rate> getRawRate(String platform, String symbol);

    /**
     * Belirli bir sembol için tüm platformlardan gelen son ham kurları getirir.
     * Anahtar: Platform adı, Değer: Rate nesnesi.
     * @param symbol Sembol (örn: "USDTRY")
     * @return Platform adlarına göre Rate nesnelerini içeren Map.
     */
    Map<String, Rate> getLatestRawRatesForSymbol(String symbol);

    /**
     * Hesaplanan kur verisini cache'e kaydeder.
     * @param rate Hesaplanan Rate nesnesi (platform alanı null veya "CALC" olabilir).
     */
    void saveCalculatedRate(Rate rate);

    /**
     * Belirli bir sembol için cache'deki son hesaplanmış kuru getirir.
     * @param symbol Sembol (örn: "USDTRY")
     * @return Hesaplanan Rate nesnesi varsa Optional içinde döner, yoksa Optional.empty()
     */
    Optional<Rate> getCalculatedRate(String symbol);

}