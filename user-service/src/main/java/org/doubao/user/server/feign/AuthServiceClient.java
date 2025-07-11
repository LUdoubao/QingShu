package org.doubao.user.server.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

// AuthServiceClient.java
@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {

	@PostMapping("/login")
	Result<UserInfo> login(@RequestBody UserInfo userInfo);
}