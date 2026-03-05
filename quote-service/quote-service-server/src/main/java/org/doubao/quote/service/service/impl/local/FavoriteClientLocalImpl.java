package org.doubao.quote.service.service.impl.local;

import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.feign.FavoriteClient;
import org.doubao.favorite.service.service.FavoriteService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@MonolithMode
public class FavoriteClientLocalImpl implements FavoriteClient {

	@Resource
	@Lazy
	private FavoriteService favoriteService;

	@Override
	public Result<Map<Long, Long>> countQuotes(List<Long> contentIds) {
		try {
			Map<Long, Long> counts = favoriteService.countQuotes(contentIds);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("统计收藏数失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Map<LocalDate, Long>> batchSumDailyCounts(Map<String, Object> params) {
		try {
			Map<LocalDate, Long> counts = favoriteService.batchSumDailyCounts(params);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("统计每日收藏数失败: " + e.getMessage());
		}
	}
}