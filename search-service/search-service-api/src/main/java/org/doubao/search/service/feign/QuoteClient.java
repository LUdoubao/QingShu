package org.doubao.search.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "quote-service", path = "/quote")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface QuoteClient {
	@GetMapping("/search/suggestion")
	Result<Map<String, String>> getSearchSuggestions(@RequestParam("keyword") String keyword);

	@GetMapping("/search/type")
	Result<Map<String, Object>> searchQuotes(@RequestParam("keyword") String keyword,
								@RequestParam("page") int page, @RequestParam("size") int size,
								@RequestParam("currentUserId") Long currentUserId,
								@RequestParam("type") String type);
}