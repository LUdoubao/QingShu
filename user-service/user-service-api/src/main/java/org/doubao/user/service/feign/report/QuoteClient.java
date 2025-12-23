package org.doubao.user.service.feign.report;


import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "quote-service")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface QuoteClient {
	@PostMapping("/quote/updateStatus")
	Result<Void> updateStatus(@RequestBody Map<String, String> request);
}