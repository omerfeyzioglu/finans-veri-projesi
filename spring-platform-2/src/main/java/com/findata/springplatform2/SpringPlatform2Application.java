package com.findata.springplatform2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Platform 2 uygulamasının ana sınıfı.
 * <p>
 * Bu uygulama, finansal kur verileri sağlayan bir REST API hizmeti sunar.
 * Platform 2 (PF2) olarak işlev görür ve kur verilerini belirli aralıklarla
 * günceller. Zamanlanmış görevler için {@code EnableScheduling} anotasyonu
 * kullanılmıştır.
 * </p>
 *
 * @author Finans Veri Projesi Team
 * @version 1.0
 * @since 2025-04-25
 */
@SpringBootApplication
@EnableScheduling
public class SpringPlatform2Application {

	/**
	 * Uygulamanın başlangıç noktası.
	 *
	 * @param args Komut satırı argümanları
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringPlatform2Application.class, args);
	}

}
