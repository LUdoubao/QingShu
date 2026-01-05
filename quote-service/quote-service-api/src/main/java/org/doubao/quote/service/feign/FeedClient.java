package org.doubao.quote.service.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.UpdateValidDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "feed-service")
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface FeedClient {
	@PostMapping("/update-status")
	Result<Void> updateFeedStatus(
			@RequestBody UpdateValidDto updateValidDto);
}