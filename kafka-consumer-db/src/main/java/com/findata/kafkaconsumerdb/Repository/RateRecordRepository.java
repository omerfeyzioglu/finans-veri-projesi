package com.findata.kafkaconsumerdb.Repository;

import com.findata.kafkaconsumerdb.Entity.RateRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Kur kayıtları için veri erişim katmanı.
 * <p>
 * Bu arayüz, {@link RateRecord} entity'si için JPA Repository işlevselliği sağlar.
 * Spring Data JPA tarafından çalışma zamanında uygulanır ve temel CRUD (Create, Read, Update, Delete)
 * operasyonlarını otomatik olarak sağlar.
 * </p>
 * <p>
 * Özel sorgulara ihtiyaç duyulduğunda bu arayüze yeni metotlar eklenebilir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @see RateRecord
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
public interface RateRecordRepository extends JpaRepository<RateRecord, Long> {
    // JpaRepository, CRUD işlemleri için gerekli metotları sağlar
    // Ekstra bir şey yapmaya gerek yok, JpaRepository yeterli
    // Eğer özel sorgular gerekiyorsa, burada tanımlayabiliriz
}
