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

/**
 * 评论数据访问层接口
 * <p>
 * 定义评论相关的数据库操作方法，包括评论查询、统计等功能
 */
public interface CommentMapper extends BaseMapper<Comment> {
	/**
	 * 根据文章ID查询评论列表
	 * <p>
	 * 查询指定文章下的所有评论，按创建时间倒序排列
	 * 主要用于获取文章下的所有主评论（非回复）
	 * 
	 * @param postId 文章ID
	 * @return 评论实体列表，按创建时间倒序排列
	 */
	List<Comment> selectByPostId(@Param("postId")String postId);

	/**
	 * 查询评论的回复
	 * <p>
	 * 查询指定评论的前N条回复，按创建时间倒序排列
	 * 用于在评论详情中展示部分回复内容
	 * 
	 * @param commentId 评论ID
	 * @param count 查询回复的数量限制
	 * @return 评论回复实体列表，最多返回count条记录
	 */
	List<Comment> selectReplies(@Param("commentId") String commentId, @Param("count") int count);

	/**
	 * 根据主评论ID分页查询回复列表
	 * <p>
	 * 分页查询指定主评论下的所有回复
	 * 用于评论详情页的回复列表分页展示
	 * 
	 * @param page 分页参数，包含页码和每页大小
	 * @param rootId 主评论ID（根评论ID），用于查询该评论下的所有回复
	 * @return 分页的回复列表，包含分页信息和回复数据
	 */
	IPage<Comment> selectRepliesByCommentId(
			Page<Comment> page,
			@Param("rootId") String rootId
	);
	
	/**
	 * 根据文章ID列表查询评论数
	 * <p>
	 * 统计指定文章ID列表中每个文章的评论数量
	 * 用于批量获取文章评论数的场景
	 * 
	 * @param postIds 文章ID列表
	 * @return 文章ID与评论数量的映射列表，包含postId和commentCount字段
	 */
	List<CommentCountVo> countCommentsByPostIds(@Param("postIds") List<String> postIds);

	/**
	 * 查询指定文章在多个日期的每日有效评论数总和
	 * <p>
	 * 统计指定文章在给定日期范围内每日的评论总数
	 * 用于数据统计和报表功能
	 * 
	 * @param postIds 文章ID列表（字符串类型，匹配Comment的postId）
	 * @param dates 日期列表
	 * @return 按日期分组的统计结果列表，每个Map包含stat_date和total_count两个键
	 */
	List<Map<String, Object>> selectDailyCommentCounts(
			@Param("postIds") List<String> postIds,
			@Param("dates") List<LocalDate> dates);
}