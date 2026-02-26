package org.doubao.user.service.feign.core;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.service.config.UserFeignErrorDecoderConfig;
import org.doubao.user.service.feign.back.AuthServiceFallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// AuthServiceClient.java
@FeignClient(name = "auth-service", path = "/auth", fallback = AuthServiceFallback.class, configuration = UserFeignErrorDecoderConfig.class)
@MicroserviceMode
public interface AuthServiceClient {

	@PostMapping("/login")
	Result<UserLoginVo> login(@RequestBody UserLoginVo userInfo);

	@PostMapping("/token/expiration")
	Result<Long> getTokenExpiration(@RequestBody String token);
}