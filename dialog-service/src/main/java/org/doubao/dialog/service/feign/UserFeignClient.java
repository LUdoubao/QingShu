package org.doubao.dialog.service.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;
@FeignClient(name = "user-service")
public interface UserFeignClient {
	@PostMapping("/user/listByIds")
	Result<List<UserInfo>> getUsersByIds(Set<Long> userIds);

	@GetMapping("/user/privacy/chat")
	Result<Boolean> checkChatPermission(@RequestParam("targetUserId") Long targetUserId,
										@RequestParam("currentUserId") Long currentUserId);
}