package org.doubao.dialog.service.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;
@FeignClient(name = "user-service")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface UserFeignClient {
	@PostMapping("/user/listByIds")
	Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds);

	@GetMapping("/user/privacy/chat")
	Result<Boolean> checkChatPermission(@RequestParam("targetUserId") Long targetUserId,
										@RequestParam("currentUserId") Long currentUserId);
	@PostMapping("/user/block/checkBatch")
	Result<Map<Long, Boolean>> checkBatch(@RequestParam("userId") Long userId, @RequestBody List<Long> targetUserIds);
}