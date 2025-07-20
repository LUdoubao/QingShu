package org.doubao.favorite.service.controller;

import org.doubao.favorite.service.entity.FavoriteFolder;
import org.doubao.favorite.service.service.FolderService;
import org.doubao.mall.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite/folder")
public class FolderController {
	private final FolderService folderService;

	@Autowired
	public FolderController(FolderService folderService) {
		this.folderService = folderService;
	}

	// 创建收藏夹
	@PostMapping("/create")
	public Result<FavoriteFolder> createFolder(
			@RequestParam Long userId,
			@RequestParam String folderName
	) {
		FavoriteFolder folder = folderService.createFolder(userId, folderName);
		return Result.success(folder);
	}

	// 获取用户收藏夹列表
	@GetMapping("/list")
	public Result<List<FavoriteFolder>> getUserFolders(@RequestParam Long userId) {
		List<FavoriteFolder> folders = folderService.getUserFolders(userId);
		return Result.success(folders);
	}

	// 重命名收藏夹
	@GetMapping("/rename/{folderId}")
	public Result<Void> renameFolder(
			@PathVariable Long folderId,
			@RequestParam String newName
	) {
		folderService.renameFolder(folderId, newName);
		return Result.success();
	}

	// 删除收藏夹
	@DeleteMapping("/{folderId}")
	public Result<Void> deleteFolder(@PathVariable Long folderId) {
		folderService.deleteFolder(folderId);
		return Result.success();
	}
}