package org.doubao.user.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan({
		"org.doubao.user.service.mapper.core",
		"org.doubao.user.service.mapper.relation",
		"org.doubao.user.service.mapper.report",
		"org.doubao.user.service.mapper.log"
})
@EnableFeignClients({
		"org.doubao.user.service.feign.core",
		"org.doubao.user.service.feign.report"
})
@EnableAspectJAutoProxy
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
