package org.doubao.feed.service.feign.fallback;

import org.doubao.feed.service.feign.LikeClient;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LikeClientFallbackFactory implements FallbackFactory<LikeClient> {
	private static final Logger log =  LoggerFactory.getLogger(LikeClientFallbackFactory.class);

	public LikeClient create(Throwable cause) {
		return new LikeClient() {
			@Override
			public Result<Map<String, Object>> likeAction(Long userId, String targetType, Long targetId, Integer action) {

				log.error("[LikeClient] likeAction fallback, userId: {}, targetType: {}, targetId: {}, action: {}, cause: {}",
						userId, targetType, targetId, action, cause.getMessage());
				return Result.success(new HashMap<>());

			}

			@Override
			public Result<Map<String, Boolean>> getLikeStatusBatch(Long userId, String targetType, String targetIds) {
				log.error("[LikeClient] getLikeStatusBatch fallback, userId: {}, targetType: {}, targetIds: {}, cause: {}",
						userId, targetType, targetIds, cause.getMessage());
				return Result.success(new HashMap<>());
			}
		};
	}
}
