package org.doubao.quote.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "comment-service")
public interface CommentClient {
	@PostMapping("/comment/count/batch")
	Result<Map<Long, Long>> batchGetCounts(@RequestBody List<Long> contentIds);

	@PostMapping("/comment/count/sum")
	Result<Map<LocalDate, Long>> batchSumDailyCounts(@RequestBody Map<String, Object> params);
}
