package org.doubao.topic.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.doubao.topic.service.mapper")
@EnableFeignClients(basePackages = "org.doubao.topic.service.feign")
public class TopicServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TopicServiceApplication.class, args);
    }

}