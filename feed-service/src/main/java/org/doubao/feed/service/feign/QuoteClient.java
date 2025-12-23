package org.doubao.feed.service.feign;

import org.doubao.feed.service.feign.fallback.QuoteClientFallbackFactory;
import org.doubao.feed.service.model.dto.ContentDTO;
import org.doubao.feed.service.model.dto.ContentStatsDTO;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "quote-service", fallbackFactory = QuoteClientFallbackFactory.class)
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface QuoteClient {

	/**
	 * 批量获取内容信息
	 */
	@PostMapping("/quote/batch")
	Result<List<Map<String, Object>>> batch(
			@RequestBody List<Long> ids);


}