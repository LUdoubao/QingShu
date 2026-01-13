package org.doubao.comment.service.vo;

import org.doubao.mall.common.entity.BaseEntity;

/**
 * 引文视图对象
 * <p>
 * 用于封装引文的详细信息，包括内容、作者、来源和分类等
 */
public class QuoteVo extends BaseEntity {
	/**
	 * 引文ID
	 * <p>
	 * 引文的唯一标识符
	 */
	private Long id;
	/**
	 * 引文内容
	 * <p>
	 * 引文的文本内容
	 */
	private String content;
	/**
	 * 引文作者
	 * <p>
	 * 引文的作者姓名
	 */
	private String author;
	/**
	 * 引文来源
	 * <p>
	 * 引文的出处或来源信息
	 */
	private String source;
	/**
	 * 分类名称
	 * <p>
	 * 引文所属分类的名称
	 */
	private String categoryName;
	/**
	 * 分类ID
	 * <p>
	 * 引文所属分类的唯一标识符
	 */
	private Long categoryId;

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

}
