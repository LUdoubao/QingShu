package org.doubao.ai.service.dto;


/**
 * AI服务响应DTO
 */
public class AIResponse {

	/**
	 * 回复内容
	 */
	private String content;

	/**
	 * 响应时间
	 */
	private Long responseTime;

	/**
	 * 是否成功
	 */
	private Boolean success;

	/**
	 * 错误信息
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