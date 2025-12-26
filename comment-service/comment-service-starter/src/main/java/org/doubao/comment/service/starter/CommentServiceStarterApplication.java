package org.doubao.comment.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("org.doubao.comment.service.mapper")
@ComponentScan(basePackages = "org.doubao.comment.service")
public class CommentServiceStarterApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommentServiceStarterApplication.class, args);
	}

}