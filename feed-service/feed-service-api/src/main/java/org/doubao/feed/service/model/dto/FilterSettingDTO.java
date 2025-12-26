package org.doubao.feed.service.model.dto;


import java.util.List;

public class FilterSettingDTO {
	/**
	 * 屏蔽的事件类型列表
	 */
	private List<Integer> blockedTypes;

	/**
	 * 屏蔽的创作者ID列表
	 */
	private List<Long> blockedActors;

	public List<Integer> getBlockedTypes() {
		return blockedTypes;
	}

	public void setBlockedTypes(List<Integer> blockedTypes) {
		this.blockedTypes = blockedTypes;
	}

	public List<Long> getBlockedActors() {
		return blockedActors;
	}

	public void setBlockedActors(List<Long> blockedActors) {
		this.blockedActors = blockedActors;
	}
}