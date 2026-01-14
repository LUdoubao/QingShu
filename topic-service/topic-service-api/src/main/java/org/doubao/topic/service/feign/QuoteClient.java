package org.doubao.topic.service.feign;

import org.doubao.mall.common.entity.Result;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 文案服务Feign客户端
 * 用于与文案服务进行远程通信，仅在微服务模式下启用
 */
@FeignClient(name = "quote-service")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface QuoteClient {

}