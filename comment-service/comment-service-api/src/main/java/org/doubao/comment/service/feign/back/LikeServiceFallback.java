package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.dto.BatchLikeStatusRequest;
import org.doubao.comment.service.dto.BatchLikeStatusResponse;
import org.doubao.comment.service.dto.CommentLikeRequest;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.feign.LikeClient;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Feign降级处理
@Component
public class LikeServiceFallback implements LikeClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(LikeServiceFallback.class);

	@Override
	public Result<ToggleLikeResponse> toggleLike(CommentLikeRequest request) {
		LOGGER.error("toggleLike--点赞服务暂时不可用，触发降级处理");
		return Result.error("点赞服务暂时不可用，请稍后重试");
	}

	@Override
	public Result<BatchLikeStatusResponse> batchGetLikeStatus(BatchLikeStatusRequest request) {
		LOGGER.error("batchGetLikeStatus--点赞服务暂时不可用，触发降级处理");
		return Result.success(new BatchLikeStatusResponse());
	}

}