package org.doubao.feed.service.feign;

import org.doubao.feed.service.feign.fallback.LikeClientFallbackFactory;
import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "like-service", fallbackFactory = LikeClientFallbackFactory.class)
public interface LikeClient {

	/**
	 * 点赞/取消点赞操作
	 */
	@PostMapping("/like/action")
	Result<Map<String, Object>> likeAction(
			@RequestParam("userId") Long userId,
			@RequestParam("targetType") String targetType,
			@RequestParam("targetId") Long targetId,
			@RequestParam("action") Integer action);

	/**
	 * 批量查询点赞状态
	 */
	@PostMapping("/like/status/batch")
	Result<Map<String, Boolean>> getLikeStatusBatch(
			@RequestParam("userId") Long userId,
			@RequestParam("targetType") String targetType,
			@RequestParam("targetIds") String targetIds);
}
