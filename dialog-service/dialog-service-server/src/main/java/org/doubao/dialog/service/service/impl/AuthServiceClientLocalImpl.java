package org.doubao.dialog.service.service.impl;

import org.doubao.auth.service.service.AuthService;
import org.doubao.dialog.service.feign.AuthServiceClient;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@MonolithMode
public class AuthServiceClientLocalImpl implements AuthServiceClient {

	@Resource
	private AuthService authService;
	@Override
	public Result<String> webSocket(String token) {
		return authService.webSocket(token);
	}
}