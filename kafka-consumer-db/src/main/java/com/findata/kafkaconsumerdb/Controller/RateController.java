package com.findata.kafkaconsumerdb.Controller;

import com.findata.kafkaconsumerdb.Entity.RateRecord;
import com.findata.kafkaconsumerdb.Repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * Kur bilgilerine erişim için REST API kontrolcüsü.
 * <p>
 * Bu kontrolcü, veritabanında saklanan kur verilerine HTTP endpoint'leri aracılığıyla
 * erişim sağlar. Tüm kurları listeleme ve belirli bir kuru sorgulama gibi
 * işlemleri destekler.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @see RateRecord
 * @see RateRepository
 */

@RestController
@RequestMapping("/api/rates")
public class RateController {

    @Autowired
    private RateRepository repository;

    /**
     * Tüm kur kayıtlarını getiren endpoint.
     * <p>
     * Veritabanında bulunan tüm kur kayıtlarını döndürür. Bu endpoint, genel bir bakış
     * veya tüm kur verilerinin alınması gerektiğinde kullanılır.
     * </p>
     * 
     * @return Tüm kur kayıtlarının listesi
     */

    @GetMapping
    public List<RateRecord> getAllRates() {
        return repository.findAll();
    }

    /**
     * Belirli bir kur adına göre en son kaydı getiren endpoint.
     * <p>
     * URL'de belirtilen kur adına göre en son güncellenen kaydı döndürür.
     * Eğer belirtilen kur adı için kayıt bulunamazsa 404 hatası döner.
     * </p>
     * 
     * @param rateName Sorgulanacak kur adı (örneğin, "USDTRY", "EURTRY")
     * @return İlgili kurun en son kaydı veya 404 hatası
     */
    @GetMapping("/{rateName}")
    public ResponseEntity<RateRecord> getLatestRateByName(@PathVariable String rateName) {
        RateRecord record = repository.findTopByRateNameOrderByRateUpdatetimeDesc(rateName);
        if (record != null) {
            return ResponseEntity.ok(record);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 