package org.doubao.search.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.model.entity.SearchHistory;

import java.util.List;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
    List<SearchHistory> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    void deleteByUserId(@Param("userId") Long userId);
}
