package org.doubao.like.service.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.like.service.dto.LikeCountDTO;
import org.doubao.like.service.entity.LikeRecord;

import java.util.List;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
	List<LikeRecord> selectByUserAndEntities(
			@Param("userId") Long userId,
			@Param("entityType") Integer entityType,
			@Param("entityIds") List<Long> entityIds
	);

	List<LikeCountDTO> countByEntities(
			@Param("entityType") Integer entityType,
			@Param("entityIds") List<Long> entityIds
	);

	void insertOrUpdate(@Param("likeRecord") LikeRecord likeRecord);
}
