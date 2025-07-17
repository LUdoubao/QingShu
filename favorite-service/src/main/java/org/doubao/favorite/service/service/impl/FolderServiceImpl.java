package org.doubao.favorite.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.favorite.service.entity.FavoriteFolder;
import org.doubao.favorite.service.mapper.FavoriteFolderMapper;
import org.doubao.favorite.service.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderServiceImpl extends ServiceImpl<FavoriteFolderMapper, FavoriteFolder> implements FolderService {
	private final FavoriteFolderMapper folderMapper;

	@Autowired
	public FolderServiceImpl(FavoriteFolderMapper folderMapper) {
		this.folderMapper = folderMapper;
	}

	@Override
	@Transactional
	public FavoriteFolder createFolder(Long userId, String folderName) {
		// 创建文件夹
		FavoriteFolder folder = new FavoriteFolder();
		folder.setUserId(userId);
		folder.setName(folderName);
		folder.setCreatedTime(LocalDateTime.now());
		folder.setUpdatedTime(LocalDateTime.now());

		folderMapper.insert(folder);
		return folder;
	}

	@Override
	@Transactional
	public void deleteFolder(Long folderId) {
		FavoriteFolder folder = folderMapper.selectById(folderId);
		if (folder != null && folder.getIsDefault() == 1) {
			throw new RuntimeException("默认收藏夹不能删除");
		}

		// 逻辑删除
		folder.setDeleted(1);
		folder.setUpdatedTime(LocalDateTime.now());
		folderMapper.updateById(folder);

		// 删除文件夹中的内容
		// 实际实现中可能需要软删除文件夹中的内容
	}

	@Override
	public void renameFolder(Long folderId, String newName) {
		FavoriteFolder folder = folderMapper.selectById(folderId);
		if (folder != null) {
			folder.setName(newName);
			folder.setUpdatedTime(LocalDateTime.now());
			folderMapper.updateById(folder);
		}
	}

	@Override
	public FavoriteFolder getUserDefaultFolder(Long userId) {
		// 如果存在则返回，不存在则创建
		List<FavoriteFolder> userFolders = folderMapper.selectUserFolders(userId);

		for (FavoriteFolder folder : userFolders) {
			if (folder.getIsDefault() == 1) {
				return folder;
			}
		}

		// 创建默认收藏夹
		FavoriteFolder defaultFolder = new FavoriteFolder();
		defaultFolder.setUserId(userId);
		defaultFolder.setName("我的收藏");
		defaultFolder.setIsDefault(1);
		defaultFolder.setCreatedTime(LocalDateTime.now());
		defaultFolder.setUpdatedTime(LocalDateTime.now());

		folderMapper.insert(defaultFolder);
		return defaultFolder;
	}

	@Override
	public List<FavoriteFolder> getUserFolders(Long userId) {
		List<FavoriteFolder> favoriteFolders = folderMapper.selectUserFolders(userId);
		if (favoriteFolders.isEmpty()) {
			FavoriteFolder userDefaultFolder = getUserDefaultFolder(userId);
			favoriteFolders.add(userDefaultFolder);
		}
 		return favoriteFolders;
	}
}