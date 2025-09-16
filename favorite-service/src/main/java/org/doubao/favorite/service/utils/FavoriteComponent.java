package org.doubao.favorite.service.utils;

import org.doubao.favorite.service.entity.FavoriteFolder;
import org.doubao.favorite.service.mapper.FavoriteContentMapper;
import org.doubao.favorite.service.mapper.FavoriteFolderMapper;
import org.doubao.favorite.service.service.FavoriteService;
import org.doubao.favorite.service.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class FavoriteComponent {
	@Resource
	private FavoriteContentMapper favoriteContentMapper;
	@Autowired
	private FavoriteFolderMapper folderMapper;
	public FavoriteFolder getUserDefaultFolder(Long userId, Integer type) {
		// 如果存在则返回，不存在则创建
		List<FavoriteFolder> userFolders = folderMapper.selectUserFolders(userId, type);

		for (FavoriteFolder folder : userFolders) {
			if (folder.getIsDefault() == 1) {
				return folder;
			}
		}

		// 创建默认收藏夹
		FavoriteFolder defaultFolder = new FavoriteFolder();
		defaultFolder.setUserId(userId);
		defaultFolder.setType(type);
		defaultFolder.setName(type == 0 ? "文案" : "其他");
		defaultFolder.setIsDefault(1);
		defaultFolder.setCreatedTime(LocalDateTime.now());
		defaultFolder.setUpdatedTime(LocalDateTime.now());

		folderMapper.insert(defaultFolder);
		return defaultFolder;
	}

	public void removeByFolderId(Long folderId) {
		// 逻辑删除 文件夹中的内容
		favoriteContentMapper.removeByFolderId(folderId);
	}



}
