package org.doubao.share.service.feign;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "quote-service")
@MicroserviceMode
public interface QuoteServiceFeign {

	@GetMapping("/quote/{quoteId}/type")
	String getQuoteType(@PathVariable("quoteId") String quoteId);

	@PostMapping("/quote/inner/exists")
	boolean checkQuoteExists(@RequestBody Map<String, String> request);
}