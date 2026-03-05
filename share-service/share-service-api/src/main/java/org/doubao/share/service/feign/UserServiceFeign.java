package org.doubao.share.service.feign;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "user-service")
@MicroserviceMode
public interface UserServiceFeign {

	@PostMapping("/user/inner/exists")
	boolean checkUserExists(@RequestBody Map<String, String> request);
}