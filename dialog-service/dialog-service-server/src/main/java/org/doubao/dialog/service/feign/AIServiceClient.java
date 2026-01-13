package org.doubao.dialog.service.feign;

import org.doubao.dialog.service.dto.AIRequest;
import org.doubao.dialog.service.dto.AIResponse;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI服务Feign客户端, , fallback = AIServiceFallback.classconfiguration = UserFeignErrorDecoderConfig.class
 */
@FeignClient(name = "ai-service")
public interface AIServiceClient {

	/**
	 * 调用AI服务生成回复
	 * @param request AI请求参数
	 * @return AI回复结果
	 */
	@PostMapping("/ai/generate/reply")
	Result<AIResponse> generateReply(@RequestBody AIRequest request);
}