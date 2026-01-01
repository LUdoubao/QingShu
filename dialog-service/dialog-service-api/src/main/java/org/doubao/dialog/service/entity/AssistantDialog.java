package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 对话主表实体类
 * 业务说明：存储用户与AI助手或管理员的对话记录，包含对话状态、标题、创建时间等信息
 * 数据表：assistant_dialog
 * 适用场景：AI助手对话管理、客服对话跟踪、对话状态维护
 */
@TableName("assistant_dialog")
public class AssistantDialog {

	/**
	 * 对话ID
	 * 业务说明：对话记录的唯一标识符，自增主键
	 * 数据类型：Long类型，自动生成
	 * 关联关系：作为外键被assistant_message表引用
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 用户ID（未登录为NULL）
	 * 业务说明：发起对话的用户ID，未登录用户对话此字段为NULL
	 * 数据类型：Long类型，外键关联user表
	 * 使用场景：用户对话历史查询、个性化服务提供
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 状态：0-活跃 1-已解决 2-待跟进
	 * 业务说明：标识对话的当前处理状态，用于对话管理和状态跟踪
	 * 枚举值：0=ACTIVE（活跃对话，等待处理）1=RESOLVED（已解决，无需跟进）2=PENDING（待跟进，需要后续处理）
	 * 使用场景：对话列表状态筛选、对话处理流程控制
	 */
	@TableField("status")
	private Integer status;

	/**
	 * 标题
	 * 业务说明：对话的标题或主题，用于快速识别对话内容
	 * 生成方式：AI根据对话内容自动生成或管理员手动设置
	 * 显示位置：对话列表、对话详情页等
	 */
	@TableField("title")
	private String title;
	/**
	 * 创建时间
	 * 业务说明：对话记录的创建时间，用于对话排序和历史查询
	 * 数据类型：LocalDateTime，精确到秒
	 * 时区处理：系统默认时区（东八区）
	 */
	@TableField("created_time")
	private LocalDateTime createdTime;

	/**
	 * 更新时间
	 * 业务说明：对话记录的最后更新时间，用于跟踪对话活动状态
	 * 数据类型：LocalDateTime，精确到秒
	 * 更新时机：每次对话状态变更或新消息添加时更新
	 */
	@TableField("updated_time")
	private LocalDateTime updatedTime;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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
}