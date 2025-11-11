package org.doubao.quote.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@FeignClient(name = "favorite-service")
public interface FavoriteClient {
	@PostMapping("/favorite/quote/count")
	Result<Map<Long, Long>> countQuotes(@RequestBody List<Long> contentIds);

	@PostMapping("/favorite/quote/sum")
	Result<Map<LocalDate, Long>> batchSumDailyCounts(@RequestBody Map<String, Object> params);
}
