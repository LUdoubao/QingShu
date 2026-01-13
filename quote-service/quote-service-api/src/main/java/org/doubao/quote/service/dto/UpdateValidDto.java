package org.doubao.quote.service.dto;

import java.io.Serializable;

public class UpdateValidDto implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long actorId;

	private Long targetId;

	private String targetType;

	private Integer isValid = 0;


	public Long getActorId() {
		return actorId;
	}

	public void setActorId(Long actorId) {
		this.actorId = actorId;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
	}
}
