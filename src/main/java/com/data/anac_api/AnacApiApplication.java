package com.data.anac_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class AnacApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnacApiApplication.class, args);
	}

}

//   - Email : admin@anac.tg
//   - Mot de passe : Admin123!