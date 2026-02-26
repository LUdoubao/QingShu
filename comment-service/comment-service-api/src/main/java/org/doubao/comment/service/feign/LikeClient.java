package org.doubao.comment.service.feign;

import org.doubao.comment.service.config.FeignErrorDecoderConfig;
import org.doubao.comment.service.dto.BatchLikeStatusRequest;
import org.doubao.comment.service.dto.BatchLikeStatusResponse;
import org.doubao.comment.service.dto.CommentLikeRequest;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.feign.back.LikeClientFallback;
import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * 点赞服务Feign客户端
 * <p>
 * 在微服务模式下（service.run-mode=microservice），用于调用点赞服务的接口
 * 提供点赞/取消点赞和批量获取点赞状态的功能
 * 配置了错误解码器和降级处理机制
 */
@FeignClient(name = "like-service", fallbackFactory = LikeClientFallback.class,
		configuration = FeignErrorDecoderConfig.class)
@MicroserviceMode
public interface LikeClient {

	/**
	 * 切换点赞状态
	 * <p>
	 * 调用点赞服务的切换点赞状态接口，执行点赞或取消点赞操作
	 * 注意：参数需与点赞服务的ToggleLikeRequest字段对应，使用@RequestBody传递
	 * 
	 * @param request 点赞请求对象，包含操作用户ID、实体类型、实体ID等信息
	 * @return 包含点赞操作结果的响应对象
	 */
	@PostMapping("/like/toggle")
	Result<ToggleLikeResponse> toggleLike(@RequestBody CommentLikeRequest request);


	/**
	 * 批量获取点赞状态
	 * <p>
	 * 调用点赞服务的批量获取点赞状态接口，获取多个实体的点赞状态和计数
	 * 
	 * @param request 批量获取点赞状态请求对象，包含用户ID和实体列表
	 * @return 包含批量点赞状态结果的响应对象
	 */
	@PostMapping("/like/status")
	Result<BatchLikeStatusResponse> batchGetLikeStatus(@RequestBody BatchLikeStatusRequest request);
}