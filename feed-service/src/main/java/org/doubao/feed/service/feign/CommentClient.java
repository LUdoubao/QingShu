package org.doubao.feed.service.feign;

import org.doubao.feed.service.feign.fallback.CommentClientFallbackFactory;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "comment-service", fallbackFactory = CommentClientFallbackFactory.class)
public interface CommentClient {

	/**
	 * 发布评论
	 */
	@PostMapping("/comment")
	Result<CommentDTO> publishComment(
			@RequestParam("userId") Long userId,
			@RequestParam("targetType") String targetType,
			@RequestParam("targetId") Long targetId,
			@RequestParam("content") String content);

	/**
	 * 获取评论列表
	 */
	@GetMapping("/comment/list")
	Result<PageResult<CommentDTO>> getComments(
			@RequestParam("targetType") String targetType,
			@RequestParam("targetId") Long targetId,
			@RequestParam("pageNum") int pageNum,
			@RequestParam("pageSize") int pageSize);

	/**
	 * 评论DTO
	 */
	class CommentDTO {
		private Long id;
		private Long userId;
		private String userName;
		private String userAvatar;
		private String content;
		private String createdTime;
		private Integer likeCount;
		private Boolean liked;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getUserAvatar() {
			return userAvatar;
		}

		public void setUserAvatar(String userAvatar) {
			this.userAvatar = userAvatar;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(String createdTime) {
			this.createdTime = createdTime;
		}

		public Integer getLikeCount() {
			return likeCount;
		}

		public void setLikeCount(Integer likeCount) {
			this.likeCount = likeCount;
		}

		public Boolean getLiked() {
			return liked;
		}

		public void setLiked(Boolean liked) {
			this.liked = liked;
		}
	}
}
