package org.doubao.user.service.feign.back;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.service.feign.core.AuthServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Feign降级处理
@Component
@MicroserviceMode
public class AuthServiceFallback implements AuthServiceClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceFallback.class);

	@Override
	public Result<UserLoginVo> login(UserLoginVo userInfo) {
		LOGGER.error("auth登录服务login暂时不可用，触发降级处理");
		return Result.error("登录服务暂时不可用，请稍后重试");
	}

	@Override
	public Result<Long> getTokenExpiration(String token) {
		LOGGER.error("auth服务暂时getTokenExpiration不可用，触发降级处理");
		return Result.error("登录服务暂时不可用，请稍后重试");
	}
}