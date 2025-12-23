package org.doubao.favorite.service.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.doubao.mall.common.entity.BaseDel;

import java.time.LocalDateTime;

@TableName("favorite_folder")
public class FavoriteFolder extends BaseDel {
	@TableId(type = IdType.AUTO)
	private Long id;
	private Long userId;
	private String name;
	private int isDefault = 0;
	private Integer orderNum = 0;
	/**
	 * 0:文案收藏夹
	 */
	private Integer type = 0;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public int getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(int isDefault) {
		this.isDefault = isDefault;
	}
}