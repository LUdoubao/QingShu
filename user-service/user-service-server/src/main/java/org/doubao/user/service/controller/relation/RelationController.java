package org.doubao.user.service.controller.relation;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.vo.PageResult;
import org.doubao.user.service.annotation.UserRateLimiter;
import org.doubao.user.service.dto.relation.UserInfoDesFollow;
import org.doubao.user.service.service.relation.RelationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/user/relations")
public class RelationController {
	@Resource
	private RelationService relationService;

	/**
	 * 关注用户
	 */
	@PostMapping("/follow")
	@UserRateLimiter(key = "follow")
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
	@UserRateLimiter(key = "unfollow")
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
	@UserRateLimiter(key = "batch_follow", count = 5, period = 60)
	public Result<Void> batchFollow(
			@RequestBody List<Long> targetUserIds
	) {
		relationService.batchFollow(targetUserIds);
		return Result.success();
	}

	@GetMapping("/followers")
	public Result<PageResult<UserInfoDesFollow>> getFollowers(
			@RequestParam Long targetUserId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		PageResult<UserInfoDesFollow> result = relationService.getFollowers(targetUserId, page, size);
		return Result.success(result);
	}

	@GetMapping("/allFollowers")
	public Result<List<Long>> allFollowers(
			@RequestParam(name = "targetUserId") Long targetUserId
	) {
		List<Long> longList = relationService.allFollowers(targetUserId);
		return Result.success(longList);
	}


	@GetMapping("/following")
	public Result<PageResult<UserInfoDes>> getFollowing(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		PageResult<UserInfoDes> result = relationService.getFollowing(userId, page, size);
		return Result.success(result);
	}

	@GetMapping("/allFollows")
	public Result<List<Long>> allFollows(
			@RequestParam(name = "targetUserId") Long targetUserId
	) {
		return Result.success(relationService.allFollows(targetUserId));
	}


	@PostMapping("/follower-counts")
	public Result<Map<Long, Long>> getFollowerCounts(@RequestBody List<Long> userIds) {
		return Result.success(relationService.getFollowerCounts(userIds));
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


	@GetMapping("/follower-count")
	public Result<Integer> getFollowerCount(
			@RequestParam Long userId
	) {
		int count = relationService.getFollowerCount(userId);
		return Result.success(count);
	}


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
