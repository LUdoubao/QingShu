package org.doubao.search.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.entity.SearchSuggestTermDO;

import java.util.List;

@Mapper
public interface SearchSuggestTermMapper extends BaseMapper<SearchSuggestTermDO> {

    List<SearchSuggestTermDO> selectByPrefixAndType(@Param("prefixText") String prefixText,
                                                    @Param("termType") String termType,
                                                    @Param("limit") int limit);

    int deleteBySource(@Param("sourceBizType") String sourceBizType, @Param("sourceId") Long sourceId);

    int batchInsert(@Param("terms") java.util.List<SearchSuggestTermDO> terms);
}
