package org.doubao.dialog.service.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.doubao.dialog.service.config.FeignErrorDecoderConfig;
import org.doubao.dialog.service.dto.AIRequest;
import org.doubao.dialog.service.dto.AIResponse;
import org.doubao.dialog.service.dto.MessageRequest;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.CompletableFuture;

/**
 * AI服务Feign客户端, , fallback = AIServiceFallback.classconfiguration = FeignErrorDecoderConfig.class
 */
@FeignClient(name = "ai-service")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface AIServiceClient {

	/**
	 * 调用AI服务生成回复
	 * @param request AI请求参数
	 * @return AI回复结果
	 */
	@PostMapping("/ai/generate/reply")
	Result<AIResponse> generateReply(@RequestBody AIRequest request);
}