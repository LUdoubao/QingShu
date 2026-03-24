package org.doubao.search.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.entity.SearchIndexSyncTaskDO;

import java.util.List;

@Mapper
public interface SearchIndexSyncTaskMapper extends BaseMapper<SearchIndexSyncTaskDO> {

    List<SearchIndexSyncTaskDO> selectPendingTasks(@Param("limit") int limit);

    int markSuccess(@Param("id") Long id);

    int markFailed(@Param("id") Long id, @Param("failReason") String failReason);
}
