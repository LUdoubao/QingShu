package org.doubao.user.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan({
		"org.doubao.user.server.core.mapper",
		"org.doubao.user.server.relation.mapper",
		"org.doubao.user.server.report.mapper",
		"org.doubao.user.server.log.mapper"
})
@EnableFeignClients({
		"org.doubao.user.server.core.feign",
		"org.doubao.user.server.relation.feign",
		"org.doubao.user.server.report.feign"
})
@EnableAspectJAutoProxy
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
