package org.doubao.feed.service.feign.fallback;

import org.doubao.feed.service.feign.CommentClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CommentClientFallbackFactory implements FallbackFactory<CommentClient> {
	private static final Logger log =  LoggerFactory.getLogger(CommentClientFallbackFactory.class);

	@Override
	public CommentClient create(Throwable cause) {
		return new CommentClient() {

			@Override
			public Result<CommentDTO> publishComment(Long userId, String targetType, Long targetId, String content) {
				log.error("[CommentClient] publishComment fallback, userId: {}, targetType: {}, targetId: {}, content: {}, cause: {}",
						userId, targetType, targetId, content, cause.getMessage());
				return Result.success(new CommentDTO());
			}

			@Override
			public Result<PageResult<CommentDTO>> getComments(String targetType, Long targetId, int pageNum, int pageSize) {
				log.error("[CommentClient] getComments fallback, targetType: {}, targetId: {}, pageNum: {}, pageSize: {}, cause: {}",
						targetType, targetId, pageNum, pageSize, cause.getMessage());
				return Result.success(new PageResult<>());
			}
		};
	}
}
