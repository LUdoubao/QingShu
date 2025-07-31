package org.doubao.comment.service.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Set;

@FeignClient(name = "user-service")
public interface UserClient {
	@PostMapping("/user/listByIds")
	Result<List<UserInfo>> getUsersByIds(Set<Long> userIds);
}
