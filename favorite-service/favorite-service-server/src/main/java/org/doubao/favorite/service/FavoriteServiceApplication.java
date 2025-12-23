package org.doubao.favorite.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.doubao.favorite.service.mapper")
@EnableFeignClients(basePackages = "org.doubao.favorite.service.feign")
public class FavoriteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FavoriteServiceApplication.class, args);
    }

}