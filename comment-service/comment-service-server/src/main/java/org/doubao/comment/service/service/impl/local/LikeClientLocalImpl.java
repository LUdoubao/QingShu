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

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class LikeClientLocalImpl implements LikeClient {

	@Resource
	private LikeService likeService;

	@Override
	public Result<org.doubao.comment.service.dto.ToggleLikeResponse> toggleLike(org.doubao.comment.service.dto.CommentLikeRequest request) {
		ToggleLikeRequest likeRequest = new ToggleLikeRequest();
		BeanUtils.copyProperties(request, likeRequest);
		ToggleLikeResponse toggleLikeResponse = likeService.toggleLike(likeRequest);
		org.doubao.comment.service.dto.ToggleLikeResponse likeResponse = new org.doubao.comment.service.dto.ToggleLikeResponse();
		BeanUtils.copyProperties(toggleLikeResponse, likeResponse);
		return Result.success(likeResponse);

	}

	@Override
	public Result<org.doubao.comment.service.dto.BatchLikeStatusResponse> batchGetLikeStatus(org.doubao.comment.service.dto.BatchLikeStatusRequest request) {
		BatchLikeStatusRequest likeRequest = new BatchLikeStatusRequest();
		BeanUtils.copyProperties(request, likeRequest);
		BatchLikeStatusResponse response = likeService.batchGetLikeStatus(likeRequest);
		org.doubao.comment.service.dto.BatchLikeStatusResponse likeResponse = new org.doubao.comment.service.dto.BatchLikeStatusResponse();
		BeanUtils.copyProperties(response, likeResponse);
		return Result.success(likeResponse);
	}
}