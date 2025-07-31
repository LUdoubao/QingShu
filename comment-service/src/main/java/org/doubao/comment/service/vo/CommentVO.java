package org.doubao.comment.service.vo;

import org.doubao.comment.service.entity.Comment;
import org.doubao.mall.common.entity.UserInfo;

import java.util.List;

public class CommentVO extends Comment {
	private double hotScore;
	private boolean isLike;
	private UserInfo user;
	private List<ReplyVO> replyList;
	public double getHotScore() {
		return hotScore;
	}

	public void setHotScore(double hotScore) {
		this.hotScore = hotScore;
	}

	public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}

	public boolean isLike() {
		return isLike;
	}

	public void setLike(boolean like) {
		isLike = like;
	}

	public List<ReplyVO> getReplyList() {
		return replyList;
	}

	public void setReplyList(List<ReplyVO> replyList) {
		this.replyList = replyList;
	}
}
