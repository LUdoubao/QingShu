package org.doubao.favorite.service.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "user-service")
public interface UserClient {
	@PostMapping("/user/listByIds")
	Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds);
}
