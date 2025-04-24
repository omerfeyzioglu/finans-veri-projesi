package com.findata.springplatform2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringPlatform2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringPlatform2Application.class, args);
	}

}
