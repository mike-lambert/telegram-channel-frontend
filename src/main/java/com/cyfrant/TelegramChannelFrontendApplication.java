package com.cyfrant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TelegramChannelFrontendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramChannelFrontendApplication.class, args);
	}

}
