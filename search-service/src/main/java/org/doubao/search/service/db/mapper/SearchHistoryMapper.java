package org.doubao.search.service.db.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.db.entity.SearchHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
    /**
     * 获取用户的搜索历史
     */
    List<SearchHistory> getUserSearchHistory(
            @Param("userId") Long userId, 
            @Param("limit") int limit);
    

}
