package com.findata.mainapplication.Config;

import com.fasterxml.jackson.annotation.JsonTypeInfo; // Gerekli import
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator; // Şimdilik kullanmayabiliriz
// import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator; // Şimdilik kullanmayabiliriz
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis önbellek yapılandırması.
 * <p>
 * Bu sınıf, Spring Data Redis entegrasyonu için gerekli bean'leri yapılandırır.
 * Özellikle RedisTemplate bean'i, Java nesnelerinin Redis'e JSON formatında
 * serileştirilmesini ve Redis'ten deserileştirilmesini sağlar.
 * </p>
 * <p>
 * Bu yapılandırma, Rate nesnelerinin Jackson kütüphanesi kullanılarak
 * JSON formatında saklanmasını ve Instant gibi Java 8 tarih/zaman
 * nesnelerinin doğru şekilde işlenmesini sağlar.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate bean'ini yapılandırır.
     * <p>
     * Bu bean, Java nesnelerini Redis'e kaydetmek ve okumak için kullanılır.
     * Key'ler String olarak, value'lar ise JSON formatında saklanır.
     * </p>
     * <p>
     * Jackson ObjectMapper, sınıf bilgisini JSON içine ekler (@class alanı),
     * böylece deserializasyon sırasında doğru tipte nesneler oluşturulabilir.
     * JavaTimeModule, Java 8 tarih/zaman nesnelerinin doğru serileştirilmesini sağlar.
     * </p>
     * 
     * @param connectionFactory Redis bağlantı fabrikası
     * @return Yapılandırılmış RedisTemplate nesnesi
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value Serializer (Jackson JSON)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Instant desteği


        // YENİ (Daha Standart Yaklaşım):
        // Tip bilgisini JSON içine '@class' alanı olarak ekle (NON_FINAL en yaygın kullanılanlardan biridir)
        // Bu, Jackson'a hangi sınıfa deserialize edeceğini söyler.
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        // !!! DEĞİŞTİRİLMİŞ KISIM !!!

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}