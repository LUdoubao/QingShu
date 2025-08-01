package org.doubao.like.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.like.service.entity.LikeCount;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LikeCountMapper  extends BaseMapper<LikeCount> {
	int updateCount(@Param("type") int entityType,
					@Param("id") String entityId,
					@Param("delta") int delta,
					@Param("oldCount") int oldCount,
					@Param("now") LocalDateTime now);

	void updateSync(@Param("likeCount") LikeCount likeCount);

	// 批量更新点赞计数
	int batchUpdateCounts(@Param("list") List<LikeCount> likeCounts);
}
