package org.doubao.comment.service.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "quote-service")
public interface QuoteClient {
	boolean isAuthor(String postId, Integer userId);
}
