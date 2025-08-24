package org.doubao.user.server.relation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_relation_count")
public class UserRelationCount {
	@TableId("user_id")
	private Long userId;
	@TableField("following_count")
	private Integer followingCount;
	@TableField("follower_count")
	private Integer followerCount;
	@TableField("mutual_count")
	private Integer mutualCount;
	@TableField("updated_time")
	private LocalDateTime updatedTime;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getFollowingCount() {
		return followingCount;
	}

	public void setFollowingCount(Integer followingCount) {
		this.followingCount = followingCount;
	}

	public Integer getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(Integer followerCount) {
		this.followerCount = followerCount;
	}

	public Integer getMutualCount() {
		return mutualCount;
	}

	public void setMutualCount(Integer mutualCount) {
		this.mutualCount = mutualCount;
	}

	public LocalDateTime getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}
}