package org.doubao.comment.service.feign;

import org.doubao.comment.service.config.FeignErrorDecoderConfig;
import org.doubao.comment.service.feign.back.UserServiceFallback;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Set;

/**
 * 用户服务Feign客户端
 * <p>
 * 在微服务模式下（service.run-mode=microservice），用于调用用户服务的接口
 * 提供根据用户ID列表批量获取用户信息的功能
 * 配置了错误解码器和降级处理机制
 */
@FeignClient(name = "user-service", fallbackFactory = UserServiceFallback.class,
		configuration = FeignErrorDecoderConfig.class)
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface UserClient {
	
	/**
	 * 根据用户ID列表获取用户信息
	 * <p>
	 * 调用用户服务批量获取指定ID列表的用户详细信息
	 * 
	 * @param userIds 用户ID集合
	 * @return 包含用户信息列表的响应对象
	 */
	@PostMapping("/user/listByIds")
	Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds);
}