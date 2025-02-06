package com.team1.travel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class TravelProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelProjectApplication.class, args);
	}

}
