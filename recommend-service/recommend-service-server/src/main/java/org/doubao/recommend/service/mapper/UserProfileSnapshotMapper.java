package org.doubao.recommend.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.recommend.service.domain.UserProfileSnapshotRow;

@Mapper
public interface UserProfileSnapshotMapper {

    UserProfileSnapshotRow selectByUserIdentity(@Param("userIdentity") String userIdentity);

    int upsert(@Param("userIdentity") String userIdentity,
               @Param("userId") Long userId,
               @Param("tagProfile") String tagProfile,
               @Param("topicProfile") String topicProfile,
               @Param("authorProfile") String authorProfile,
               @Param("dynastyProfile") String dynastyProfile,
               @Param("categoryProfile") String categoryProfile,
               @Param("recentContentIds") String recentContentIds,
               @Param("lastActiveTime") java.time.LocalDateTime lastActiveTime);
}
