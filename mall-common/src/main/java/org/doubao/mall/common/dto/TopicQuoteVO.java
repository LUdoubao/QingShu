package org.doubao.mall.common.dto;

import org.doubao.mall.common.entity.BaseEntity;
import org.doubao.mall.common.entity.UserInfoDes;

public class TopicQuoteVO extends BaseEntity {
	/**
	 * 引文id
	 */
	private Long id;
	/**
	 * 引文内容
	 */
	private String content;
	/**
	 * 引文作者
	 */
	private String author;
	/**
	 * 引文来源
	 */
	private String source;

	/**
	 * 是否原创
	 */
	private int original;

	/**
	 * 用户信息
	 */
	private UserInfoDes userInfo;

	/**
	 * 是否关注
	 */
	private boolean follow;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getOriginal() {
		return original;
	}

	public void setOriginal(int original) {
		this.original = original;
	}

	public UserInfoDes getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfoDes userInfo) {
		this.userInfo = userInfo;
	}

	public boolean isFollow() {
		return follow;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
	}
}
