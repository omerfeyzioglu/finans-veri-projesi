package com.findata.mainapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Finans Veri Projesi ana uygulama sınıfı.
 * <p>
 * Bu sınıf, Spring Boot uygulamasını başlatır ve çalışma zamanı yapılandırmasını sağlar.
 * {@code @EnableScheduling} anotasyonu, zamanlanmış görevlerin çalışmasını etkinleştirir.
 * </p>
 * 
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@SpringBootApplication
@EnableScheduling
public class MainApplication {

	/**
	 * Uygulamanın başlangıç noktası.
	 * 
	 * @param args Komut satırı argümanları
	 */
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

}
