package org.doubao.topic.service.feign;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.dto.TopicContentDto;
import org.doubao.mall.common.entity.Result;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 文案服务Feign客户端
 * 用于与文案服务进行远程通信，仅在微服务模式下启用
 */
@FeignClient(name = "quote-service")
@MicroserviceMode
public interface QuoteClient {
	/**
	 * 批量获取文案基础信息
	 * @param ids 文案ID列表
	 * @return 文案信息列表
	 */
	@PostMapping("/topic_batch")
	Result<List<Map<String, Object>>> topicBatch(@RequestBody TopicContentDto topicContentDto);
}