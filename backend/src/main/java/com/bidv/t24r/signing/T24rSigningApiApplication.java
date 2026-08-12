package com.bidv.t24r.signing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class T24rSigningApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(T24rSigningApiApplication.class, args);
	}

}
