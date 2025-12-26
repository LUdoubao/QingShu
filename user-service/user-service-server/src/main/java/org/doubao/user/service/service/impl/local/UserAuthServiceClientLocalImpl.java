package org.doubao.user.service.service.impl.local;

import org.doubao.auth.service.service.AuthService;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.service.feign.core.AuthServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class UserAuthServiceClientLocalImpl implements AuthServiceClient {
	@Resource
	private AuthService authService;
	@Override
	public Result<UserLoginVo> login(UserLoginVo userInfo) {
		return authService.login(userInfo);
	}

	@Override
	public Result<Long> getTokenExpiration(String token) {
		return authService.getTokenExpiration(token);
	}
}
