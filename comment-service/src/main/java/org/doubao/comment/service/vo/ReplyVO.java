package org.doubao.comment.service.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.doubao.mall.common.entity.UserInfoDes;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论回复的视图对象，用于向前端返回回复相关信息
 */
@ApiModel(description = "评论回复信息")
public class ReplyVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "回复ID", example = "R-202507241005")
	private String replyId;

	@ApiModelProperty(value = "回复者用户ID", example = "1002")
	private Long userId;

	@ApiModelProperty(value = "回复内容", example = "@文学爱好者 同意你的观点，这首诗确实意境深远")
	private String content;

	@ApiModelProperty(value = "被回复者昵称（如果是回复他人）", example = "文学爱好者")
	private String repliedNickname;

	@ApiModelProperty(value = "点赞数量", example = "8")
	private Long likeCount;

	@ApiModelProperty(value = "当前用户是否已点赞该回复", example = "false")
	private Boolean isLiked = false;

	@ApiModelProperty(value = "回复创建时间", example = "2025-07-24 10:05:11")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createdTime;

	@ApiModelProperty(value = "回复是否被作者置顶", example = "false")
	private Boolean isAuthorTop = false;

	@ApiModelProperty(value = "回复标签（如管理员回复）")
	private String[] tags;

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
