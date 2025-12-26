package org.doubao.view.count.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("view_correction_log")
public class ViewCorrectionLog {
	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("content_id")
	private Long contentId;

	@TableField("before_count")
	private Long beforeCount;

	@TableField("after_count")
	private Long afterCount;

	@TableField("correction_time")
	private LocalDateTime correctionTime;

	@TableField("reason")
	private String reason;

	@TableField("operator")
	private String operator;

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

	public Long getBeforeCount() {
		return beforeCount;
	}

	public void setBeforeCount(Long beforeCount) {
		this.beforeCount = beforeCount;
	}

	public Long getAfterCount() {
		return afterCount;
	}

	public void setAfterCount(Long afterCount) {
		this.afterCount = afterCount;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDateTime getCorrectionTime() {
		return correctionTime;
	}

	public void setCorrectionTime(LocalDateTime correctionTime) {
		this.correctionTime = correctionTime;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
}