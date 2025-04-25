package com.findata.mainapplication.Abstract;


import com.findata.mainapplication.model.Rate;
import java.util.Optional;
import java.util.Map;

/**
 * Ham kurları kullanarak hedef kurları hesaplayan servis arayüzü.
 * <p>
 * Bu arayüz, farklı platformlardan gelen ham verileri (raw rates) kullanarak
 * hesaplanmış kur verilerini (calculated rates) üretmek için gerekli metodu tanımlar.
 * Kur hesaplama stratejileri, bu arayüzün implementasyonlarında gerçekleştirilir.
 * </p>
 * <p>
 * Örneğin, EURTRY kuru hesaplanırken EURUSD ve USDTRY kurları kullanılabilir
 * veya farklı platformlardan gelen aynı kur için ağırlıklı ortalama hesaplanabilir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
public interface CalculationService {

    /**
     * Gerekli ham kurları kullanarak hedef kuru hesaplar.
     * <p>
     * Bu metod, verilen sembole göre farklı hesaplama stratejileri uygulayabilir.
     * Hesaplama için yeterli veri yoksa Optional.empty() döndürür.
     * </p>
     * 
     * @param targetSymbol Hesaplanacak kur (örn: "USDTRY", "EURTRY", "GBPTRY")
     * @param rawRates Gerekli ham kurları içeren Map (Anahtar: Platform_Symbol örn: "PF1_USDTRY", Değer: Rate)
     * @return Hesaplanan Rate nesnesi veya hesaplama yapılamadıysa Optional.empty()
     */
    Optional<Rate> calculateTargetRate(String targetSymbol, Map<String, Rate> rawRates);
}