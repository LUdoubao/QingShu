package org.doubao.feed.service.model.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.mall.common.entity.BaseEntity;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@TableName("user_feed_filter")
public class FeedFilter extends BaseEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 用户ID
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 屏蔽的事件类型列表
	 */
	@TableField("blocked_types")
	private String blockedTypes;

	/**
	 * 临时屏蔽的关注对象ID列表
	 */
	@TableField("blocked_actors")
	private String blockedActors;


	/**
	 * 获取屏蔽的事件类型列表
	 */
	public List<Integer> getBlockedTypesList() {
		try {
			if (StringUtils.isEmpty(blockedTypes)) {
				return new ArrayList<>();
			}
			return new ObjectMapper().readValue(blockedTypes, new TypeReference<List<Integer>>() {});
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	/**
	 * 设置屏蔽的事件类型列表
	 */
	public void setBlockedTypesList(List<Integer> types) {
		try {
			this.blockedTypes = new ObjectMapper().writeValueAsString(types);
		} catch (Exception e) {
			this.blockedTypes = "[]";
		}
	}

	/**
	 * 获取屏蔽的创作者列表
	 */
	public List<Long> getBlockedActorsList() {
		try {
			if (StringUtils.isEmpty(blockedActors)) {
				return new ArrayList<>();
			}
			return new ObjectMapper().readValue(blockedActors, new TypeReference<List<Long>>() {});
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	/**
	 * 设置屏蔽的创作者列表
	 */
	public void setBlockedActorsList(List<Long> actors) {
		try {
			this.blockedActors = new ObjectMapper().writeValueAsString(actors);
		} catch (Exception e) {
			this.blockedActors = "[]";
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getBlockedActors() {
		return blockedActors;
	}

	public void setBlockedActors(String blockedActors) {
		this.blockedActors = blockedActors;
	}

	public String getBlockedTypes() {
		return blockedTypes;
	}

	public void setBlockedTypes(String blockedTypes) {
		this.blockedTypes = blockedTypes;
	}
}