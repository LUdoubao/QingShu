package org.doubao.comment.service.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;

/**
 * 评论实体类
 * <p>
 * 用于映射数据库中的comments表，包含评论的基本信息、关联关系和状态
 */
@TableName("comments")
public class Comment extends BaseDel {
	/**
	 * 评论唯一标识符
	 * <p>
	 * 采用"C-"或"R-"前缀加时间戳和随机数的格式，区分主评论和回复
	 */
	@TableId
	private String commentId;
	/**
	 * 关联文章ID
	 * <p>
	 * 评论所属文章或引文的ID，用于关联查询和统计
	 */
	private String postId;
	/**
	 * 评论作者用户ID
	 * <p>
	 * 发表该评论的用户唯一标识符
	 */
	private Long userId;
	/**
	 * 被回复用户ID
	 * <p>
	 * 在回复评论时，被回复的用户ID，用于发送通知
	 */
	private Long repliedUserId;
	/**
	 * 评论内容
	 * <p>
	 * 评论的文本内容，经过字数和敏感词校验
	 */
	private String content;
	/**
	 * 父评论ID
	 * <p>
	 * 如果是回复评论，则指向被回复的评论ID；如果是主评论，则为null
	 */
	private String parentId;
	/**
	 * 根评论ID
	 * <p>
	 * 用于标识同一评论树的根节点，方便按评论树查询和统计
	 */
	private String rootId;
	/**
	 * 点赞数量
	 * <p>
	 * 该评论获得的点赞总数，冗余字段用于提高查询效率
	 */
	private Long likeCount;
	/**
	 * 回复数量
	 * <p>
	 * 该评论下的回复总数，冗余字段用于提高查询效率
	 */
	private Long replyCount;
	/**
	 * 评论状态
	 * <p>
	 * 0-正常，1-折叠，2-删除，用于评论的生命周期管理
	 */
	private Integer status;
	/**
	 * 评论标签
	 * <p>
	 * 用于标记评论的特殊属性，如官方回复、管理员回复等
	 */
	private String tags;
	/**
	 * 是否置顶
	 * <p>
	 * 0-未置顶，1-已置顶，用于标识评论的显示优先级
	 */
	private Integer isTop;

	public Long getRepliedUserId() {
		return repliedUserId;
	}

	public void setRepliedUserId(Long repliedUserId) {
		this.repliedUserId = repliedUserId;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRootId() {
		return rootId;
	}

	public void setRootId(String rootId) {
		this.rootId = rootId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Integer getIsTop() {
		return isTop;
	}

	public void setIsTop(Integer isTop) {
		this.isTop = isTop;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	public Long getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(Long replyCount) {
		this.replyCount = replyCount;
	}
}