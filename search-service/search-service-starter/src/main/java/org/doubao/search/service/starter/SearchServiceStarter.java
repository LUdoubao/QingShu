package org.doubao.search.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "org.doubao.search.service")
@EnableFeignClients(basePackages = "org.doubao.search.service.feign")
@EnableDiscoveryClient
@MapperScan("org.doubao.search.service.mapper")
public class SearchServiceStarter {
    
    public static void main(String[] args) {
        SpringApplication.run(SearchServiceStarter.class, args);
    }
}