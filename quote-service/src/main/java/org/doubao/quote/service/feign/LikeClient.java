package org.doubao.quote.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "like-service")
public interface LikeClient {
	@PostMapping("/like/count/batch")
	Result<Map<Long, Long>> batchGetCounts(@RequestBody List<Long> contentIds);
}
