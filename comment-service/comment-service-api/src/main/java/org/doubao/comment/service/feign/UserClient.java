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

@FeignClient(name = "user-service", fallbackFactory = UserServiceFallback.class,
		configuration = FeignErrorDecoderConfig.class)
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface UserClient {
	@PostMapping("/user/listByIds")
	Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds);
}