package com.findata.mainapplication.Service;


import com.findata.mainapplication.Abstract.CalculationService;
import com.findata.mainapplication.model.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service // Bu sınıfın bir Spring Bean olduğunu belirtir
public class CalculationServiceImpl implements CalculationService {

    private static final Logger log = LoggerFactory.getLogger(CalculationServiceImpl.class);
    private static final String CALCULATED_PLATFORM_NAME = "CALC"; // Hesaplanan kurlar için platform adı

    @Override
    public Optional<Rate> calculateTargetRate(String targetSymbol, Map<String, Rate> rawRates) {
        log.debug("Attempting calculation for target symbol: {}", targetSymbol);
        // Gerekli ham kurları al
        Rate pf1UsdTry = rawRates.get("PF1_USDTRY");
        Rate pf2UsdTry = rawRates.get("PF2_USDTRY");
        Rate pf1EurUsd = rawRates.get("PF1_EURUSD");
        Rate pf2EurUsd = rawRates.get("PF2_EURUSD");
        Rate pf1GbpUsd = rawRates.get("PF1_GBPUSD");
        Rate pf2GbpUsd = rawRates.get("PF2_GBPUSD");

        // Hesaplamada kullanılan ham kurların en güncel zaman damgasını bulalım
        Instant latestTimestamp = findLatestTimestamp(rawRates);

        try {
            switch (targetSymbol) {
                case "USDTRY":
                    // Doğrudan USDTRY hesaplaması (iki platform ortalaması)
                    if (pf1UsdTry != null && pf2UsdTry != null) {
                        double bid = (pf1UsdTry.getBid() + pf2UsdTry.getBid()) / 2.0;
                        double ask = (pf1UsdTry.getAsk() + pf2UsdTry.getAsk()) / 2.0;
                        // Platformu "CALC" olarak işaretleyelim
                        return Optional.of(new Rate(CALCULATED_PLATFORM_NAME, targetSymbol, bid, ask, latestTimestamp));
                    }
                    break;

                case "EURTRY":
                    // EURTRY = USDTRY(mid) * EURUSD(avg)
                    if (pf1UsdTry != null && pf2UsdTry != null && pf1EurUsd != null && pf2EurUsd != null) {
                        double usdTryBidAvg = (pf1UsdTry.getBid() + pf2UsdTry.getBid()) / 2.0;
                        double usdTryAskAvg = (pf1UsdTry.getAsk() + pf2UsdTry.getAsk()) / 2.0;
                        double usdMid = (usdTryBidAvg + usdTryAskAvg) / 2.0; // USDTRY mid price

                        double eurUsdBidAvg = (pf1EurUsd.getBid() + pf2EurUsd.getBid()) / 2.0;
                        double eurUsdAskAvg = (pf1EurUsd.getAsk() + pf2EurUsd.getAsk()) / 2.0;

                        double bid = usdMid * eurUsdBidAvg;
                        double ask = usdMid * eurUsdAskAvg;
                        return Optional.of(new Rate(CALCULATED_PLATFORM_NAME, targetSymbol, bid, ask, latestTimestamp));
                    }
                    break;

                case "GBPTRY":
                    // GBPTRY = USDTRY(mid) * GBPUSD(avg)
                    if (pf1UsdTry != null && pf2UsdTry != null && pf1GbpUsd != null && pf2GbpUsd != null) {
                        double usdTryBidAvg = (pf1UsdTry.getBid() + pf2UsdTry.getBid()) / 2.0;
                        double usdTryAskAvg = (pf1UsdTry.getAsk() + pf2UsdTry.getAsk()) / 2.0;
                        double usdMid = (usdTryBidAvg + usdTryAskAvg) / 2.0; // USDTRY mid price

                        double gbpUsdBidAvg = (pf1GbpUsd.getBid() + pf2GbpUsd.getBid()) / 2.0;
                        double gbpUsdAskAvg = (pf1GbpUsd.getAsk() + pf2GbpUsd.getAsk()) / 2.0;

                        double bid = usdMid * gbpUsdBidAvg;
                        double ask = usdMid * gbpUsdAskAvg;
                        return Optional.of(new Rate(CALCULATED_PLATFORM_NAME, targetSymbol, bid, ask, latestTimestamp));
                    }
                    break;

                default:
                    log.warn("Unsupported target symbol for calculation: {}", targetSymbol);
                    return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error during calculation for symbol {}: {}", targetSymbol, e.getMessage(), e);
            return Optional.empty();
        }

        // Eğer gerekli ham kurlar bulunamadıysa buraya düşer
        log.warn("Calculation for {} failed due to missing raw rates in the input map.", targetSymbol);
        return Optional.empty();
    }

    // Hesaplamada kullanılan ham kurların en yeni zaman damgasını bulur
    private Instant findLatestTimestamp(Map<String, Rate> rawRates) {
        return rawRates.values().stream()
                .map(Rate::getTimestamp)
                .filter(java.util.Objects::nonNull) // Null timestamp'leri filtrele
                .max(Instant::compareTo)
                .orElse(Instant.now()); // Hiç geçerli timestamp yoksa şimdiki zaman
    }
}