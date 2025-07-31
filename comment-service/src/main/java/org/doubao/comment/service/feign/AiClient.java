package org.doubao.comment.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ai-service")
public interface AiClient {
	@PostMapping("/v1/risk/check")
	int checkContent(@RequestParam String content);

	@PostMapping("/v1/nlp/analyze-tags")
	List<String> analyzeTags(@RequestParam String content);
}