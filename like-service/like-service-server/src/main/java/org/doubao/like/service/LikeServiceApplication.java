package org.doubao.like.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "org.doubao.like.service.feign")
public class LikeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LikeServiceApplication.class, args);
	}

}