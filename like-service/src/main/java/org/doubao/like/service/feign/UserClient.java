package org.doubao.like.service.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "user-service")
public interface UserClient {
	@PostMapping("/user/listByIds")
	Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds);

	@PostMapping("/user/relations/isFollow")
	Result<Map<Long, Boolean>> isFollow(@RequestParam("currentUserId") Long currentUserId, Set<Long> userIds);

	@GetMapping("/user/privacy/work")
	Result<Boolean> checkWorkPermission(@RequestParam("targetUserId") Long targetUserId,
										@RequestParam("currentUserId") Long currentUserId);
}
