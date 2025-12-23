package org.doubao.quote.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.quote.service.entity.QuoteTag;
import org.doubao.quote.service.entity.Tag;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuoteTagMapper extends BaseMapper<QuoteTag> {
	void insertBatch(List<QuoteTag> quoteTags);

	List<Map<String, Object>> selectQuoteTagsWithDetails(@Param("quoteIds") List<Long> quoteIds);

	void deleteByQuoteId(@Param("id") Long id);
}