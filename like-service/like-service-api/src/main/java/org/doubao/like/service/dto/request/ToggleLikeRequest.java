package org.doubao.like.service.dto.request;





import javax.validation.constraints.NotNull;

/**
 * 点赞/取消点赞请求DTO
 * <p>
 * 用于处理用户对文案/评论的点赞状态切换
 */
//@ApiModel(description = "点赞操作请求参数")
public class ToggleLikeRequest {

	/**
	 * 用户ID
	 * <p>
	 * 执行点赞操作的用户唯一标识
	 * 必须为非空值
	 */
	@NotNull(message = "用户ID不能为空")
	private Long operatorUserId;

	/**
	 * 实体类型
	 * <p>
	 * 指定被点赞的实体类型：
	 * - 0: 文案
	 * - 1: 评论
	 */
	@NotNull(message = "实体类型不能为空")
	private int entityType;

	/**
	 * 实体ID
	 * <p>
	 * 被点赞的文案或评论的唯一标识
	 * 必须为正数
	 */
	@NotNull(message = "实体ID不能为空")
	private String entityId;

	/**
	 * 点赞用户ID
	 * <p>
	 * 被点赞的用户唯一标识
	 * 必须为非空值
	 */
	@NotNull(message = "用户ID不能为空")
	private Long userId;

	/**
	 * 点赞内容
	 * <p>
	 * 点赞时，指定点赞内容
	 * 默认为空
	 */
	private String content;

	public @NotNull(message = "用户ID不能为空") Long getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(@NotNull(message = "用户ID不能为空") Long operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public @NotNull(message = "用户ID不能为空") Long getUserId() {
		return userId;
	}

	public void setUserId(@NotNull(message = "用户ID不能为空") Long userId) {
		this.userId = userId;
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
}