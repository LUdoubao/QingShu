package org.doubao.quote.service.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "view-count-service")
public interface ViewCountClient {
	@PostMapping("/views/count/batch")
	Result<Map<Long, Long>> batchGetViewCounts(@RequestBody List<Long> contentIds);
}
