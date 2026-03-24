package org.doubao.search.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.entity.SearchDocIndexDO;

import java.util.List;

@Mapper
public interface SearchDocIndexMapper extends BaseMapper<SearchDocIndexDO> {

    int upsert(SearchDocIndexDO doc);

    int batchUpsert(@Param("docs") List<SearchDocIndexDO> docs);

    List<SearchDocIndexDO> selectByBizIds(@Param("bizType") String bizType, @Param("bizIds") List<Long> bizIds);

    List<SearchDocIndexDO> selectExactMatches(@Param("bizType") String bizType,
                                              @Param("normalizedQuery") String normalizedQuery,
                                              @Param("limit") int limit);

    List<SearchDocIndexDO> selectLikeMatches(@Param("bizType") String bizType,
                                             @Param("normalizedQuery") String normalizedQuery,
                                             @Param("limit") int limit);
}
