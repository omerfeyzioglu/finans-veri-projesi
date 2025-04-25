package com.findata.springplatform2;





import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kur verilerine erişim sağlayan REST kontrolcüsü.
 * <p>
 * Bu sınıf, Platform 2 (PF2) uygulamasının REST API endpoint'lerini tanımlar.
 * İstemcilerin kur verilerine HTTP istekleri aracılığıyla erişmelerini sağlar.
 * </p>
 * <p>
 * Tüm endpoint'ler "/api/rates" taban yolunu kullanır.
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@RestController
@RequestMapping("/api/rates")
public class RateController {

    /** Loglama için kullanılan Logger nesnesi */
    private static final Logger log = LoggerFactory.getLogger(RateController.class);

    /** Kur verilerine erişim sağlayan servis */
    private final RateService rateService;

    /**
     * Bağımlılıkların enjekte edildiği constructor.
     *
     * @param rateService Kur verilerine erişim sağlayan servis
     */
    @Autowired
    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    /**
     * İsimle belirtilen kur verisini getirir.
     * <p>
     * Bu endpoint, tam kur ismi (örn: "PF2_USDTRY") ile belirtilen
     * kur verisini JSON formatında döndürür. Kur bulunamazsa
     * 404 Not Found yanıtı döner.
     * </p>
     * <p>
     * Örnek: GET /api/rates/PF2_USDTRY
     * </p>
     *
     * @param rateName Kur ismi (platform öneki dahil)
     * @return Kur verisi veya 404 Not Found yanıtı
     */
    @GetMapping("/{rateName}")
    public ResponseEntity<Rate> getRateByName(@PathVariable String rateName) {
        log.info("GET request received for rate: {}", rateName);
        // Gelen rateName'i (PF2_USDTRY gibi) doğrudan service'e ver
        Rate rate = rateService.getRate(rateName);

        if (rate != null) {
            log.debug("Rate found: {}", rate);
            // Rate nesnesini 200 OK ile döndür
            return ResponseEntity.ok(rate);
        } else {
            log.warn("Rate not found for name: {}", rateName);
            // Rate bulunamazsa 404 Not Found döndür
            return ResponseEntity.notFound().build();
        }
    }
}