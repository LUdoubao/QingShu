package org.doubao.favorite.service.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.favorite.service.entity.FavoriteFolder;
import org.doubao.favorite.service.vo.FavoriteFolderVo;

import java.util.List;

public interface FolderService extends IService<FavoriteFolder> {
	FavoriteFolder createFolder(Long userId, String folderName);
	void deleteFolder(Long folderId);
	void renameFolder(Long folderId, String newName);
	List<FavoriteFolder> getUserFolders(Long userId);

	FavoriteFolderVo getDetailById(Long folderId);
}