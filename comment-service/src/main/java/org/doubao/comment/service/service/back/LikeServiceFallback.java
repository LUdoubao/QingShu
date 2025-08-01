package org.doubao.comment.service.service.back;

import org.doubao.comment.service.dto.BatchLikeStatusRequest;
import org.doubao.comment.service.dto.BatchLikeStatusResponse;
import org.doubao.comment.service.dto.CommentLikeRequest;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.feign.LikeClient;
import org.doubao.mall.common.entity.Result;
import org.springframework.stereotype.Component;

import java.util.Map;

// Feign降级处理
@Component
public class LikeServiceFallback implements LikeClient {
	@Override
	public Result<ToggleLikeResponse> toggleLike(CommentLikeRequest request) {
		return Result.error("点赞服务暂时不可用，请稍后重试");
	}

	@Override
	public Result<BatchLikeStatusResponse> batchGetLikeStatus(BatchLikeStatusRequest request) {
		return Result.error("点赞服务暂时不可用，请稍后重试");
	}

}