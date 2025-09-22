package org.doubao.feed.service.feign.fallback;

import org.doubao.feed.service.feign.UserClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

	private static final Logger log =  LoggerFactory.getLogger(UserClientFallbackFactory.class);
	@Override
	public UserClient create(Throwable cause) {
		return new UserClient() {
			@Override
			public Result<List<Long>> getFollowees(Long userId) {
				log.error("[UserClient] getFollowees fallback, userId: {}, cause: {}", userId, cause.getMessage());
				return Result.success(Collections.emptyList());
			}

			@Override
			public Result<List<Long>> getFollowers(Long userId) {
				log.error("[UserClient] getFollowers fallback, userId: {}, cause: {}", userId, cause.getMessage());
				return Result.success(Collections.emptyList());
			}

			@Override
			public Result<Integer> getFollowerCount(Long userId) {
				log.error("[UserClient] getFollowerCount fallback, userId: {}, cause: {}", userId, cause.getMessage());
				return Result.success(0);
			}

			@Override
			public Result<Map<Long, Long>> getFollowerCounts(List<Long> userIds) {
				log.error("[UserClient] getFollowerCounts fallback, cause: {}", cause.getMessage());
				return Result.success(Collections.emptyMap());
			}

			@Override
			public Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds) {
				log.error("[UserClient] getUsersByIds fallback, userIds: {}, cause: {}", userIds, cause.getMessage());
				return Result.success(Collections.emptyList());
			}

			@Override
			public Result<Boolean> checkMutualFollow(Long userId, Long otherId) {
				log.error("[UserClient] checkMutualFollow fallback, userId: {}, otherId: {}, cause: {}",
						userId, otherId, cause.getMessage());
				return Result.success(false);
			}
		};
	}
}
