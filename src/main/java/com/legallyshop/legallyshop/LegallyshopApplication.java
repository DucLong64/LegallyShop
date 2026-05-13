package com.legallyshop.legallyshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LegallyshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegallyshopApplication.class, args);
	}

}
