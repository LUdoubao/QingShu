package org.doubao.comment.service.vo;

/**
 * 评论计数视图对象
 * <p>
 * 用于封装文章ID与对应评论数量的映射关系
 */
public class CommentCountVo {
	/**
	 * 文章ID
	 * <p>
	 * 评论所属文章的唯一标识符
	 */
	private String postId;
	/**
	 * 评论数量
	 * <p>
	 * 该文章下的评论总数
	 */
	private Long commentCount;

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public Long getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Long commentCount) {
		this.commentCount = commentCount;
	}
}
