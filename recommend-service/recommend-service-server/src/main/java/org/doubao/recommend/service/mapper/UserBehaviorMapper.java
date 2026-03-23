package org.doubao.recommend.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.recommend.service.domain.BehaviorEvent;
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
    
    /**
     * 查询指定用户在指定时间之后的行为记录
     * 
     * @param userIdentity 用户身份标识
     * @param since 起始时间
     * @return 行为记录列表
     */
    List<BehaviorEvent> selectRecentBehaviors(@Param("userIdentity") String userIdentity, 
                                              @Param("since") LocalDateTime since);
}
