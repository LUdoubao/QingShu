package org.doubao.user.server.core.service.back;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.user.server.core.feign.AuthServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Feign降级处理
@Component
public class AuthServiceFallback implements AuthServiceClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceFallback.class);

	@Override
	public Result<UserInfo> login(UserInfo userInfo) {
		LOGGER.error("auth登录服务login暂时不可用，触发降级处理");
		return Result.error("登录服务暂时不可用，请稍后重试");
	}

	@Override
	public Result<Long> getTokenExpiration(String token) {
		LOGGER.error("auth服务暂时getTokenExpiration不可用，触发降级处理");
		return Result.error("登录服务暂时不可用，请稍后重试");
	}
}