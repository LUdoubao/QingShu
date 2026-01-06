package org.doubao.favorite.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.favorite.service.entity.FavoriteFolder;
import org.doubao.favorite.service.feign.UserClient;
import org.doubao.favorite.service.mapper.FavoriteFolderMapper;
import org.doubao.favorite.service.service.FolderService;
import org.doubao.favorite.service.utils.FavoriteComponent;
import org.doubao.favorite.service.vo.FavoriteFolderVo;
import org.doubao.mall.common.entity.UserInfoDes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FolderServiceImpl extends ServiceImpl<FavoriteFolderMapper, FavoriteFolder> implements FolderService {
	@Autowired
	private FavoriteFolderMapper folderMapper;
	@Resource
	private UserClient userClient;
	@Resource
	private FavoriteComponent favoriteComponent;

	private static final Logger logger = LoggerFactory.getLogger(FolderServiceImpl.class);
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
		if (folder == null) {
			throw new RuntimeException("收藏夹不存在");
		}
		if (folder.getIsDefault() == 1) {
			throw new RuntimeException("默认收藏夹不能删除");
		}

		this.removeById(folderId);
		logger.info("删除收藏夹成功：{}", folderId);

		// 软删除文件夹中的内容
		favoriteComponent.removeByFolderId(folderId);
		logger.info("删除收藏夹中的内容成功：{}", folderId);
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
	public List<FavoriteFolder> getUserFolders(Long userId, Integer type) {
		List<FavoriteFolder> favoriteFolders = folderMapper.selectUserFolders(userId, type);
		if (favoriteFolders == null || favoriteFolders.isEmpty()) {
			favoriteFolders = new ArrayList<>();
			// 创建默认收藏夹
			favoriteFolders.add(favoriteComponent.getUserDefaultFolder(userId, type));
			return favoriteFolders;
		} else {
			Optional<FavoriteFolder> folder = favoriteFolders.stream().filter(favoriteFolder -> favoriteFolder.getIsDefault() == 1).findFirst();
			if (!folder.isPresent()) {
				// 创建默认收藏夹
				favoriteFolders.add(favoriteComponent.getUserDefaultFolder(userId, type));
			}
		}
 		return favoriteFolders;
	}

	@Override
	public FavoriteFolderVo getDetailById(Long folderId) {
		FavoriteFolder favoriteFolder = folderMapper.selectById(folderId);
		if (favoriteFolder != null) {
			FavoriteFolderVo favoriteFolderVo = new FavoriteFolderVo();
			BeanUtils.copyProperties(favoriteFolder, favoriteFolderVo);
			// 查询收藏夹中内容数
			favoriteFolderVo.setFavoriteCount(folderMapper.selectFavoriteCount(folderId));
			// 查询用户信息
			List<UserInfoDes> userInfos = userClient.getUsersByIds(Collections.singleton(favoriteFolder.getUserId())).getData();
			if (!userInfos.isEmpty()) {
				UserInfoDes userInfo = userInfos.get(0);
				favoriteFolderVo.setCreateUserName(userInfo.getNickname());
				favoriteFolderVo.setCreateUserAvatar(userInfo.getAvatarUrl());
			}
			return favoriteFolderVo;
		}
		return new FavoriteFolderVo();
	}
}