package org.doubao.view.count.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.view.count.service.dto.ViewRecordDTO;
import org.doubao.view.count.service.service.ViewCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/views")
public class ViewCountController {

	@Autowired
	private ViewCountService viewCountService;

	@PostMapping("/record")
	public Result<Boolean> recordView(@RequestBody ViewRecordDTO record) {
		boolean result = viewCountService.recordView(record);
		return Result.success(result);
	}

	@GetMapping("/count/{contentId}")
	public Result<Long> getViewCount(@PathVariable Long contentId) {
		Long count = viewCountService.getViewCount(contentId);
		return Result.success(count);
	}

	@PostMapping("/count/batch")
	public Result<Map<Long, Long>> batchGetViewCounts(@RequestBody List<Long> contentIds) {
		Map<Long, Long> counts = viewCountService.batchGetViewCounts(contentIds);
		return Result.success(counts);
	}


	@PostMapping("/count/sum")
	public Result<Map<LocalDate, Long>> batchSumDailyCounts(@RequestBody Map<String, Object> params) {
		return Result.success(viewCountService.batchSumDailyCounts(params));
	}
}