package org.doubao.recommend.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.recommend.service.domain.ContentFeature;

import java.util.List;

@Mapper
public interface RecommendQuoteMapper {

    List<ContentFeature> selectFeaturesByIds(@Param("ids") List<Long> ids);

    ContentFeature selectFeatureById(@Param("id") Long id);

    List<ContentFeature> selectRecentPublished(@Param("author") String author, @Param("limit") int limit);

    List<ContentFeature> selectRecentPublishedBeforeId(@Param("author") String author, @Param("maxContentId") Long maxContentId, @Param("limit") int limit);

    List<ContentFeature> selectByAuthorId(@Param("authorId") Long authorId, @Param("limit") int limit);

    List<ContentFeature> selectByDynasty(@Param("dynasty") String dynasty, @Param("limit") int limit);

    List<ContentFeature> selectByTags(@Param("tags") List<String> tags, @Param("limit") int limit);

    List<ContentFeature> selectByTopicIds(@Param("topicIds") List<Long> topicIds, @Param("limit") int limit);

    List<ContentFeature> selectByAuthors(@Param("authors") List<String> authors, @Param("limit") int limit);

    List<Long> selectValidIds(@Param("ids") List<Long> ids);
}
