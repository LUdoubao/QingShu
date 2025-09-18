package org.doubao.dialog.service.feign;

import io.swagger.annotations.ApiOperation;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;
@FeignClient(name = "user-service")
public interface UserFeignClient {
	@PostMapping("/user/listByIds")
	Result<List<UserInfo>> getUsersByIds(Set<Long> userIds);

	@GetMapping("/user/privacy/chat")
	Result<Boolean> checkChatPermission(@RequestParam("targetUserId") Long targetUserId,
										@RequestParam("currentUserId") Long currentUserId);
	@PostMapping("/user/block/checkBatch")
	Result<Map<Long, Boolean>> checkBatch(@RequestParam("userId") Long userId, @RequestBody List<Long> targetUserIds);
}