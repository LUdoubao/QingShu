package org.doubao.share.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceFeign {

	@PostMapping("/user/inner/exists")
	boolean checkUserExists(@RequestBody Map<String, String> request);
}