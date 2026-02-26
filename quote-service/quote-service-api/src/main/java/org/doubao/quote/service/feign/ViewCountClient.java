package org.doubao.quote.service.feign;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "view-count-service")
@MicroserviceMode
public interface ViewCountClient {
	@PostMapping("/views/count/batch")
	Result<Map<Long, Long>> batchGetViewCounts(@RequestBody List<Long> contentIds);

	@PostMapping("/views/count/sum")
	Result<Map<LocalDate, Long>> batchSumDailyCounts(@RequestBody Map<String, Object> params);
}