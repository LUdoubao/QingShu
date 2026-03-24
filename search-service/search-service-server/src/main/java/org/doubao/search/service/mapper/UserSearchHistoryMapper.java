package org.doubao.search.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.entity.UserSearchHistoryDO;

import java.util.List;

@Mapper
public interface UserSearchHistoryMapper extends BaseMapper<UserSearchHistoryDO> {

    int upsertHistory(@Param("userId") Long userId,
                      @Param("queryText") String queryText,
                      @Param("normalizedQuery") String normalizedQuery);

    List<UserSearchHistoryDO> selectUserHistory(@Param("userId") Long userId,
                                                @Param("limit") int limit);

    int clearUserHistory(@Param("userId") Long userId);
}
