package org.doubao.user.server.report.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "comment-service")
public interface CommentClient {
	@PostMapping("/comment/updateStatus")
	Result<Void> updateStatus(@RequestBody Map<String, String> request);
}
