package org.doubao.favorite.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.favorite.service.dto.QuoteCountDTO;
import org.doubao.favorite.service.entity.FavoriteContent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface FavoriteContentMapper extends BaseMapper<FavoriteContent> {
	List<FavoriteContent> selectByFolderId(@Param("folderId") Long folderId);
	FavoriteContent selectByUserAndQuote(@Param("userId") Long userId, @Param("quoteId") Long quoteId, @Param("folderId") Long folderId);
	int countByFolderId(@Param("folderId") Long folderId);
	int countByUser(@Param("userId") Long userId);

	List<FavoriteContent> selectByFolderIdPaged(@Param("folderId") Long folderId, @Param("offset") int offset, @Param("size") int size);

	Set<Long> selectFavoriteIdsByUserAndQuotes(@Param("userId") Long userId, @Param("quoteIds") List<Long> quoteIds);

	List<QuoteCountDTO> countQuotes(@Param("quoteIds") List<Long> quoteIds);

	void removeByFolderId(@Param("folderId") Long folderId);

	void batchDelete(@Param("folderId") Long folderId, @Param("quoteIds") List<Long> quoteIds, @Param("userId")Long userId);

	/**
	 * 查询指定文章在多个日期的每日有效收藏数总和
	 * @param quoteIds 文章ID列表（Long类型，匹配FavoriteContent的quoteId）
	 * @param dates 日期列表
	 * @return 按日期分组的统计结果（键：stat_date-日期，值：total_count-当日总收藏数）
	 */
	List<Map<String, Object>> selectDailyFavoriteCounts(
			@Param("quoteIds") List<Long> quoteIds,
			@Param("dates") List<LocalDate> dates);
}