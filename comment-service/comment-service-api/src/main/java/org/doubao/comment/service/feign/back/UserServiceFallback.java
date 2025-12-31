package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.feign.UserClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public class UserServiceFallback implements FallbackFactory<UserClient> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceFallback.class);

	@Override
	public UserClient create(Throwable cause) {
		LOGGER.error("User service unavailable, cause: {}", cause.getMessage(), cause);
		return new UserClient() {
			@Override
			public Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds) {
				LOGGER.error("User service getUsersByIds method failed for userIds: {}, cause: {}", userIds, cause.getMessage());
				return Result.success(Collections.emptyList());
			}
		};
	}
}