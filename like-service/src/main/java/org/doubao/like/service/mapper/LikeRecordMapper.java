package org.doubao.like.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.like.service.dto.LikeCountDTO;
import org.doubao.like.service.dto.response.LikeCountVo;
import org.doubao.like.service.entity.LikeRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

	/**
	 * 查询指定entityIds的点赞数
	 */
	List<LikeCountVo> countLikesByEntityIds(@Param("entityIds") List<String> entityIds);

	/**
	 * 查询指定文章在多个日期的每日有效点赞数总和
	 * @param entityIds 文章ID列表（字符串类型，匹配LikeRecord的entityId）
	 * @param entityType 实体类型（固定为内容类型）
	 * @param dates 日期列表
	 * @return 按日期分组的统计结果（键：stat_date-日期，值：total_count-当日总点赞数）
	 */
	List<Map<String, Object>> selectDailyLikeCounts(
			@Param("entityIds") List<String> entityIds,
			@Param("entityType") int entityType,
			@Param("dates") List<LocalDate> dates);


}
