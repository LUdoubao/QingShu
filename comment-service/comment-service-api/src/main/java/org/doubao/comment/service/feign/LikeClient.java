package org.doubao.comment.service.feign;

import org.doubao.comment.service.config.FeignErrorDecoderConfig;
import org.doubao.comment.service.dto.BatchLikeStatusRequest;
import org.doubao.comment.service.dto.BatchLikeStatusResponse;
import org.doubao.comment.service.dto.CommentLikeRequest;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.feign.back.LikeServiceFallback;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


// 评论服务中定义Feign客户端，适配点赞服务接口
@FeignClient(name = "like-service", fallback = LikeServiceFallback.class, configuration = FeignErrorDecoderConfig.class)
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public interface LikeClient {

	/**
	 * 调用点赞服务的切换点赞状态接口
	 * 注意：参数需与点赞服务的ToggleLikeRequest字段对应，使用@RequestBody传递
	 */
	@PostMapping("/like/toggle")
	Result<ToggleLikeResponse> toggleLike(@RequestBody CommentLikeRequest request);


	@PostMapping("/like/status")
	Result<BatchLikeStatusResponse> batchGetLikeStatus(@RequestBody BatchLikeStatusRequest request);
}