package org.doubao.like.service.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * 批量获取点赞状态请求DTO
 * <p>
 * 用于客户端批量查询多个实体的点赞状态和计数
 */
@ApiModel(description = "批量获取点赞状态请求参数")
public class BatchLikeStatusRequest {

	/**
	 * 用户ID
	 * <p>
	 * 用于检查该用户是否已点赞指定实体
	 * 必须为非空值
	 */
	@NotNull(message = "用户ID不能为空")
	@ApiModelProperty(value = "用户ID", required = true, example = "123456")
	private Long userId;

	private boolean queryStatus = true;
	private boolean queryCount = true;

	/**
	 * 待查询的实体列表
	 * <p>
	 * 每个元素包含：
	 * - entityType: 实体类型（CONTENT/COMMENT）
	 * - entityId: 实体ID
	 * <p>
	 * 列表不能为空且每个元素必须有效
	 */
	@Valid
	@NotNull(message = "实体列表不能为空")
	@ApiModelProperty(
			value = "待查询的实体列表",
			required = true,
			example = "[{\"entityType\":\"CONTENT\",\"entityId\":1},{\"entityType\":\"COMMENT\",\"entityId\":2}]"
	)
	private List<EntityRequest> entities;

	public @NotNull(message = "用户ID不能为空") Long getUserId() {
		return userId;
	}

	public void setUserId(@NotNull(message = "用户ID不能为空") Long userId) {
		this.userId = userId;
	}

	public @Valid @NotNull(message = "实体列表不能为空") List<EntityRequest> getEntities() {
		return entities;
	}

	public void setEntities(@Valid @NotNull(message = "实体列表不能为空") List<EntityRequest> entities) {
		this.entities = entities;
	}

	public boolean isQueryStatus() {
		return queryStatus;
	}

	public void setQueryStatus(boolean queryStatus) {
		this.queryStatus = queryStatus;
	}

	public boolean isQueryCount() {
		return queryCount;
	}

	public void setQueryCount(boolean queryCount) {
		this.queryCount = queryCount;
	}

	/**
	 * 单个实体查询参数
	 */
	@ApiModel(description = "单个实体查询参数")
	public static class EntityRequest {

		/**
		 * 实体类型枚举
		 * <p>
		 * 可选值：
		 * - 0: 文案
		 * - 1: 评论
		 */
		@NotNull(message = "实体类型不能为空")
		@ApiModelProperty(
				value = "实体类型",
				required = true,
				allowableValues = "CONTENT, COMMENT",
				example = "0"
		)
		private int entityType;

		/**
		 * 实体ID
		 * <p>
		 * 对应文案或评论的唯一标识
		 * 必须为正数
		 */
		@NotNull(message = "实体ID不能为空")
		@ApiModelProperty(value = "实体ID", required = true, example = "1")
		private String entityId;

		public EntityRequest() {

		}

		@NotNull(message = "实体类型不能为空")
		public int getEntityType() {
			return entityType;
		}

		public void setEntityType(@NotNull(message = "实体类型不能为空") int entityType) {
			this.entityType = entityType;
		}

		public @NotNull(message = "实体ID不能为空") String getEntityId() {
			return entityId;
		}

		public void setEntityId(@NotNull(message = "实体ID不能为空") String entityId) {
			this.entityId = entityId;
		}

		public EntityRequest(int entityType, String entityId) {
			this.entityType = entityType;
			this.entityId = entityId;
		}
	}

	/**
	 * 快速构建方法
	 *
	 * @param userId 用户ID
	 * @param entityType 实体类型
	 * @param entityId 实体ID
	 * @return 包含单个实体的请求对象
	 */
	public static BatchLikeStatusRequest ofSingle(
			Long userId,
			int entityType,
			String entityId
	) {
		BatchLikeStatusRequest request = new BatchLikeStatusRequest();
		request.setUserId(userId);

		EntityRequest entity = new EntityRequest();
		entity.setEntityType(entityType);
		entity.setEntityId(entityId);

		request.setEntities(Collections.singletonList(entity));
		return request;
	}
}