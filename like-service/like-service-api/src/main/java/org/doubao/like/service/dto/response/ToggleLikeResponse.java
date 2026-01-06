package org.doubao.like.service.dto.response;




/**
 * 点赞操作响应DTO
 * <p>
 * 包含点赞/取消点赞操作的结果状态和最新数据
 */
//@ApiModel(description = "点赞操作响应结果")
public class ToggleLikeResponse {

	/**
	 * 操作是否成功
	 * <p>
	 * true: 操作已成功执行
	 * false: 操作失败（需结合错误信息处理）
	 */

	private Boolean success;

	/**
	 * 执行动作类型
	 * <p>
	 * 可能的取值：
	 * - "like": 表示执行的是点赞操作
	 * - "cancel": 表示执行的是取消点赞操作
	 */
	private String action;

	/**
	 * 当前点赞总数
	 * <p>
	 * 操作完成后该实体的最新点赞数
	 */
	private Long currentCount;

	/**
	 * 附加消息
	 * <p>
	 * 用于传递额外的操作信息（如错误原因等）
	 */
	private String message;

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(Long currentCount) {
		this.currentCount = currentCount;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 成功响应构建方法
	 *
	 * @param action 动作类型（like/cancel）
	 * @param count 最新点赞数
	 * @return 构建好的响应对象
	 */
	public static ToggleLikeResponse success(String action, long count) {
		ToggleLikeResponse response = new ToggleLikeResponse();
		response.setSuccess(true);
		response.setAction(action);
		response.setCurrentCount(count);
		response.setMessage("操作成功");
		return response;
	}

	/**
	 * 失败响应构建方法
	 *
	 * @param message 错误信息
	 * @return 构建好的响应对象
	 */
	public static ToggleLikeResponse fail(String message) {
		ToggleLikeResponse response = new ToggleLikeResponse();
		response.setSuccess(false);
		response.setAction("none");
		response.setCurrentCount(0L);
		response.setMessage(message);
		return response;
	}

	/**
	 * 点赞成功响应快捷方法
	 *
	 * @param count 最新点赞数
	 * @return 构建好的响应对象
	 */
	public static ToggleLikeResponse likeSuccess(Integer count) {
		return success("like", count);
	}

	/**
	 * 取消点赞成功响应快捷方法
	 *
	 * @param count 最新点赞数
	 * @return 构建好的响应对象
	 */
	public static ToggleLikeResponse cancelSuccess(Integer count) {
		return success("cancel", count);
	}
}