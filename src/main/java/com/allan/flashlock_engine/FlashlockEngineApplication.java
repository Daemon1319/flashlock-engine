package com.allan.flashlock_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FlashlockEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashlockEngineApplication.class, args);
	}

}
