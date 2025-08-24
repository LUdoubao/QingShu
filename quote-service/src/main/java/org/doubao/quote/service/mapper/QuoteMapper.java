package org.doubao.quote.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.quote.service.entity.Quote;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuoteMapper extends BaseMapper<Quote> {
	// 返回总条数用于分页
	long countByTagIdsAndCategory(
			@Param("categoryId") Long categoryId,
			@Param("tagIds") List<Long> tagIds,
			@Param("tagIdsSize") Integer tagIdsSize,
			@Param("userId") Long userId,
			@Param("original") Integer original
	);

	List<Quote> selectByTagIdsAndCategory (@Param("categoryId") Long categoryId,
										   @Param("tagIds")  List<Long> tagIds,
										   @Param("tagIdsSize") Integer tagIdsSize,
										   @Param("pageSize") int pageSize,
										   @Param("pageNum") int pageNum,
										   @Param("userId") Long userId,
										   @Param("original") Integer original
	);

	int getQuoteType(@Param("quoteId") String quoteId);

	boolean checkQuoteExists(@Param("quoteId") String quoteId);
}
