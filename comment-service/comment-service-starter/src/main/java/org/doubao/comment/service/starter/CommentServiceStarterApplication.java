package org.doubao.comment.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 评论服务启动类
 * <p>
 * 该类是评论服务的Spring Boot启动入口，配置了服务发现、Feign客户端、
 * Mapper扫描和组件扫描等必要的微服务配置
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("org.doubao.comment.service.mapper")
@ComponentScan(basePackages = "org.doubao.comment.service")
public class CommentServiceStarterApplication {

	/**
	 * 应用程序入口方法
	 * <p>
	 * 启动Spring Boot应用程序，初始化Spring上下文和所有配置的组件
	 * 
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(CommentServiceStarterApplication.class, args);
	}

}