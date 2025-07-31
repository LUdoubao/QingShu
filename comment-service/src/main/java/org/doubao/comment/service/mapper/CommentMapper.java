package org.doubao.comment.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.doubao.comment.service.entity.Comment;

import java.util.List;

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
}
