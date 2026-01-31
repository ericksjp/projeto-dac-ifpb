package com.ifpb.charger_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ChargerManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargerManagerApplication.class, args);
	}

}
