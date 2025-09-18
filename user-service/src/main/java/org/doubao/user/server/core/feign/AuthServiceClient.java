package org.doubao.user.server.core.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.server.core.config.FeignErrorDecoderConfig;
import org.doubao.user.server.core.service.back.AuthServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// AuthServiceClient.java
@FeignClient(name = "auth-service", path = "/auth", fallback = AuthServiceFallback.class, configuration = FeignErrorDecoderConfig.class)
public interface AuthServiceClient {

	@PostMapping("/login")
	Result<UserLoginVo> login(@RequestBody UserLoginVo userInfo);

	@PostMapping("/token/expiration")
	Result<Long> getTokenExpiration(@RequestBody String token);
}