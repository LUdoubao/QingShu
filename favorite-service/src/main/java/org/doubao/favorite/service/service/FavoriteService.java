package org.doubao.favorite.service.service;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.favorite.service.entity.FavoriteContent;
import org.doubao.favorite.service.vo.FavoriteContentVo;

import java.util.List;
import java.util.Map;

public interface FavoriteService extends IService<FavoriteContent> {
	FavoriteContent addFavorite(Long userId, Long quoteId, Long folderId);
	void removeFavorite(Long userId, Long quoteId);
	void moveFavorite(Long favoriteId, Long newFolderId);
	int countUserFavorites(Long userId);
	Page<FavoriteContentVo> getUserFavoritesInFolder(Long folderId, int page, int size);
	Map<Long, Boolean> getFavoriteStatus(Long userId, List<Long> quoteIds);

	Map<Long, Long> countQuotes(List<Long> quoteIds);
}