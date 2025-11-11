package org.doubao.comment.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.doubao.comment.service.entity.Comment;
import org.doubao.comment.service.vo.CommentCountVo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CommentMapper extends BaseMapper<Comment> {
	List<Comment> selectByPostId(@Param("postId")String postId);

	List<Comment> selectReplies(@Param("commentId") String commentId, @Param("count") int count);

	/**
	 * 根据主评论ID分页查询回复列表
	 * @param page 分页参数
	 * @param rootId 主评论ID（根评论ID）
	 * @return 分页的回复列表
	 */
	IPage<Comment> selectRepliesByCommentId(
			Page<Comment> page,
			@Param("rootId") String rootId
	);
	/**
	 * 根据postIds查询评论数
	 */
	List<CommentCountVo> countCommentsByPostIds(@Param("postIds") List<String> postIds);

	/**
	 * 查询指定文章在多个日期的每日有效评论数总和
	 * @param postIds 文章ID列表（字符串类型，匹配Comment的postId）
	 * @param dates 日期列表
	 * @return 按日期分组的统计结果（键：stat_date-日期，值：total_count-当日总评论数）
	 */
	List<Map<String, Object>> selectDailyCommentCounts(
			@Param("postIds") List<String> postIds,
			@Param("dates") List<LocalDate> dates);
}
