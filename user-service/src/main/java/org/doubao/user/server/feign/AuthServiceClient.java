package org.doubao.user.server.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.user.server.config.FeignErrorDecoderConfig;
import org.doubao.user.server.service.back.AuthServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

// AuthServiceClient.java
@FeignClient(name = "auth-service", path = "/auth", fallback = AuthServiceFallback.class, configuration = FeignErrorDecoderConfig.class)
public interface AuthServiceClient {

	@PostMapping("/login")
	Result<UserInfo> login(@RequestBody UserInfo userInfo);

	@PostMapping("/token/expiration")
	Result<Long> getTokenExpiration(@RequestBody String token);
}