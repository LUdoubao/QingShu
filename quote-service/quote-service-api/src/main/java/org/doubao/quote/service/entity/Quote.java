package org.doubao.quote.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;

@TableName("quote")
public class Quote extends BaseDel {
	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;
	/**
	 * 内容
	 */
	@TableField(value = "content")
	private String content;
	/**
	 * 标题
	 */
	@TableField(value = "title")
	private String title;
	/**
	 * 作者
	 */
	@TableField(value = "author")
	private String author;
	/**
	 * 来源
	 */
	@TableField(value = "source")
	private String source;
	/**
	 * 状态，0:审核中1:已发布 2:屏蔽 3:草稿 4:未通过 5:下架
	 */
	@TableField("status")
	private int status;
	/**
	 * 是否原创，0:否 1:是
	 */
	@TableField("original")
	private int original;
	/**
	 * 诗词原创id
	 */
	@TableField("poetry_original_id")
	private String poetryOriginalId;
	/**
	 * 朝代
	 */
	@TableField("dynasty")
	private String dynasty;
	/**
	 * 诗词类别
	 */
	@TableField("poetry_category")
	private String poetryCategory;
	/**
	 * 作者id
	 */
	@TableField("author_id")
	private String authorId;

	public String getDynasty() {
		return dynasty;
	}

	public void setDynasty(String dynasty) {
		this.dynasty = dynasty;
	}

	public String getPoetryCategory() {
		return poetryCategory;
	}

	public void setPoetryCategory(String poetryCategory) {
		this.poetryCategory = poetryCategory;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public String getPoetryOriginalId() {
		return poetryOriginalId;
	}

	public void setPoetryOriginalId(String poetryOriginalId) {
		this.poetryOriginalId = poetryOriginalId;
	}

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
