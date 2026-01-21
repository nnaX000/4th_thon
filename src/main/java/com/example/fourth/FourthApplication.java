package com.example.fourth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FourthApplication {

	public static void main(String[] args) {
		SpringApplication.run(FourthApplication.class, args);
	}

}
