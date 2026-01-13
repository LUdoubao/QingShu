package org.doubao.view.count.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("content_view")
public class ContentView {
	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("content_id")
	private Long contentId;

	@TableField("view_count")
	private Long viewCount;

	@TableField("yesterday_count")
	private Integer yesterdayCount;

	@TableField("today_count")
	private Integer todayCount;

	@TableField(value = "created_time", fill = FieldFill.INSERT)
	private LocalDateTime createdTime;
	@TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updatedTime;

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public LocalDateTime getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getContentId() {
		return contentId;
	}

	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}

	public Long getViewCount() {
		return viewCount;
	}

	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}

	public Integer getYesterdayCount() {
		return yesterdayCount;
	}

	public void setYesterdayCount(Integer yesterdayCount) {
		this.yesterdayCount = yesterdayCount;
	}

	public Integer getTodayCount() {
		return todayCount;
	}

	public void setTodayCount(Integer todayCount) {
		this.todayCount = todayCount;
	}
}