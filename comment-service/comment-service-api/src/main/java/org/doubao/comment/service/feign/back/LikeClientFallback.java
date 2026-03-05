package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.dto.BatchLikeStatusRequest;
import org.doubao.comment.service.dto.BatchLikeStatusResponse;
import org.doubao.comment.service.dto.CommentLikeRequest;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.feign.LikeClient;
import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 点赞服务降级处理工厂
 * <p>
 * 在微服务模式下（service.run-mode=microservice），当点赞服务不可用时，
 * 提供降级处理逻辑，确保评论服务的可用性
 */
@Component
@MicroserviceMode
public class LikeClientFallback implements FallbackFactory<LikeClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LikeClientFallback.class);

    /**
     * 创建点赞服务降级实现
     * <p>
     * 当点赞服务调用失败时，返回降级实现，提供默认的错误处理逻辑
     * 
     * @param cause 服务调用失败的原因
     * @return 点赞服务的降级实现
     */
    @Override
    public LikeClient create(Throwable cause) {
        LOGGER.error("Like service unavailable", cause);
        return new LikeClient() {
            /**
             * 点赞/取消点赞操作降级处理
             * <p>
             * 当点赞服务不可用时，返回错误信息，避免影响评论服务的正常运行
             * 
             * @param request 点赞请求对象
             * @return 包含错误信息的响应结果
             */
            @Override
            public Result<ToggleLikeResponse> toggleLike(CommentLikeRequest request) {
                LOGGER.error("toggleLike failed due to: {}", cause.getMessage());
                return Result.error( ErrorCode.COMMENT_LIKE_ERROR.getMessage());
            }

            /**
             * 批量获取点赞状态操作降级处理
             * <p>
             * 当点赞服务不可用时，返回空的点赞状态列表，避免影响评论服务的正常运行
             * 
             * @param request 批量获取点赞状态请求对象
             * @return 包含空列表的成功响应结果
             */
            @Override
            public Result<BatchLikeStatusResponse> batchGetLikeStatus(BatchLikeStatusRequest request) {
                LOGGER.error("batchGetLikeStatus failed due to: {}", cause.getMessage());
                return Result.success(BatchLikeStatusResponse.of(Collections.emptyList()));
            }
        };
    }
}