package org.doubao.favorite.service.service.impl.local;

import org.doubao.favorite.service.feign.UserClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.user.service.service.core.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class UserClientLocalImpl implements UserClient {

	@Resource
	private UserService userService;

	@Override
	public Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds) {
		try {
			List<UserInfoDes> users = userService.usersByIds(userIds);
			return Result.success(users);
		} catch (Exception e) {
			return Result.error("获取用户信息失败: " + e.getMessage());
		}
	}
}