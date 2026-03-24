package org.doubao.search.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.entity.QuoteSearchSyncData;

import java.util.List;

@Mapper
public interface QuoteSearchSourceMapper {

    List<Long> selectQuoteIds(@Param("offset") int offset, @Param("limit") int limit);

    QuoteSearchSyncData selectQuoteSyncData(@Param("quoteId") Long quoteId);
}
