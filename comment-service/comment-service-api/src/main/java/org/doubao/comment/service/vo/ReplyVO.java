package org.doubao.comment.service.vo;

import com.fasterxml.jackson.annotation.JsonFormat;


import org.doubao.mall.common.entity.UserInfoDes;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论回复的视图对象，用于向前端返回回复相关信息
 * <p>
 * 封装评论回复的详细信息，包括回复内容、用户信息、点赞状态等
 */
//@ApiModel(description = "评论回复信息")
public class ReplyVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 回复ID
	 * <p>
	 * 回复的唯一标识符，格式为"R-"前缀加时间戳
	 */
	//@ApiModelProperty(value = "回复ID", example = "R-202507241005")
	private String replyId;

	/**
	 * 回复者用户ID
	 * <p>
	 * 发表该回复的用户唯一标识符
	 */
	//@ApiModelProperty(value = "回复者用户ID", example = "1002")
	private Long userId;

	/**
	 * 回复内容
	 * <p>
	 * 回复的文本内容，可能包含对其他用户的@引用
	 */
	//@ApiModelProperty(value = "回复内容", example = "@文学爱好者 同意你的观点，这首诗确实意境深远")
	private String content;

	/**
	 * 被回复者昵称
	 * <p>
	 * 如果是回复他人评论，则显示被回复者的昵称
	 */
	//@ApiModelProperty(value = "被回复者昵称（如果是回复他人）", example = "文学爱好者")
	private String repliedNickname;

	/**
	 * 点赞数量
	 * <p>
	 * 该回复获得的点赞总数
	 */
	//@ApiModelProperty(value = "点赞数量", example = "8")
	private Long likeCount;

	/**
	 * 当前用户是否已点赞该回复
	 * <p>
	 * 标识当前访问用户是否已对该回复点赞
	 */
	//@ApiModelProperty(value = "当前用户是否已点赞该回复", example = "false")
	private Boolean isLiked = false;

	/**
	 * 回复创建时间
	 * <p>
	 * 回复发表的具体时间，格式为yyyy-MM-dd HH:mm:ss
	 */
	//@ApiModelProperty(value = "回复创建时间", example = "2025-07-24 10:05:11")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdTime;

	/**
	 * 回复是否被作者置顶
	 * <p>
	 * 标识该回复是否被文章作者置顶
	 */
	//@ApiModelProperty(value = "回复是否被作者置顶", example = "false")
	private Boolean isAuthorTop = false;

	/**
	 * 回复标签
	 * <p>
	 * 用于标记回复的特殊属性，如管理员回复等
	 */
	//@ApiModelProperty(value = "回复标签（如管理员回复）")
	private String[] tags;

	/**
	 * 回复用户信息
	 * <p>
	 * 发表该回复的用户详细信息
	 */
	private UserInfoDes user;

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	public UserInfoDes getUser() {
		return user;
	}

	public void setUser(UserInfoDes user) {
		this.user = user;
	}

	public String getReplyId() {
		return replyId;
	}

	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRepliedNickname() {
		return repliedNickname;
	}

	public void setRepliedNickname(String repliedNickname) {
		this.repliedNickname = repliedNickname;
	}

	public Boolean getLiked() {
		return isLiked;
	}

	public void setLiked(Boolean liked) {
		isLiked = liked;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public Boolean getAuthorTop() {
		return isAuthorTop;
	}

	public void setAuthorTop(Boolean authorTop) {
		isAuthorTop = authorTop;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}
}
