package com.quotepilot;

import com.quotepilot.service.PriceNormalizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuotepilotBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(QuotepilotBackendApplication.class, args);
	}

}
