package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;


import java.time.LocalDateTime;

/**
 * AI知识库实体类
 * 业务说明：存储AI助手的预设知识库条目，用于快速匹配用户问题并提供标准化回复
 * 数据表：assistant_knowledge
 * 适用场景：AI助手知识库管理、常见问题自动回复、FAQ功能实现
 */
@TableName("assistant_knowledge")
public class AssistantKnowledge {

	/**
	 * 知识库ID
	 * 业务说明：知识库条目的唯一标识符，自增主键
	 * 数据类型：Long类型，自动生成
	 * 关联关系：作为其他表外键引用的基础
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 关键词
	 * 业务说明：用于匹配用户问题的关键词或短语，支持模糊匹配
	 * 匹配策略：包含关键词的用户问题将触发此条目回复
	 * 优化建议：建议使用同义词、关键词变体提高匹配准确率
	 */
	@TableField("keyword")
	private String keyword;

	/**
	 * 回复内容
	 * 业务说明：当用户问题匹配关键词时，AI助手返回的标准化回复内容
	 * 内容格式：支持文本、HTML格式，可包含链接、格式化内容
	 * 更新策略：定期根据用户反馈优化回复内容
	 */
	@TableField("content")
	private String content;

	/**
	 * 命中次数
	 * 业务说明：记录该知识库条目被匹配使用的次数，用于效果统计和优化
	 * 统计用途：分析热门问题、评估知识库效果、指导内容优化
	 * 更新时机：每次匹配成功时自动递增
	 */
	@TableField("hit_count")
	private Integer hitCount;

	/**
	 * 功能模块（如"执翎台""飞翎榜"）
	 * 业务说明：标识该知识库条目所属的功能模块，用于分类管理和模块化回复
	 * 应用场景：不同模块的AI助手使用对应模块的知识库条目
	 * 枚举值：根据系统功能模块定义（如执翎台、飞翎榜等）
	 */
	@TableField("module")
	private String module;

	/**
	 * 是否启用
	 * 业务说明：标识该知识库条目是否启用，用于动态控制知识库生效状态
	 * 枚举值：0=禁用（不参与匹配），1=启用（参与匹配）
	 * 使用场景：知识库维护、内容审核、A/B测试等
	 */
	@TableField("enabled")
	private int enabled;

	/**
	 * 创建时间
	 * 业务说明：知识库条目的创建时间，用于知识库管理时间线追踪
	 * 数据类型：LocalDateTime，精确到秒
	 * 时区处理：系统默认时区（东八区）
	 */
	@TableField("created_time")
	private LocalDateTime createdTime;

	/**
	 * 更新时间
	 * 业务说明：知识库条目的最后更新时间，用于追踪内容变更历史
	 * 数据类型：LocalDateTime，精确到秒
	 * 更新时机：每次内容、状态变更时更新
	 */
	@TableField("updated_time")
	private LocalDateTime updatedTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getHitCount() {
		return hitCount;
	}

	public void setHitCount(Integer hitCount) {
		this.hitCount = hitCount;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}
}