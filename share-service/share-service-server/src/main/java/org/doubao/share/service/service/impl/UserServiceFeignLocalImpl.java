package org.doubao.share.service.service.impl;

import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.share.service.feign.UserServiceFeign;
import org.doubao.user.service.service.core.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@MonolithMode
public class UserServiceFeignLocalImpl implements UserServiceFeign {

	@Resource
	private UserService userService;
	@Override
	public boolean checkUserExists(Map<String, String> request) {
		return userService.checkUserExists(request);
	}
}