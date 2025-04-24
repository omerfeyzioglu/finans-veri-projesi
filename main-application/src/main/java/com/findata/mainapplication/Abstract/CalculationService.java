package com.findata.mainapplication.Abstract;


import com.findata.mainapplication.model.Rate;
import java.util.Optional;
import java.util.Map;

/**
 * Ham kurları kullanarak hedef kurları hesaplayan servis arayüzü.
 */
public interface CalculationService {

    /**
     * Gerekli ham kurları kullanarak hedef kuru hesaplar.
     * @param targetSymbol Hesaplanacak kur (örn: "USDTRY", "EURTRY", "GBPTRY")
     * @param rawRates Gerekli ham kurları içeren Map (Anahtar: Platform_Symbol örn: "PF1_USDTRY", Değer: Rate)
     * @return Hesaplanan Rate nesnesi veya hesaplama yapılamadıysa Optional.empty()
     */
    Optional<Rate> calculateTargetRate(String targetSymbol, Map<String, Rate> rawRates);
}