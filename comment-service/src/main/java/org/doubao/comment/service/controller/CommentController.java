package org.doubao.comment.service.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.doubao.comment.service.dto.CommentDTO;
import org.doubao.comment.service.dto.ReplyDTO;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.service.CommentService;
import org.doubao.comment.service.vo.CommentVO;
import org.doubao.comment.service.vo.ReplyVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {
	@Autowired
	private CommentService commentService;

	@PostMapping("/create")
	public Result<String> createComment(@RequestBody @Valid CommentDTO dto) {
		String commentId = commentService.createComment(dto);
		return Result.success(commentId);
	}

	/**
	 * 分页加载评论的回复列表
	 * 对应前端loadMoreReplies方法的请求
	 */
	@GetMapping("/replies")
	@ApiOperation(value = "分页加载评论的回复", notes = "根据评论ID分页获取回复列表")
	public Result<Page<ReplyVO>> getReplies(
			@ApiParam(value = "评论ID", required = true, example = "C-202507241001")
			@RequestParam String commentId,
			@ApiParam(value = "页码（从1开始）", defaultValue = "1", example = "1")
			@RequestParam(defaultValue = "1") Integer page,
			@ApiParam(value = "每页条数", defaultValue = "5", example = "5")
			@RequestParam(defaultValue = "5") Integer size) {
		// 调用服务层获取分页回复数据
		Page<ReplyVO> replyPage = commentService.getRepliesByCommentId(commentId, page, size);
		return Result.success(replyPage);
	}

	/**
	 * 获取评论列表
	 */
	@GetMapping("/list")
	public Result<IPage<CommentVO>> getCommentList(
			@RequestParam String postId,
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer size,
			@RequestParam(required = false, defaultValue = "hot") String sortType) {

		// 参数校验
		if (size > 50) {
			throw new BusinessException(ErrorCode.COMMENT_PAGE_SIZE_LIMIT);
		}

		// 分页查询
		IPage<CommentVO> commentPage = commentService.getCommentList(
				postId,
				Page.of(page, size),
				sortType
		);
		return Result.success(commentPage);
	}

	/**
	 * 评论点赞/取消点赞接口
	 * 前端调用此接口，无需知道底层点赞服务的存在
	 */
	@PostMapping("/{commentId}/like")
	public Result<ToggleLikeResponse> toggleCommentLike(
			@PathVariable String commentId) {
		// 调用服务层处理点赞逻辑
		ToggleLikeResponse response = commentService.toggleLike(commentId);
		return Result.success(response);
	}

	// @PostMapping("/{commentId}/reply")
	// public Result<String> replyComment(
	// 		@PathVariable String commentId,
	// 		@RequestBody ReplyDTO dto) {
	// 	String replyId = commentService.replyComment(commentId, dto);
	// 	return Result.success(replyId);
	// }

	// @PutMapping("/{commentId}/top")
	// public Result<Void> topComment(
	// 		@PathVariable String commentId,
	// 		@RequestBody Map<String, String> action) {
	// 	commentService.handleTop(commentId, action.get("action"));
	// 	return Result.success(null);
	// }

	// @DeleteMapping("/{commentId}")
	// public Result<Void> deleteComment(@PathVariable String commentId) {
	// 	commentService.deleteComment(commentId);
	// 	return Result.success(null);
	// }
}