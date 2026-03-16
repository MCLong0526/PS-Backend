package com.points.PS_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsBackendApplication.class, args);
	}

}
