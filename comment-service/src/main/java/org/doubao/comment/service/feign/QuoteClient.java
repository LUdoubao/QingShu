package org.doubao.comment.service.feign;

import org.doubao.comment.service.config.FeignErrorDecoderConfig;
import org.doubao.comment.service.service.back.LikeServiceFallback;
import org.doubao.comment.service.service.back.QuoteServiceFallback;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "quote-service", fallback = QuoteServiceFallback.class, configuration = FeignErrorDecoderConfig.class)
public interface QuoteClient {
	@GetMapping("/quote/detail/{id}")
	Result<QuoteVo> detail(@PathVariable("id")  Long id);
}
