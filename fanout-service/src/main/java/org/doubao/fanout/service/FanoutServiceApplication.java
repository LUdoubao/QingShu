package org.doubao.fanout.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.doubao.fanout.service.mapper")
public class FanoutServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FanoutServiceApplication.class, args);
	}

}
