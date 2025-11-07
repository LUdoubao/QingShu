package org.doubao.like.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.like.service.dto.LikeQueryDto;
import org.doubao.like.service.dto.request.BatchLikeStatusRequest;
import org.doubao.like.service.dto.request.ToggleLikeRequest;
import org.doubao.like.service.dto.response.BatchLikeStatusResponse;
import org.doubao.like.service.dto.response.HotContentResponse;
import org.doubao.like.service.dto.response.LikeQuoteVo;
import org.doubao.like.service.dto.response.ToggleLikeResponse;
import org.doubao.like.service.service.LikeService;
import org.doubao.mall.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// LikeController.java
@RestController
@RequestMapping("/like")
public class LikeController {

	@Autowired
	private LikeService likeService;

	@PostMapping("/toggle")
	public Result<ToggleLikeResponse> toggleLike(@RequestBody ToggleLikeRequest request) {
		return Result.success(likeService.toggleLike(request));
	}

	@PostMapping("/status")
	public Result<BatchLikeStatusResponse> batchGetLikeStatus(@RequestBody BatchLikeStatusRequest request) {
		return Result.success(likeService.batchGetLikeStatus(request));
	}

	@GetMapping("/hot")
	public Result<List<HotContentResponse>> getHotContents(
			@RequestParam(value = "limit", defaultValue = "30") int limit) {
		return Result.success(likeService.getHotContents(limit));
	}
	@GetMapping("/list")
	public Result<Page<LikeQuoteVo>> likeList(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size) {
		return Result.success(likeService.likeList(userId, page, size));
	}
	@PostMapping("/count/batch")
	public Result<Map<Long, Long>> batchGetCounts(@RequestBody List<Long> contentIds) {
		Map<Long, Long> counts = likeService.batchCounts(contentIds);
		return Result.success(counts);
	}
}
