package org.doubao.search.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.entity.SearchCandidateDO;
import org.doubao.search.service.entity.SearchTermIndexDO;

import java.util.List;

@Mapper
public interface SearchTermIndexMapper extends BaseMapper<SearchTermIndexDO> {

    int deleteByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);

    int deleteByBizIds(@Param("bizType") String bizType, @Param("bizIds") List<Long> bizIds);

    int batchInsert(@Param("terms") List<SearchTermIndexDO> terms);

    List<SearchCandidateDO> selectByTerms(@Param("bizType") String bizType,
                                          @Param("terms") List<String> terms,
                                          @Param("limit") int limit);

    List<SearchCandidateDO> selectByPrefix(@Param("bizType") String bizType,
                                           @Param("prefix") String prefix,
                                           @Param("limit") int limit);
}
