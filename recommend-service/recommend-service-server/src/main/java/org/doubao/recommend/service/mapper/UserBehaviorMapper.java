package org.doubao.recommend.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.recommend.service.domain.HotStat;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserBehaviorMapper {

    int insert(@Param("userIdentity") String userIdentity,
               @Param("userId") Long userId,
               @Param("contentId") Long contentId,
               @Param("scene") String scene,
               @Param("actionType") String actionType,
               @Param("actionValue") Integer actionValue,
               @Param("duration") Integer duration,
               @Param("extraJson") String extraJson,
               @Param("createdTime") LocalDateTime createdTime);

    List<Long> selectRecentContentIds(@Param("userIdentity") String userIdentity, @Param("limit") int limit);

    List<HotStat> selectHotStats(@Param("since") LocalDateTime since, @Param("limit") int limit);

    Long countRecentEvents(@Param("userIdentity") String userIdentity, @Param("since") LocalDateTime since);

    List<String> selectActiveUsers(@Param("since") LocalDateTime since, @Param("limit") int limit);
}
