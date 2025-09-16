package org.doubao.search.service.db.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "quote-service", path = "/quote")
public interface QuoteClient {
	@GetMapping("/search/suggestion")
	Result<Map<String, String>> getSearchSuggestions(@RequestParam("keyword") String keyword);

	@GetMapping("/search/type")
	Result<Map<String, Object>> searchQuotes(@RequestParam("keyword") String keyword,
								@RequestParam("page") int page, @RequestParam("size") int size,
								@RequestParam("type") String type);
}