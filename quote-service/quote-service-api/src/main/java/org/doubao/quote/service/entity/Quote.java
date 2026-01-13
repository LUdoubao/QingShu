package org.doubao.quote.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;

@TableName("quote")
public class Quote extends BaseDel {
	@TableId(type = IdType.AUTO)
	private Long id;
	@TableField(value = "content")
	private String content;
	@TableField(value = "title")
	private String title;
	@TableField(value = "author")
	private String author;
	@TableField(value = "source")
	private String source;
	@TableField("category_id")
	private Long categoryId;
	/**
	 * 状态，0:审核中1:已发布 2:屏蔽 3:草稿 4:未通过 5:下架
	 */
	@TableField("status")
	private int status;
	@TableField("original")
	private int original;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public int getOriginal() {
		return original;
	}

	public void setOriginal(int original) {
		this.original = original;
	}
}
