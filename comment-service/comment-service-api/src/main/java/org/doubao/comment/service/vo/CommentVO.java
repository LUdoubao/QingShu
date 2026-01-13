package org.doubao.comment.service.vo;

import org.doubao.comment.service.entity.Comment;
import org.doubao.mall.common.entity.UserInfoDes;

import java.util.List;

/**
 * 评论视图对象
 * <p>
 * 继承自评论实体，扩展了热度分数、点赞状态、用户信息和回复列表等前端展示所需字段
 */
public class CommentVO extends Comment {
	/**
	 * 热度分数
	 * <p>
	 * 通过点赞数、回复数等计算出的评论热度值，用于热度排序
	 */
	private double hotScore;
	/**
	 * 是否已点赞
	 * <p>
	 * 标识当前用户是否已对该评论点赞
	 */
	private boolean isLike;
	/**
	 * 评论用户信息
	 * <p>
	 * 评论作者的详细信息，包括昵称、头像等
	 */
	private UserInfoDes user;
	/**
	 * 回复列表
	 * <p>
	 * 该评论下的前几条回复，用于前端展示
	 */
	private List<ReplyVO> replyList;
	public double getHotScore() {
		return hotScore;
	}

	public void setHotScore(double hotScore) {
		this.hotScore = hotScore;
	}

	public UserInfoDes getUser() {
		return user;
	}

	public void setUser(UserInfoDes user) {
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
