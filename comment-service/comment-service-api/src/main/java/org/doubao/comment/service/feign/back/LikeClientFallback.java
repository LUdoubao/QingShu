package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.dto.BatchLikeStatusRequest;
import org.doubao.comment.service.dto.BatchLikeStatusResponse;
import org.doubao.comment.service.dto.CommentLikeRequest;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.feign.LikeClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public class LikeClientFallback implements FallbackFactory<LikeClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LikeClientFallback.class);

    @Override
    public LikeClient create(Throwable cause) {
        LOGGER.error("Like service unavailable", cause);
        return new LikeClient() {
            @Override
            public Result<ToggleLikeResponse> toggleLike(CommentLikeRequest request) {
                LOGGER.error("toggleLike failed due to: {}", cause.getMessage());
                return Result.error( ErrorCode.COMMENT_LIKE_ERROR.getMessage());
            }

            @Override
            public Result<BatchLikeStatusResponse> batchGetLikeStatus(BatchLikeStatusRequest request) {
                LOGGER.error("batchGetLikeStatus failed due to: {}", cause.getMessage());
                return Result.success(BatchLikeStatusResponse.of(Collections.emptyList()));
            }
        };
    }
}
