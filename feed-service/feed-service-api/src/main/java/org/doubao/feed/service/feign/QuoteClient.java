package org.doubao.feed.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "quote-service")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface QuoteClient {

	/**
	 * 批量获取内容信息
	 */
	@PostMapping("/quote/batch")
	Result<List<Map<String, Object>>> batch(
			@RequestBody List<Long> ids);


}