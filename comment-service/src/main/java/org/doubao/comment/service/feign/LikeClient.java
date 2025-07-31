package org.doubao.comment.service.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "like-service")
public interface LikeClient {

	Boolean isLiked(String commentId, Long currentUserId);
}
