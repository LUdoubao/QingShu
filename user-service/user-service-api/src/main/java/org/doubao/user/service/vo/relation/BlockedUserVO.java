package org.doubao.user.service.vo.relation;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 黑名单用户信息VO
 */
@ApiModel("黑名单用户信息")
public class BlockedUserVO {
	@ApiModelProperty("用户ID")
	private Long id;

	@ApiModelProperty("用户昵称")
	private String nickname;

	@ApiModelProperty("用户头像URL")
	private String avatarUrl;

	@ApiModelProperty("拉黑时间")
	private String blockTime;

	@ApiModelProperty("存储类型")
	private String storageType;

	public String getStorageType() {
		return storageType;
	}

	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getBlockTime() {
		return blockTime;
	}

	public void setBlockTime(String blockTime) {
		this.blockTime = blockTime;
	}
}

