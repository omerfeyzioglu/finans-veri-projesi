package com.findata.mainapplication.Abstract;

import com.findata.mainapplication.model.Rate;

import java.util.Optional;
import java.util.Map;

/**
 * Döviz kuru verilerinin önbelleklenmesini sağlayan servis arayüzü.
 * <p>
 * Bu arayüz, farklı platformlardan gelen ham verilerin (raw rates) ve hesaplanmış 
 * verilerin (calculated rates) önbellekte saklanması ve alınması için gerekli
 * metodları tanımlar.
 * </p>
 * <p>
 * Önbellek sistemi, uygulamanın performansını artırmak ve verilere hızlı erişim
 * sağlamak için kullanılır. Bu arayüzün implementasyonu Redis gibi bir 
 * in-memory veri depolama sistemi kullanabilir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
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