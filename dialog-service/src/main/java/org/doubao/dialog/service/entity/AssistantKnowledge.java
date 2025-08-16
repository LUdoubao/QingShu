package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI知识库实体类
 */
@TableName("assistant_knowledge")
public class AssistantKnowledge {

	/**
	 * 知识库ID
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 关键词
	 */
	@TableField("keyword")
	private String keyword;

	/**
	 * 回复内容
	 */
	@TableField("content")
	private String content;

	/**
	 * 命中次数
	 */
	@TableField("hit_count")
	private Integer hitCount;

	/**
	 * 功能模块（如“执翎台”“飞翎榜”）
	 */
	@TableField("module")
	private String module;

	/**
	 * 是否启用
	 */
	@TableField("enabled")
	private int enabled;

	/**
	 * 创建时间
	 */
	@TableField("created_time")
	private LocalDateTime createdTime;

	/**
	 * 更新时间
	 */
	@TableField("updated_time")
	private LocalDateTime updatedTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getHitCount() {
		return hitCount;
	}

	public void setHitCount(Integer hitCount) {
		this.hitCount = hitCount;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}
}