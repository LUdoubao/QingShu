package org.doubao.dialog.service.dto;


/**
 * AI服务响应DTO
 * 用途：封装AI服务的处理结果，包括回复内容、执行状态、响应时间等
 * 适用场景：AI服务调用完成后，将结果封装成此对象返回给调用方
 */
public class AIResponse {

	/**
	 * 回复内容
	 * 业务说明：AI生成的具体回复文本，可能包含普通文本、诗词内容、帮助信息等
	 * 数据格式：纯文本内容，长度受maxTokens参数限制
	 */
	private String content;

	/**
	 * 响应时间
	 * 业务说明：记录AI服务的响应耗时（毫秒），用于性能监控和优化
	 * 计算方式：AI服务开始处理时间 - AI服务结束处理时间
	 */
	private Long responseTime;

	/**
	 * 是否成功
	 * 业务说明：标识AI服务调用是否成功，true=成功，false=失败
	 * 使用场景：前端根据此字段判断是否显示AI回复内容
	 */
	private Boolean success;

	/**
	 * 错误信息
	 * 业务说明：当AI服务调用失败时，记录具体的错误描述，便于问题排查
	 * 常见错误：服务不可用、参数错误、模型异常等
	 */
	private String errorMsg;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}