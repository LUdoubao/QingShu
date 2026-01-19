package org.doubao.quote.service.feign;

import org.doubao.mall.common.dto.TopicBindDTO;
import org.doubao.mall.common.dto.TopicNameVo;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.UpdateValidDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "Topic-service")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface TopicClient {
	@PostMapping("/topicQuote/deleteQuoteBind")
	public Result<Boolean> deleteQuoteBind(@RequestBody List<Long> quoteIds);

	@PostMapping("/topicQuote/bind-quote")
	public Result<Boolean> bindQuoteToTopic(@RequestBody TopicBindDTO dto);

	@PostMapping("/update-bind-quote")
	public Result<Boolean> updateBindQuote(@RequestBody TopicBindDTO dto);

	@GetMapping("/getName/{id}")
	public Result<TopicNameVo> getNameById(@RequestParam("id") Long id);
}