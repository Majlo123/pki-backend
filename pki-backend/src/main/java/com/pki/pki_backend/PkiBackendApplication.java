package com.pki.pki_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PkiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PkiBackendApplication.class, args);
	}

}
