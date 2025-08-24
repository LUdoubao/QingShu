package org.doubao.user.server.relation.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.vo.PageResult;
import org.doubao.user.server.relation.annotation.UserRateLimiter;
import org.doubao.user.server.relation.service.RelationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 关系功能控制器
 */
@RestController
@RequestMapping("/user/relations")
public class RelationController {
	@Resource
	private RelationService relationService;

	/**
	 * 关注用户
	 */
	@PostMapping("/follow")
	@UserRateLimiter(key = "follow", count = 10, period = 60) // 限流：1分钟最多10次
	public Result<Void> follow(
			@RequestParam Long targetUserId
	) {
		relationService.follow(targetUserId);
		return Result.success();
	}

	/**
	 * 取消关注
	 */
	@PostMapping("/unfollow")
	@UserRateLimiter(key = "unfollow", count = 10, period = 60)
	public Result<Void> unfollow(
			@RequestParam Long targetUserId
	) {
		relationService.unfollow(targetUserId);
		return Result.success();
	}

	/**
	 * 批量关注
	 */
	@PostMapping("/batch-follow")
	@UserRateLimiter(key = "batch_follow", count = 5, period = 60) // 批量操作限流更严格
	public Result<Void> batchFollow(
			@RequestBody List<Long> targetUserIds
	) {
		relationService.batchFollow(targetUserIds);
		return Result.success();
	}
	/**
	 * 分页查询粉丝列表
	 * @param targetUserId 目标用户ID（被关注者）
	 * @param page 页码（默认1）
	 * @param size 每页条数（默认20）
	 * @return 分页粉丝ID列表
	 */
	@GetMapping("/followers")
	public Result<PageResult<UserInfo>> getFollowers(
			@RequestParam Long targetUserId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		PageResult<UserInfo> result = relationService.getFollowers(targetUserId, page, size);
		return Result.success(result);
	}

	/**
	 * 分页查询关注列表
	 * @param userId 目标用户ID（关注者）
	 * @param page 页码（默认1）
	 * @param size 每页条数（默认20）
	 * @return 分页关注ID列表
	 */
	@GetMapping("/following")
	public Result<PageResult<UserInfo>> getFollowing(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		PageResult<UserInfo> result = relationService.getFollowing(userId, page, size);
		return Result.success(result);
	}

	/**
	 * 获取用户的粉丝数和关注数
	 * @param userId 目标用户ID
	 * @return 包含粉丝数和关注数的Map
	 */
	@GetMapping("/counts")
	public Result<Map<String, Integer>> getRelationCounts(
			@RequestParam Long userId
	) {
		Map<String, Integer> counts = relationService.getRelationCounts(userId);
		return Result.success(counts);
	}

	/**
	 * 获取用户的粉丝数
	 * @param userId 目标用户ID
	 * @return 粉丝数
	 */
	@GetMapping("/follower-count")
	public Result<Integer> getFollowerCount(
			@RequestParam Long userId
	) {
		int count = relationService.getFollowerCount(userId);
		return Result.success(count);
	}

	/**
	 * 获取用户的关注数
	 * @param userId 目标用户ID
	 * @return 关注数
	 */
	@GetMapping("/following-count")
	public Result<Integer> getFollowingCount(
			@RequestParam Long userId
	) {
		int count = relationService.getFollowingCount(userId);
		return Result.success(count);
	}

	@PostMapping("/isFollow")
	public Result<Map<Long, Boolean>> isFollow(
			@RequestParam Long currentUserId,
			@RequestBody Set<Long> userIds
	) {
		Map<Long, Boolean> result = relationService.isFollow(currentUserId, userIds);
		return Result.success(result);
	}

	@GetMapping("/existsFollowRelation")
	public Result<Boolean> existsFollowRelation(
			@RequestParam Long targetUserId
	) {
		boolean result = relationService.existsFollowRelation(targetUserId);
		return Result.success(result);
	}
}
