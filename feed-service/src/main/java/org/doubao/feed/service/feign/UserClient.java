package org.doubao.feed.service.feign;

import org.doubao.feed.service.feign.fallback.UserClientFallbackFactory;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

	/**
	 * 获取用户关注列表
	 */
	@GetMapping("/user/relations/allFollows")
	Result<List<Long>> getFollowees(@RequestParam("targetUserId") Long targetUserId);

	/**
	 * 获取用户粉丝列表
	 */
	@GetMapping("/user/relations/allFollowers")
	Result<List<Long>> getFollowers(@RequestParam("targetUserId") Long targetUserId);

	/**
	 * 获取用户粉丝数
	 */
	@GetMapping("/user/relations/follower-count")
	Result<Integer> getFollowerCount(@RequestParam("userId") Long userId);

	/**
	 * 批量获取用户粉丝数
	 */
	@PostMapping("/user/relations/follower-counts")
	Result<Map<Long, Long>> getFollowerCounts(@RequestBody List<Long> userIds);

	/**
	 * 批量获取用户信息
	 */
	@PostMapping("/user/listByIds")
	Result<List<UserInfoDes>> getUsersByIds(@RequestBody Set<Long> userIds);

	/**
	 * 检查是否互相关注
	 */
	@GetMapping("/user/check-mutual/{userId}/{otherId}")
	Result<Boolean> checkMutualFollow(
			@PathVariable("userId") Long userId,
			@PathVariable("otherId") Long otherId);


}
