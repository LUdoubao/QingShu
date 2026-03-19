package org.doubao.recommend.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.recommend.service.domain.ContentFeature;

@Mapper
public interface ContentFeatureSnapshotMapper {

    ContentFeature selectById(@Param("contentId") Long contentId);

    int upsert(ContentFeature feature);
}
