package org.doubao.favorite.service.vo;

import org.doubao.favorite.service.entity.FavoriteFolder;

public class FavoriteFolderVo extends FavoriteFolder {
	private Long favoriteCount;
	private String createUserAvatar;
	private String createUserName;

	public Long getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(Long favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public String getCreateUserAvatar() {
		return createUserAvatar;
	}

	public void setCreateUserAvatar(String createUserAvatar) {
		this.createUserAvatar = createUserAvatar;
	}

	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}
}