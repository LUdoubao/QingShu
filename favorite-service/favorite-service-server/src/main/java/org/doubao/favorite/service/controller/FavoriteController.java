package org.doubao.favorite.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.favorite.service.dto.BatchDelDto;
import org.doubao.favorite.service.entity.FavoriteContent;
import org.doubao.favorite.service.service.FavoriteService;
import org.doubao.favorite.service.vo.FavoriteContentVo;
import org.doubao.mall.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {
	private final FavoriteService favoriteService;

	@Autowired
	public FavoriteController(FavoriteService favoriteService) {
		this.favoriteService = favoriteService;
	}

	// 添加收藏
	@PostMapping("/add")
	public Result<FavoriteContent> addFavorite(
			@RequestParam Long userId,
			@RequestParam Long quoteId,
			@RequestParam(required = false) Long folderId,
			@RequestParam(required = false) Integer type
	) {
		FavoriteContent favorite = favoriteService.addFavorite(userId, quoteId, folderId, type);
		return Result.success(favorite);
	}

	// 取消收藏
	@PostMapping("/remove")
	public Result<Void> removeFavorite(
			@RequestParam Long userId,
			@RequestParam Long quoteId
	) {
		favoriteService.removeFavorite(userId, quoteId);
		return Result.success();
	}

	@PostMapping("/batch-delete")
	public Result<Void> batchDelete(
			@RequestBody BatchDelDto batchDelDto
	) {
		favoriteService.batchDelete(batchDelDto);
		return Result.success();
	}


	// 获取用户收藏总数
	@GetMapping("/count")
	public Result<Integer> countUserFavorites(@RequestParam Long userId) {
		int count = favoriteService.countUserFavorites(userId);
		return Result.success(count);
	}

	@PostMapping("/quote/count")
	public Result<Map<Long, Long>> countQuotes(@RequestBody List<Long> quoteIds) {
		return Result.success(favoriteService.countQuotes(quoteIds));
	}

	// 获取收藏状态
	@PostMapping("/status")
	public Result<Map<Long, Boolean>> getFavoriteStatus(
			@RequestParam Long userId,
			@RequestBody List<Long> quoteIds
	) {
		Map<Long, Boolean> status = favoriteService.getFavoriteStatus(userId, quoteIds);
		return Result.success(status);
	}

	// 获取收藏夹内容列表
	@GetMapping("/list")
	public Result<Page<FavoriteContentVo>> getFavoriteList(
			@RequestParam Long folderId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		Page<FavoriteContentVo> favorites = favoriteService.getUserFavoritesInFolder(folderId, page, size);
		return Result.success(favorites);
	}
	@PostMapping("/quote/sum")
	public Result<Map<LocalDate, Long>> batchSumDailyCounts(@RequestBody Map<String, Object> params) {
		return Result.success(favoriteService.batchSumDailyCounts(params));
	}
}