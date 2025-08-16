package org.doubao.dialog.service.feign;

import org.doubao.dialog.service.config.FeignErrorDecoderConfig;
import org.doubao.dialog.service.dto.AIRequest;
import org.doubao.dialog.service.dto.AIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI服务Feign客户端
 */
@FeignClient(name = "ai-service", fallback = AIServiceFallback.class, configuration = FeignErrorDecoderConfig.class)
public interface AIServiceClient {

	/**
	 * 调用AI服务生成回复
	 * @param request AI请求参数
	 * @return AI回复结果
	 */
	@PostMapping("/ai/generate/reply")
	AIResponse generateReply(@RequestBody AIRequest request);

	/**
	 * 调用AI服务生成标题
	 * @param content 输入内容
	 * @return 标题结果
	 */
	@PostMapping("/ai/generate/title")
	String generateTitle(String content);
}