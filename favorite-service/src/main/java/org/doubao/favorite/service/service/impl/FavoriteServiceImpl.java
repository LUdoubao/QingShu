package org.doubao.favorite.service.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.favorite.service.dto.QuoteCountDTO;
import org.doubao.favorite.service.entity.FavoriteContent;
import org.doubao.favorite.service.entity.FavoriteFolder;
import org.doubao.favorite.service.feign.QuoteServiceClient;
import org.doubao.favorite.service.mapper.FavoriteContentMapper;
import org.doubao.favorite.service.service.FavoriteService;
import org.doubao.favorite.service.service.FolderService;
import org.doubao.favorite.service.vo.FavoriteContentVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteContentMapper, FavoriteContent> implements FavoriteService {
	private final static Logger LOGGER = LoggerFactory.getLogger(FavoriteServiceImpl.class);
	private final QuoteServiceClient quoteServiceClient;
	private final FavoriteContentMapper favoriteMapper;
	private final FolderService folderService;

	@Autowired
	public FavoriteServiceImpl(FavoriteContentMapper favoriteMapper, FolderService folderService, QuoteServiceClient quoteServiceClient) {
		this.favoriteMapper = favoriteMapper;
		this.folderService = folderService;
		this.quoteServiceClient = quoteServiceClient;
	}

	@Override
	@Transactional
	public FavoriteContent addFavorite(Long userId, Long quoteId, Long folderId) {
		// 确保用户创建了默认文件夹
		FavoriteFolder folder = null;
		if (folderId == null) {
			folder = folderService.getUserDefaultFolder(userId);
		} else {
			folder = folderService.getUserFolders(userId).stream()
					.filter(f -> f.getId().equals(folderId))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("收藏夹不存在"));
		}

		// 检查是否已收藏
		FavoriteContent existing = favoriteMapper.selectByUserAndQuote(userId, quoteId);
		if (existing != null) {
			// 如果已收藏，返回已收藏的内容
			return existing;
		}

		// 创建新的收藏
		FavoriteContent favorite = new FavoriteContent();
		favorite.setUserId(userId);
		favorite.setQuoteId(quoteId);
		favorite.setFolderId(folder.getId());
		favorite.setCreatedTime(LocalDateTime.now());

		favoriteMapper.insert(favorite);
		return favorite;
	}

	@Override
	@Transactional
	public void removeFavorite(Long userId, Long quoteId) {
		// 查找用户收藏记录
		FavoriteContent favorite = favoriteMapper.selectByUserAndQuote(userId, quoteId);
		if (favorite != null) {
			// 软删除
			this.removeById(favorite.getId());
		}
	}

	@Override
	public void moveFavorite(Long favoriteId, Long newFolderId) {
		FavoriteContent favorite = favoriteMapper.selectById(favoriteId);
		if (favorite != null && favorite.getDeleted() == 0) {
			favorite.setFolderId(newFolderId);
			favoriteMapper.updateById(favorite);
		}
	}

	@Override
	public int countUserFavorites(Long userId) {
		return favoriteMapper.countByUser(userId);
	}

	@Override
	public Page<FavoriteContentVo> getUserFavoritesInFolder(Long folderId, int page, int size) {
		// 分页查询
		Page<FavoriteContent> pageParam = new Page<>(page, size);
		Page<FavoriteContent> favorites = favoriteMapper.selectPage(pageParam, new QueryWrapper<FavoriteContent>()
				.eq("folder_id", folderId)
				.eq("deleted", 0));
		Page<FavoriteContentVo> favoritePage = new Page<>(page, size, favorites.getTotal());
		if (!favorites.getRecords().isEmpty()) {
			List<Long> quoteIds = favorites.getRecords().stream().map(FavoriteContent::getQuoteId).collect(Collectors.toList());
			List<FavoriteContentVo> collect = favorites.getRecords().stream().map(favorite -> {
				FavoriteContentVo favoriteVo = new FavoriteContentVo();
				BeanUtils.copyProperties(favorite, favoriteVo);
				return favoriteVo;
			}).collect(Collectors.toList());
			List<Map<String, Object>> data = quoteServiceClient.getQuotesByIds(quoteIds).getData();
			LOGGER.info("quoteIds: {}, data: {}", JSON.toJSONString(quoteIds), JSON.toJSONString(data));
			if (data != null && !data.isEmpty()) {
				collect.forEach(favoriteVo -> {
					LOGGER.info("favoriteVo: {}", JSON.toJSONString(favoriteVo));
					for (Map<String, Object> d : data) {
						LOGGER.info("d: {}", JSON.toJSONString(d));
						LOGGER.info("d.getId(): {}, favoriteVo.getQuoteId(): {}", d.get("id"), favoriteVo.getQuoteId());
						if (Long.valueOf(d.get("id").toString()).equals(Long.valueOf(favoriteVo.getQuoteId().toString()))) {
							LOGGER.info("d1: {}", JSON.toJSONString(d));
							favoriteVo.setQuote(d);
							break;
						}
					}
				});
				favoritePage.setRecords(collect);
			}
		}
		return favoritePage;
	}

	@Override
	public Map<Long, Boolean> getFavoriteStatus(Long userId, List<Long> quoteIds) {
		if (quoteIds == null || quoteIds.isEmpty()) {
			return new HashMap<>();
		}

		// 查询用户是否收藏了这些quote
		Set<Long> favoritedIds = favoriteMapper.selectFavoriteIdsByUserAndQuotes(userId, quoteIds);

		return quoteIds.stream().collect(Collectors.toMap(
				id -> id,
				favoritedIds::contains
		));
	}

	@Override
	public Map<Long, Long> countQuotes(List<Long> quoteIds) {
		List<QuoteCountDTO> countQuotes = favoriteMapper.countQuotes(quoteIds);
		Map<Long, Long> longLongMap = new HashMap<>();

		if (countQuotes == null) {
			for (Long quoteId : quoteIds) {
				longLongMap.put(quoteId, 0L);
			}
			return longLongMap;
		}
		for (Long quoteId : quoteIds) {
			QuoteCountDTO quoteCountDTO = countQuotes.stream().filter(dto -> dto.getQuoteId().equals(quoteId)).findFirst().orElse(null);
			Long count = quoteCountDTO != null ? quoteCountDTO.getCount() : 0L;
			longLongMap.put(quoteId, count);
		}
		return longLongMap;
	}
}