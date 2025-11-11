package org.doubao.comment.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.comment.service.dto.CommentDTO;
import org.doubao.comment.service.dto.ReplyDTO;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.entity.Comment;
import org.doubao.comment.service.vo.CommentVO;
import org.doubao.comment.service.vo.ReplyVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CommentService extends IService<Comment> {
	String createComment(CommentDTO dto);

	IPage<CommentVO> getCommentList(String postId, Page<Comment> page, String sortType);

	double calculateHotScore(Comment comment);

	void handleLike(String commentId, String action);

	String replyComment(String commentId, ReplyDTO dto);

	void handleTop(String commentId, String action);

	void deleteComment(String commentId);

	Page<ReplyVO> getRepliesByCommentId(String commentId, Integer page, Integer size);

	ToggleLikeResponse toggleLike(String commentId);

	void updateStatus(Map<String, String> request);

	Map<Long, Long> batchCounts(List<Long> contentIds);

	Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params);
}
