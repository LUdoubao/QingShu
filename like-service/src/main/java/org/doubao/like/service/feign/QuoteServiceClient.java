package org.doubao.like.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

// QuoteServiceClient.java
@FeignClient(name = "quote-service", path = "/quote")
public interface QuoteServiceClient {

	/**
	 * 批量获取文案基础信息
	 * @param ids 文案ID列表
	 * @return 文案信息列表
	 */
	@PostMapping("/batch")
	Result<List<Map<String, Object>>> getQuotesByIds(@RequestBody List<Long> ids);
}