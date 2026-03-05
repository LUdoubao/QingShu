package org.doubao.user.service.feign.report;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "comment-service")
@MicroserviceMode
public interface CommentClient {
	@PostMapping("/comment/updateStatus")
	Result<Void> updateStatus(@RequestBody Map<String, String> request);
}
