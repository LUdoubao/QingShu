package org.doubao.like.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "org.doubao.like.service")
@EnableScheduling
@EnableFeignClients(basePackages = "org.doubao.like.service.feign")
@EnableDiscoveryClient
@MapperScan("org.doubao.like.service.mapper")
public class LikeServiceStarter {

	public static void main(String[] args) {
		SpringApplication.run(LikeServiceStarter.class, args);
	}

}