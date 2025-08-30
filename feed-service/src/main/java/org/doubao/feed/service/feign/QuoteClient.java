package org.doubao.feed.service.feign;

import org.doubao.feed.service.feign.fallback.QuoteClientFallbackFactory;
import org.doubao.feed.service.model.dto.ContentDTO;
import org.doubao.feed.service.model.dto.ContentStatsDTO;
import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "quote-service", fallbackFactory = QuoteClientFallbackFactory.class)
public interface QuoteClient {

	/**
	 * 批量获取内容信息
	 */
	@PostMapping("/quote/batch")
	Result<List<Map<String, Object>>> batch(
			@RequestBody List<Long> ids);

	/**
	 * 获取内容统计信息
	 */
	@GetMapping("/quote/stats/{type}/{id}")
	Result<ContentStatsDTO> getContentStats(
			@PathVariable("type") String type,
			@PathVariable("id") Long id);

	/**
	 * 获取内容标签
	 */
	@GetMapping("/quote/tags/{id}")
	Result<List<String>> getContentTags(@PathVariable("id") Long id);




}

