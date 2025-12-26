package org.doubao.feed.service.service.impl;

import org.doubao.feed.service.feign.UserClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.user.service.service.core.UserService;
import org.doubao.user.service.service.relation.RelationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class FeedUserClientLocalImpl implements UserClient {

	@Resource
	private UserService userService;
	
	@Resource
	private RelationService relationService;

	@Override
	public Result<List<Long>> getFollowees(Long targetUserId) {
		try {
			List<Long> followees = relationService.allFollows(targetUserId);
			return Result.success(followees);
		} catch (Exception e) {
			return Result.error("获取用户关注列表失败: " + e.getMessage());
		}
	}

	@Override
	public Result<List<Long>> getFollowers(Long targetUserId) {
		try {
			List<Long> followers = relationService.allFollowers(targetUserId);
			return Result.success(followers);
		} catch (Exception e) {
			return Result.error("获取用户粉丝列表失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Integer> getFollowerCount(Long userId) {
		try {
			int count = relationService.getFollowerCount(userId);
			return Result.success(count);
		} catch (Exception e) {
			return Result.error("获取用户粉丝数失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Map<Long, Long>> getFollowerCounts(List<Long> userIds) {
		try {
			Map<Long, Long> counts = relationService.getFollowerCounts(userIds);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("批量获取用户粉丝数失败: " + e.getMessage());
		}
	}

	@Override
	public Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds) {
		try {
			List<UserInfoDes> users = userService.usersByIds(userIds);
			return Result.success(users);
		} catch (Exception e) {
			return Result.error("批量获取用户信息失败: " + e.getMessage());
		}
	}
}