package com.findata.kafkaconsumerdb.Repository;

import com.findata.kafkaconsumerdb.Entity.RateRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RateRecord varlıkları için veritabanı erişim katmanı.
 * <p>
 * Bu repository, kur kayıtlarının veritabanında saklanması ve sorgulanması için 
 * gerekli metotları sağlar. Spring Data JPA'nın JpaRepository arayüzünü genişleterek
 * temel CRUD (Create, Read, Update, Delete) işlemlerini ve özel sorguları destekler.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @see RateRecord
 */
@Repository
public interface RateRepository extends JpaRepository<RateRecord, Long> {
    
    /**
     * Belirli bir kur adına göre en son kur kaydını bulur.
     * <p>
     * Bu metot, veritabanından belirtilen kur adına sahip en son güncellenen kaydı döndürür.
     * Bu, bir kur çiftinin en güncel değerlerini almak için kullanılabilir.
     * </p>
     * 
     * @param rateName Sorgulanacak kur adı (örneğin, "USDTRY", "EURTRY")
     * @return En son kaydedilen RateRecord nesnesi veya ilgili kur adı bulunamazsa null
     */
    RateRecord findTopByRateNameOrderByRateUpdatetimeDesc(String rateName);
} 