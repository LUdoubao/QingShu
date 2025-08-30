package org.doubao.feed.service.controller;

import org.doubao.feed.service.feign.CommentClient;
import org.doubao.feed.service.feign.LikeClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/feed/interact")
public class InteractController {

	private static final Logger log = LoggerFactory.getLogger(InteractController.class);
	@Resource
	private LikeClient likeClient;
	@Resource
	private CommentClient commentClient;

	/**
	 * 点赞/取消点赞
	 */
	@PostMapping("/like")
	public Result<Map<String, Object>> likeAction(
			@RequestHeader("X-User-Id") Long userId,

			@RequestParam String targetType,

			@RequestParam Long targetId,

			@RequestParam Integer action) {

		try {
			return likeClient.likeAction(userId, targetType, targetId, action);
		} catch (Exception e) {
			log.error("[likeAction] 点赞操作失败，userId: {}, targetId: {}", userId, targetId, e);
			return Result.error("点赞操作失败");
		}
	}

	/**
	 * 发布评论
	 */
	@PostMapping("/comment")
	public Result<CommentClient.CommentDTO> publishComment(
			@RequestHeader("X-User-Id") Long userId,

			@RequestParam String targetType,

			@RequestParam Long targetId,

			@RequestParam String content) {

		try {
			return commentClient.publishComment(userId, targetType, targetId, content);
		} catch (Exception e) {
			log.error("[publishComment] 发布评论失败，userId: {}, targetId: {}", userId, targetId, e);
			return Result.error("发布评论失败");
		}
	}

	/**
	 * 获取评论列表
	 */
	@GetMapping("/comments")
	public Result<PageResult<CommentClient.CommentDTO>> getComments(
			@RequestParam String targetType,

			@RequestParam Long targetId,

			@RequestParam(defaultValue = "1") int pageNum,

			@RequestParam(defaultValue = "10") int pageSize) {

		try {
			return commentClient.getComments(targetType, targetId, pageNum, pageSize);
		} catch (Exception e) {
			log.error("[getComments] 获取评论列表失败，targetId: {}", targetId, e);
			return Result.error("获取评论列表失败");
		}
	}
}
