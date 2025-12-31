package org.doubao.comment.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

/**
 * 评论数据传输对象
 * <p>
 * 用于封装评论创建和回复操作的请求参数
 * 包含评论内容、关联文章ID、父评论ID等基本信息
 */
public class CommentDTO {
	/**
	 * 评论内容
	 * <p>
	 * 评论的文本内容，经过字数和敏感词校验
	 */
	private String content;
	/**
	 * 关联文章ID
	 * <p>
	 * 评论所属的文章或引文的唯一标识符
	 */
	private String postId;
	/**
	 * 父评论ID
	 * <p>
	 * 如果是回复评论，则指向被回复的评论ID；如果是主评论，则为null
	 */
	private String parentId;
	/**
	 * 被回复用户ID
	 * <p>
	 * 在回复评论时，指定被回复的用户ID，用于发送通知
	 */
	private Long repliedUserId;

	public Long getRepliedUserId() {
		return repliedUserId;
	}

	public void setRepliedUserId(Long repliedUserId) {
		this.repliedUserId = repliedUserId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * 判断是否为回复操作
	 * <p>
	 * 通过检查parentId是否存在来判断当前操作是否为回复评论
	 * 
	 * @return true表示是回复操作，false表示是主评论
	 */
	@JsonIgnore
	public boolean isReply() {
		return StringUtils.hasText(parentId);
	}
}
