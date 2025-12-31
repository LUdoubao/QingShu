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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 评论控制器
 * <p>
 * 提供评论相关的REST API接口，包括评论创建、查询、点赞、状态更新等功能
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
	@Autowired
	private CommentService commentService;

	/**
	 * 创建评论
	 * <p>
	 * 接收评论创建请求，调用服务层执行创建逻辑并返回评论ID
	 * 该接口会进行敏感词检测、字数校验、频率限制等验证
	 * 
	 * @param dto 评论数据传输对象，包含评论内容、关联文章ID等信息
	 * @return 包含评论唯一标识符的成功响应结果，评论ID格式为"C-"前缀加时间戳或"R-"前缀加时间戳
	 */
	@PostMapping("/create")
	public Result<String> createComment(@RequestBody @Valid CommentDTO dto) {
		String commentId = commentService.createComment(dto);
		return Result.success(commentId);
	}

	/**
	 * 分页加载评论的回复列表
	 * <p>
	 * 根据评论ID分页获取回复列表，对应前端loadMoreReplies方法的请求
	 * 用于评论详情页中分页加载回复内容
	 * 
	 * @param commentId 评论唯一标识符
	 * @param page 页码（从1开始），默认为1
	 * @param size 每页条数，默认为5，最大不超过20
	 * @return 包含分页回复数据的成功响应结果
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
	 * <p>
	 * 根据文章ID获取评论列表，支持分页和排序
	 * 使用缓存机制提升热点数据访问性能
	 * 
	 * @param postId 关联文章ID
	 * @param page 页码，默认为1
	 * @param size 每页大小，默认为10，最大不超过50
	 * @param sortType 排序类型，"hot"表示按热度排序，"time"表示按时间排序，默认为"hot"
	 * @return 包含分页评论数据的成功响应结果
	 */
	@GetMapping("/list")
	public Result<IPage<CommentVO>> getCommentList(
			@RequestParam String postId,
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer size,
			@RequestParam(required = false, defaultValue = "hot") String sortType) {

		// 参数校验：限制每页大小不超过50
		if (size > 50) {
			throw new BusinessException(ErrorCode.COMMENT_PAGE_SIZE_LIMIT);
		}

		// 分页查询
		IPage<CommentVO> commentPage = commentService.getCommentList(
				postId,
				new Page<>(page, size),
				sortType
		);
		return Result.success(commentPage);
	}

	/**
	 * 评论点赞/取消点赞接口
	 * <p>
	 * 前端调用此接口，无需知道底层点赞服务的存在
	 * 通过调用点赞服务完成实际的点赞逻辑，并更新评论表中的点赞数冗余字段
	 * 
	 * @param commentId 评论唯一标识符
	 * @return 包含点赞操作结果的成功响应，包含操作是否成功、操作类型和当前点赞总数
	 */
	@PostMapping("/{commentId}/like")
	public Result<ToggleLikeResponse> toggleCommentLike(
			@PathVariable String commentId) {
		// 调用服务层处理点赞逻辑
		ToggleLikeResponse response = commentService.toggleLike(commentId);
		return Result.success(response);
	}

	/**
	 * 更新评论状态
	 * <p>
	 * 根据请求参数更新评论的状态信息，如折叠、删除等状态
	 * 操作完成后会清理相关缓存，确保数据一致性
	 * 
	 * @param request 包含评论ID和状态的请求参数映射
	 * @return 成功响应结果
	 */
	@PostMapping("/updateStatus")
	public Result<Void> updateStatus(@RequestBody Map<String, String> request) {
		commentService.updateStatus(request);
		return Result.success();
	}
	
	/**
	 * 批量获取评论计数
	 * <p>
	 * 根据内容ID列表批量获取对应的评论数量
	 * 用于批量展示文章评论数的场景
	 * 
	 * @param contentIds 内容ID列表
	 * @return 包含内容ID到评论数量映射的成功响应结果
	 */
	@PostMapping("/count/batch")
	public Result<Map<Long, Long>> batchGetCounts(@RequestBody List<Long> contentIds) {
		Map<Long, Long> counts = commentService.batchCounts(contentIds);
		return Result.success(counts);
	}

	/**
	 * 批量统计每日评论数
	 * <p>
	 * 根据参数统计指定日期范围内每日的评论总数
	 * 用于数据统计和报表展示功能
	 * 
	 * @param params 统计参数，包含引文ID和日期列表
	 * @return 包含按日期分组的评论数量统计结果的成功响应
	 */
	@PostMapping("/count/sum")
	public Result<Map<LocalDate, Long>> batchSumDailyCounts(@RequestBody Map<String, Object> params) {
		Map<LocalDate, Long> counts = commentService.batchSumDailyCounts(params);
		return Result.success(counts);
	}
}