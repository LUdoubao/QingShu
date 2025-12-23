package org.doubao.dialog.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// AuthServiceClient.java
@FeignClient(name = "auth-service", path = "/auth")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface AuthServiceClient {
	@GetMapping("/token/webSocket")
	Result<String> webSocket(@RequestParam(value = "token") String token);
}