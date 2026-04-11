package com.csrd.RDSystemcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableScheduling
@SpringBootApplication
public class RdSystemcdApplication {

	public static void main(String[] args) {

		// 🔥 TEMPORARY (run karke password copy karo)
		System.out.println("ENCODED PASSWORD: " + new BCryptPasswordEncoder().encode("1234"));

		SpringApplication.run(RdSystemcdApplication.class, args);
		System.out.print("Success.......");
	}

}