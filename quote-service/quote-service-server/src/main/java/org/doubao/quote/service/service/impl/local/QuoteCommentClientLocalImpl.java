package org.doubao.quote.service.service.impl.local;

import org.doubao.comment.service.service.CommentService;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.feign.CommentClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@MonolithMode
public class QuoteCommentClientLocalImpl implements CommentClient {

	@Resource
	private CommentService commentService;

	@Override
	public Result<Map<Long, Long>> batchGetCounts(List<Long> contentIds) {
		try {
			Map<Long, Long> counts = commentService.batchCounts(contentIds);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("批量获取评论数失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Map<LocalDate, Long>> batchSumDailyCounts(Map<String, Object> params) {
		try {
			Map<LocalDate, Long> counts = commentService.batchSumDailyCounts(params);
			return Result.success(counts);
		} catch (Exception e) {
			return Result.error("批量汇总每日评论数失败: " + e.getMessage());
		}
	}
}