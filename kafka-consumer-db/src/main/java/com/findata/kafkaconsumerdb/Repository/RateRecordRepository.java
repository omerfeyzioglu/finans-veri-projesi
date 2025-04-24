package com.findata.kafkaconsumerdb.Repository;

import com.findata.kafkaconsumerdb.Entity.RateRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateRecordRepository extends JpaRepository<RateRecord, Long> {
    // JpaRepository, CRUD işlemleri için gerekli metotları sağlar
    // Ekstra bir şey yapmaya gerek yok, JpaRepository yeterli
    // Eğer özel sorgular gerekiyorsa, burada tanımlayabiliriz
}
