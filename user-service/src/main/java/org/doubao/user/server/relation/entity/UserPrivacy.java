package org.doubao.user.server.relation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户隐私设置实体类（对应user_privacy表）
 */
@TableName("user_privacy")
public class UserPrivacy {
	@TableId(type = IdType.AUTO)
	private Long id; // 主键ID
	@TableField("user_id")
	private Long userId; // 用户ID（唯一）
	@TableField("follower_visibility")
	private Integer followerVisibility; // 粉丝列表可见性：1=公开，2=仅互关，3=私密
	@TableField("following_visibility")
	private Integer followingVisibility; // 关注列表可见性：1=公开，2=仅互关，3=私密
	@TableField("profile_visibility")
	private Integer profileVisibility;// 个人资料可见性：1=公开，2=仅互关, 3=私密
	@TableField("work_visibility")
	private Integer workVisibility;
	@TableField("created_time")
	private LocalDateTime createdTime; // 创建时间
	@TableField("updated_time")
	private LocalDateTime updatedTime; // 更新时间

	public Integer getWorkVisibility() {
		return workVisibility;
	}

	public void setWorkVisibility(Integer workVisibility) {
		this.workVisibility = workVisibility;
	}

	public Integer getProfileVisibility() {
		return profileVisibility;
	}

	public void setProfileVisibility(Integer profileVisibility) {
		this.profileVisibility = profileVisibility;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getFollowerVisibility() {
		return followerVisibility;
	}

	public void setFollowerVisibility(Integer followerVisibility) {
		this.followerVisibility = followerVisibility;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

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

	public Integer getFollowingVisibility() {
		return followingVisibility;
	}

	public void setFollowingVisibility(Integer followingVisibility) {
		this.followingVisibility = followingVisibility;
	}
}