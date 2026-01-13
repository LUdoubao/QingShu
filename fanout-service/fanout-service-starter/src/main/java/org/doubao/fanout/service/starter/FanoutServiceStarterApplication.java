package org.doubao.fanout.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.doubao.fanout.service.mapper")
public class FanoutServiceStarterApplication {

	public static void main(String[] args) {
		SpringApplication.run(FanoutServiceStarterApplication.class, args);
	}

}