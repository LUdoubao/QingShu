package org.doubao.dialog.service.service.impl;

import org.doubao.dialog.service.feign.UserFeignClient;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.user.service.service.core.UserService;
import org.doubao.user.service.service.relation.UserBlockService;
import org.doubao.user.service.service.relation.UserPrivacyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@MonolithMode
public class UserFeignClientLocalImpl implements UserFeignClient {

	@Resource
	private UserService userService;
	
	@Resource
	private UserPrivacyService userPrivacyService;
	
	@Resource
	private UserBlockService userBlockService;

	@Override
	public Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds) {
		try {
			List<UserInfoDes> users = userService.usersByIds(userIds);
			return Result.success(users);
		} catch (Exception e) {
			return Result.error("获取用户信息失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Boolean> checkChatPermission(Long targetUserId, Long currentUserId) {
		try {
			boolean permission = userPrivacyService.checkSeePermission(targetUserId, currentUserId, 3); // 3表示聊天权限
			return Result.success(permission);
		} catch (Exception e) {
			return Result.error("检查聊天权限失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Map<Long, Boolean>> checkBatch(Long userId, List<Long> targetUserIds) {
		try {
			Map<Long, Boolean> result = userBlockService.checkBatch(userId, targetUserIds);
			return Result.success(result);
		} catch (Exception e) {
			return Result.error("批量检查黑名单失败: " + e.getMessage());
		}
	}
}