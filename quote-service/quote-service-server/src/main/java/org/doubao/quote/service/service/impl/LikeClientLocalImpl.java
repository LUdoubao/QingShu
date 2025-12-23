package org.doubao.quote.service.service.impl;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.feign.LikeClient;
import org.doubao.like.service.service.LikeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class LikeClientLocalImpl implements LikeClient {

	@Resource
	private LikeService likeService;

	@Override
	public Result<Map<Long, Long>> batchGetCounts(List<Long> contentIds) {
		try {
			Map<Long, Long> counts = likeService.batchCounts(contentIds);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("统计点赞数失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Map<LocalDate, Long>> batchSumDailyCounts(Map<String, Object> params) {
		try {
			Map<LocalDate, Long> counts = likeService.batchSumDailyCounts(params);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("统计每日点赞数失败: " + e.getMessage());
		}
	}
}