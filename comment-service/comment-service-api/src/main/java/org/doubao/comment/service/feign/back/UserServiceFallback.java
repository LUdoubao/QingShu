package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.feign.UserClient;
import org.doubao.mall.common.condition.MicroserviceMode;
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

/**
 * 用户服务降级处理工厂
 * <p>
 * 在微服务模式下（service.run-mode=microservice），当用户服务不可用时，
 * 提供降级处理逻辑，确保评论服务的可用性
 */
@Component
@MicroserviceMode
public class UserServiceFallback implements FallbackFactory<UserClient> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceFallback.class);

	/**
	 * 创建用户服务降级实现
	 * <p>
	 * 当用户服务调用失败时，返回降级实现，提供默认的错误处理逻辑
	 * 
	 * @param cause 服务调用失败的原因
	 * @return 用户服务的降级实现
	 */
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