package org.doubao.quote.service.vo;

import org.doubao.mall.common.vo.TopicNameVo;
import org.doubao.mall.common.entity.BaseEntity;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.quote.service.entity.Tag;

import java.util.List;

public class QuoteVo extends BaseEntity {
	/**
	 * 引文id
	 */
	private Long id;
	/**
	 * 引文内容
	 */
	private String content;
	/**
	 * 引文内容繁体
	 */
	private String contentTraditional;
	/**
	 * 引文作者
	 */
	private String author;
	/**
	 * 引文来源
	 */
	private String source;
	/**
	 * 分类名称
	 */
	private String categoryName;
	/**
	 * 分类id
	 */
	private Long categoryId;
	/**
	 * 标签列表
	 */
	private List<Tag> tags;
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

	/**
	 * 状态
	 */
	private Integer status;
	/**
	 * 朝代
	 */
	private String dynasty;

	/**
	 * 标题
	 */
	private String title;

	private TopicNameVo topic;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContentTraditional() {
		return contentTraditional;
	}

	public void setContentTraditional(String contentTraditional) {
		this.contentTraditional = contentTraditional;
	}

	public String getDynasty() {
		return dynasty;
	}

	public void setDynasty(String dynasty) {
		this.dynasty = dynasty;
	}

	public TopicNameVo getTopic() {
		return topic;
	}

	public void setTopic(TopicNameVo topic) {
		this.topic = topic;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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

	public int getOriginal() {
		return original;
	}

	public void setOriginal(int original) {
		this.original = original;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

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

	@Override
	public String toString() {
		return "QuoteVo{" +
				"id=" + id +
				", content='" + content + '\'' +
				", author='" + author + '\'' +
				", source='" + source + '\'' +
				", categoryName='" + categoryName + '\'' +
				", categoryId=" + categoryId +
				", tags=" + tags +
				'}';
	}
}
