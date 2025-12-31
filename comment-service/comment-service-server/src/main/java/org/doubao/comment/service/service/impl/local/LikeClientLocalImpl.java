package org.doubao.comment.service.service.impl.local;

import org.doubao.comment.service.feign.LikeClient;
import org.doubao.like.service.dto.request.BatchLikeStatusRequest;
import org.doubao.like.service.dto.request.ToggleLikeRequest;
import org.doubao.like.service.dto.response.BatchLikeStatusResponse;
import org.doubao.like.service.dto.response.ToggleLikeResponse;
import org.doubao.like.service.service.LikeService;
import org.doubao.mall.common.entity.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 评论服务本地点赞客户端实现
 * <p>
 * 在单体模式下（service.run-mode=monolith）的点赞客户端实现，
 * 通过直接调用本地LikeService来处理点赞相关操作，
 * 避免了微服务间的网络调用，提高性能
 */
@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class LikeClientLocalImpl implements LikeClient {

	@Resource
	private LikeService likeService;

	/**
	 * 切换点赞状态
	 * <p>
	 * 在单体模式下直接调用本地LikeService处理点赞/取消点赞操作，
	 * 将评论服务的请求对象转换为点赞服务的请求对象，
	 * 并将点赞服务的响应对象转换回评论服务的响应对象
	 * 
	 * @param request 评论服务的点赞请求对象
	 * @return 包含点赞操作结果的成功响应，或包含错误信息的失败响应
	 */
	@Override
	public Result<org.doubao.comment.service.dto.ToggleLikeResponse> toggleLike(org.doubao.comment.service.dto.CommentLikeRequest request) {
		// 将评论服务的请求对象转换为点赞服务的请求对象
		ToggleLikeRequest likeRequest = new ToggleLikeRequest();
		BeanUtils.copyProperties(request, likeRequest);
		// 调用本地点赞服务
		ToggleLikeResponse toggleLikeResponse = likeService.toggleLike(likeRequest);
		// 将点赞服务的响应对象转换回评论服务的响应对象
		org.doubao.comment.service.dto.ToggleLikeResponse likeResponse = new org.doubao.comment.service.dto.ToggleLikeResponse();
		BeanUtils.copyProperties(toggleLikeResponse, likeResponse);
		return Result.success(likeResponse);

	}

	/**
	 * 批量获取点赞状态
	 * <p>
	 * 在单体模式下直接调用本地LikeService批量获取实体的点赞状态，
	 * 将评论服务的请求对象转换为点赞服务的请求对象，
	 * 并将点赞服务的响应对象转换回评论服务的响应对象
	 * 
	 * @param request 评论服务的批量获取点赞状态请求对象
	 * @return 包含批量点赞状态结果的成功响应，或包含错误信息的失败响应
	 */
	@Override
	public Result<org.doubao.comment.service.dto.BatchLikeStatusResponse> batchGetLikeStatus(org.doubao.comment.service.dto.BatchLikeStatusRequest request) {
		// 将评论服务的请求对象转换为点赞服务的请求对象
		BatchLikeStatusRequest likeRequest = new BatchLikeStatusRequest();
		BeanUtils.copyProperties(request, likeRequest);
		// 调用本地点赞服务
		BatchLikeStatusResponse response = likeService.batchGetLikeStatus(likeRequest);
		// 将点赞服务的响应对象转换回评论服务的响应对象
		org.doubao.comment.service.dto.BatchLikeStatusResponse likeResponse = new org.doubao.comment.service.dto.BatchLikeStatusResponse();
		BeanUtils.copyProperties(response, likeResponse);
		return Result.success(likeResponse);
	}
}