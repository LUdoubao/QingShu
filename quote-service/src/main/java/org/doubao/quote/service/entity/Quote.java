package org.doubao.quote.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("quote")
public class Quote extends BaseEntity {
	@TableId(type = IdType.AUTO)
	private Long id;
	@TableField(value = "content")
	private String content;
	@TableField(value = "author")
	private String author;
	@TableField(value = "source")
	private String source;

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
