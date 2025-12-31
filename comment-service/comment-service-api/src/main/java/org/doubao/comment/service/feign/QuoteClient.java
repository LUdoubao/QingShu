package org.doubao.comment.service.feign;

import org.doubao.comment.service.config.FeignErrorDecoderConfig;
import org.doubao.comment.service.feign.back.QuoteClientFallback;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 引文服务Feign客户端
 * <p>
 * 在微服务模式下（service.run-mode=microservice），用于调用引文服务的接口
 * 提供获取引文详情的功能
 * 配置了错误解码器和降级处理机制
 */
@FeignClient(name = "quote-service", fallbackFactory = QuoteClientFallback.class,
		configuration = FeignErrorDecoderConfig.class)
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface QuoteClient {
	
	/**
	 * 获取引文详情
	 * <p>
	 * 调用引文服务获取指定ID的引文详情信息
	 * 
	 * @param id 引文ID
	 * @return 包含引文详情的响应对象
	 */
	@GetMapping("/public/quote/detail/{id}")
	Result<QuoteVo> detail(@PathVariable("id")  Long id);
}