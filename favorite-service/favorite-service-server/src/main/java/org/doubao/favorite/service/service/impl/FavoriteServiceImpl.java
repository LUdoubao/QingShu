package org.doubao.favorite.service.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.favorite.service.dto.BatchDelDto;
import org.doubao.favorite.service.dto.QuoteCountDTO;
import org.doubao.favorite.service.entity.FavoriteContent;
import org.doubao.favorite.service.entity.FavoriteFolder;
import org.doubao.favorite.service.feign.QuoteServiceClient;
import org.doubao.favorite.service.mapper.FavoriteContentMapper;
import org.doubao.favorite.service.service.FavoriteService;
import org.doubao.favorite.service.utils.FavoriteComponent;
import org.doubao.favorite.service.vo.FavoriteContentVo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.ConvertUtil;
import org.doubao.mall.common.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteContentMapper, FavoriteContent> implements FavoriteService {
	private final static Logger LOGGER = LoggerFactory.getLogger(FavoriteServiceImpl.class);
	private final QuoteServiceClient quoteServiceClient;
	private final FavoriteContentMapper favoriteMapper;

	@Resource
	private FavoriteComponent favoriteComponent;
	@Autowired
	public FavoriteServiceImpl(FavoriteContentMapper favoriteMapper, QuoteServiceClient quoteServiceClient) {
		this.favoriteMapper = favoriteMapper;
		this.quoteServiceClient = quoteServiceClient;
	}

	@Override
	@Transactional
	public FavoriteContent addFavorite(Long userId, Long quoteId, Long folderId, Integer type) {
		FavoriteFolder folder;
		if (folderId == null) {
			// 存入默认类型
			folder = favoriteComponent.getUserDefaultFolder(userId,  type);
			if (folder == null) {
				throw new BusinessException(ErrorCode.FAVORITE_FOLDER_NOT_EXIST);
			}
			folderId = folder.getId();
		}

		// 检查是否已收藏
		FavoriteContent existing = favoriteMapper.selectByUserAndQuote(userId, quoteId, folderId);
		if (existing != null) {
			throw new BusinessException(ErrorCode.FAVORITE_EXIST);
		}

		// 创建新的收藏
		FavoriteContent favorite = new FavoriteContent();
		favorite.setUserId(userId);
		favorite.setQuoteId(quoteId);
		favorite.setFolderId(folderId);
		favorite.setType(type);
		favorite.setCreatedTime(LocalDateTime.now());

		favoriteMapper.insert(favorite);
		return favorite;
	}

	@Override
	@Transactional
	public void removeFavorite(Long userId, Long quoteId) {
		// 查找用户收藏记录
		FavoriteContent favorite = favoriteMapper.selectByUserAndQuote(userId, quoteId, null);
		if (favorite != null) {
			// 软删除
			LambdaUpdateWrapper<FavoriteContent> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(FavoriteContent::getQuoteId, quoteId);
			updateWrapper.eq(FavoriteContent::getUserId, userId);
			updateWrapper.set(FavoriteContent::getDeleted, 1);
			this.update(updateWrapper);
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
			Map<Long, Long> countQuotes = countQuotes(quoteIds);
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
							favoriteVo.setFavoriteCount(countQuotes.getOrDefault(favoriteVo.getQuoteId(), 0L));
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

	@Override
	public void batchDelete(BatchDelDto batchDelDto) {
		Long folderId = batchDelDto.getFolderId();
		List<Long> quoteIds = batchDelDto.getQuoteIds();
		Long userId = batchDelDto.getUserId();
		// 防御性检查
		if (folderId == null || quoteIds == null || quoteIds.isEmpty()) {
			// 打印各参数
			LOGGER.error("folderId: {}, quoteIds: {}, userId: {}", folderId, quoteIds, userId);
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		Long loginUserId = UserContext.getUserId();
		if (!loginUserId.equals(userId)) {
			LOGGER.error("loginUserId: {}, userId: {}", loginUserId, userId);
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		favoriteMapper.batchDelete(folderId, quoteIds, userId);
		LOGGER.info("删除收藏夹中的内容成功: {}", folderId);
	}

	@Override
	public Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			LOGGER.error("批量查询每日收藏数参数为空");
			return Collections.emptyMap();
		}

		// 1. 解析参数：获取文章ID列表和日期列表
		List<Long> quoteIds = ConvertUtil.safeConvertToListOfLong(params.get("quoteIds"));
		List<LocalDate> dates = ConvertUtil.safeConvertToListOfLocalDate(params.get("dates"));


		// 参数校验：文章ID和日期列表不可为空
		if (CollectionUtils.isEmpty(quoteIds) || CollectionUtils.isEmpty(dates)) {
			LOGGER.error("批量查询每日收藏数参数不完整：quoteIds={}, dates={}", quoteIds, dates);
			return Collections.emptyMap();
		}

		// 2. 调用Mapper查询指定日期和文章的有效收藏数总和（按日期分组）
		List<Map<String, Object>> dailyCounts = favoriteMapper.selectDailyFavoriteCounts(quoteIds, dates);

		// 3. 转换查询结果为Map<LocalDate, Long>（日期→当日总收藏数）
		Map<LocalDate, Long> resultMap = new HashMap<>(dates.size());

		// 先初始化所有日期的计数为0（确保每个日期都有返回值）
		for (LocalDate date : dates) {
			resultMap.put(date, 0L);
		}

		// 填充查询到的实际计数（覆盖初始值）
		for (Map<String, Object> countMap : dailyCounts) {
			// 从查询结果中提取日期和计数（数据库字段与Java类型映射）
			LocalDate statDate = ConvertUtil.safeParseLocalDate(countMap.get("stat_date"));
			Long totalCount = ConvertUtil.safeParseLong(countMap.get("total_count"));

			// 仅更新输入日期列表中存在的日期
			if (statDate != null && resultMap.containsKey(statDate)) {
				resultMap.put(statDate, totalCount);
			}
		}

		LOGGER.info("批量查询每日收藏数完成：日期范围={}至{}, 文章数量={}, 结果={}",
				dates.get(0), dates.get(dates.size() - 1), quoteIds.size(), resultMap);
		return resultMap;
	}
}