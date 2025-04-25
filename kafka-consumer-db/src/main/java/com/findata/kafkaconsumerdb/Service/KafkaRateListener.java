package com.findata.kafkaconsumerdb.Service;

import com.findata.kafkaconsumerdb.Entity.RateRecord;
import com.findata.kafkaconsumerdb.Repository.RateRecordRepository;
import com.google.gson.JsonSyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.Instant;

/**
 * Kafka'dan gelen kur verilerini dinleyen ve veritabanına kaydeden servis sınıfı.
 * <p>
 * Bu sınıf, belirtilen Kafka topic'inden kur verilerini dinler, gelen JSON formatındaki
 * mesajları ayrıştırır ve {@link RateRecord} nesnelerine dönüştürüp veritabanına kaydeder.
 * Hata durumlarını uygun şekilde log'lar ve yönetir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @see RateRecord
 * @see RateRecordRepository
 */
@Component
public class KafkaRateListener {
    private final Logger logger = LoggerFactory.getLogger(KafkaRateListener.class);
    
    @Autowired
    private RateRecordRepository rateRecordRepository;
    
    /**
     * Kafka'dan gelen mesajları işleyen metot.
     * <p>
     * Bu metot, "findata" topic'inden gelen JSON formatındaki mesajları dinler ve işler.
     * Mesajı ayrıştırır, geçerli bir {@link RateRecord} nesnesine dönüştürür ve veritabanına kaydeder.
     * </p>
     * 
     * @param message Kafka'dan alınan JSON formatındaki mesaj
     */
    @KafkaListener(topics = "findata", groupId = "group_id" )
    public void consume(String message) {
        logger.info("kafka message -> {}", message);
        RateRecord rateRecord = parseMessage(message);
        
        if (rateRecord != null) {
            try {
                rateRecordRepository.save(rateRecord);
            } catch (DataAccessException dataAccessException) {
                logger.error("Veritabanına kayıt sırasında hata oluştu", dataAccessException);
            } catch (Exception e) {
                logger.error("Beklenmeyen bir hata oluştu", e);
            }
        }
    }
    
    /**
     * JSON formatındaki mesajı ayrıştıran ve {@link RateRecord} nesnesine dönüştüren metot.
     * <p>
     * Bu metot, gelen JSON mesajını ayrıştırır ve gerekli alanları RateRecord nesnesine atar.
     * JSON ayrıştırma hatalarını ve diğer olası hataları yakalar ve uygun şekilde log'lar.
     * </p>
     * 
     * @param message Ayrıştırılacak JSON formatındaki mesaj
     * @return Oluşturulan RateRecord nesnesi veya hata durumunda null
     */
    private RateRecord parseMessage(String message) {
        try {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            
            RateRecord rateRecord = new RateRecord();
            rateRecord.setName(jsonObject.get("name").getAsString());
            rateRecord.setBidValue(jsonObject.get("bid").getAsDouble());
            rateRecord.setAskValue(jsonObject.get("ask").getAsDouble());
            rateRecord.setTimestamp(Timestamp.from(Instant.now()));
            
            return rateRecord;
        } catch (JsonSyntaxException jsonSyntaxException) {
            logger.error("JSON ayrıştırma hatası: {}", jsonSyntaxException.getMessage());
        } catch (NullPointerException nullPointerException) {
            logger.error("JSON'da eksik alan bulunuyor: {}", nullPointerException.getMessage());
        } catch (Exception e) {
            logger.error("Mesaj ayrıştırma sırasında beklenmeyen hata: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Bir dizeden tarih/zaman değeri ayrıştıran yardımcı metot.
     * <p>
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" formatında verilen bir tarih/zaman dizesini
     * {@link Date} nesnesine dönüştürür.
     * </p>
     * 
     * @param dateString Ayrıştırılacak tarih/zaman dizesi
     * @return Ayrıştırılan Date nesnesi
     * @throws ParseException Tarih/zaman dizesi geçerli formatta değilse
     */
    private Date parseTimestamp(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateFormat.parse(dateString);
    }
}