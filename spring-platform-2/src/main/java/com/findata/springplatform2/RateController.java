package com.findata.springplatform2;





import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rates") // Tüm endpoint'ler için temel yol
public class RateController {

    private static final Logger log = LoggerFactory.getLogger(RateController.class);

    private final RateService rateService; // Interface tipinde enjekte et (DIP)

    @Autowired // Constructor injection (önerilen)
    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping("/{rateName}") // Örn: /api/rates/PF2_USDTRY
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