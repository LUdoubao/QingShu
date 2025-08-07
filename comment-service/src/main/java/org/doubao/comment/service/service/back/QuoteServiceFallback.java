package org.doubao.comment.service.service.back;

import org.doubao.comment.service.dto.BatchLikeStatusRequest;
import org.doubao.comment.service.dto.BatchLikeStatusResponse;
import org.doubao.comment.service.dto.CommentLikeRequest;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.feign.LikeClient;
import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.entity.Result;
import org.springframework.stereotype.Component;

// Feign降级处理
@Component
public class QuoteServiceFallback implements QuoteClient {
	@Override
	public Result<QuoteVo> detail(Long id) {
		return Result.error("引文服务暂时不可用，请稍后重试");
	}
}