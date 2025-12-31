package org.doubao.comment.service.feign;

import org.doubao.comment.service.config.FeignErrorDecoderConfig;
import org.doubao.comment.service.feign.back.QuoteClientFallback;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "quote-service", fallbackFactory = QuoteClientFallback.class,
		configuration = FeignErrorDecoderConfig.class)
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface QuoteClient {
	@GetMapping("/public/quote/detail/{id}")
	Result<QuoteVo> detail(@PathVariable("id")  Long id);
}