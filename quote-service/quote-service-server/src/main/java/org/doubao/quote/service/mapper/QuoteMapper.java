package org.doubao.quote.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.vo.CategoryCountVO;
import org.doubao.quote.service.vo.QuoteStatusCountVo;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuoteMapper extends BaseMapper<Quote> {
	// 返回总条数用于分页
	long countByTagIdsAndCategory(
			@Param("tagIds") List<Long> tagIds,
			@Param("tagIdsSize") Integer tagIdsSize,
			@Param("userId") Long userId,
			@Param("original") Integer original,
			@Param("status") Integer status,
			@Param("quoteKeyword") String quoteKeyword
	);

	List<Quote> selectByTagIdsAndCategory (
										   @Param("tagIds")  List<Long> tagIds,
										   @Param("tagIdsSize") Integer tagIdsSize,
										   @Param("pageSize") int pageSize,
										   @Param("pageNum") int pageNum,
										   @Param("userId") Long userId,
										   @Param("original") Integer original,
										   @Param("status") Integer status,
										   @Param("quoteKeyword") String quoteKeyword
	);

	int getQuoteType(@Param("quoteId") String quoteId);

	boolean checkQuoteExists(@Param("quoteId") String quoteId);

	List<Quote> selectRandomQuotes(@Param("keyword")  String keyword, @Param("quoteCount") int quoteCount);

	QuoteStatusCountVo queryStatusCount(@Param("userId") Long userId);

	void updateQuoteStatus(@Param("quoteId")Long quoteId,@Param("code") int code);
}
