package org.doubao.fanout.service.model;


import org.doubao.mall.common.enums.EventType;
import org.doubao.fanout.service.enums.TargetType;

import java.util.List;

public class FanoutRule {
	// 事件类型
	private EventType messageType;

	// 目标列表
	private List<Target> targets;

	public EventType getMessageType() {
		return messageType;
	}

	public void setMessageType(EventType messageType) {
		this.messageType = messageType;
	}

	public List<Target> getTargets() {
		return targets;
	}

	public void setTargets(List<Target> targets) {
		this.targets = targets;
	}

	public static class Target {
		// 目标类型
		private TargetType type;

		// 目标名称（如队列名）
		private String name;

		// 路由键
		private String routingKey;

		// 是否需要过滤
		private boolean needFilter = false;

		public TargetType getType() {
			return type;
		}

		public void setType(TargetType type) {
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRoutingKey() {
			return routingKey;
		}

		public void setRoutingKey(String routingKey) {
			this.routingKey = routingKey;
		}

		public boolean isNeedFilter() {
			return needFilter;
		}

		public void setNeedFilter(boolean needFilter) {
			this.needFilter = needFilter;
		}
	}
}
