package org.doubao.like.service.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "org.doubao.like.service") // 扫描整个服务包
@EnableScheduling
@EnableFeignClients(basePackages = "org.doubao.like.service.feign")
public class LikeServiceStarter {

	public static void main(String[] args) {
		SpringApplication.run(LikeServiceStarter.class, args);
	}

}