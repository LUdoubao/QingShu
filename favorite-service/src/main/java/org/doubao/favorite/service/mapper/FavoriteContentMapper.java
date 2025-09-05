package org.doubao.favorite.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.favorite.service.dto.QuoteCountDTO;
import org.doubao.favorite.service.entity.FavoriteContent;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface FavoriteContentMapper extends BaseMapper<FavoriteContent> {
	List<FavoriteContent> selectByFolderId(@Param("folderId") Long folderId);
	FavoriteContent selectByUserAndQuote(@Param("userId") Long userId, @Param("quoteId") Long quoteId);
	int countByFolderId(@Param("folderId") Long folderId);
	int countByUser(@Param("userId") Long userId);

	List<FavoriteContent> selectByFolderIdPaged(@Param("folderId") Long folderId, @Param("offset") int offset, @Param("size") int size);

	Set<Long> selectFavoriteIdsByUserAndQuotes(@Param("userId") Long userId, @Param("quoteIds") List<Long> quoteIds);

	List<QuoteCountDTO> countQuotes(@Param("quoteIds") List<Long> quoteIds);

	void removeByFolderId(@Param("folderId") Long folderId);

	void batchDelete(@Param("folderId") Long folderId, @Param("quoteIds") List<Long> quoteIds, @Param("userId")Long userId);
}