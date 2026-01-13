package org.doubao.like.service.service.impl.local;

import org.doubao.like.service.feign.UserClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.user.service.service.core.UserService;
import org.doubao.user.service.service.relation.RelationService;
import org.doubao.user.service.service.relation.UserPrivacyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class LikeUserClientLocalImpl implements UserClient {

	@Resource
	private UserService userService;
	
	@Resource
	private RelationService relationService;
	
	@Resource
	private UserPrivacyService userPrivacyService;

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
	public Result<Map<Long, Boolean>> isFollow(Long currentUserId, Set<Long> userIds) {
		try {
			Map<Long, Boolean> followMap = relationService.isFollow(currentUserId, userIds);
			return Result.success(followMap);
		} catch (Exception e) {
			return Result.error("检查关注状态失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Boolean> checkWorkPermission(Long targetUserId, Long currentUserId) {
		try {
			boolean permission = userPrivacyService.checkSeePermission(targetUserId, currentUserId, 2); // 2表示作品权限
			return Result.success(permission);
		} catch (Exception e) {
			return Result.error("检查作品权限失败: " + e.getMessage());
		}
	}
}