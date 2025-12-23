package org.doubao.quote.service.service.impl;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.feign.ViewCountClient;
import org.doubao.view.count.service.service.ViewCountService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class ViewCountClientLocalImpl implements ViewCountClient {

	@Resource
	private ViewCountService viewCountService;

	@Override
	public Result<Map<Long, Long>> batchGetViewCounts(List<Long> contentIds) {
		try {
			Map<Long, Long> counts = viewCountService.batchGetViewCounts(contentIds);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("获取浏览量失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Map<LocalDate, Long>> batchSumDailyCounts(Map<String, Object> params) {
		try {
			Map<LocalDate, Long> counts = viewCountService.batchSumDailyCounts(params);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("统计每日浏览量失败: " + e.getMessage());
		}
	}
}