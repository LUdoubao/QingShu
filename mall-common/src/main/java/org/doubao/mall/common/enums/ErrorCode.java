package org.doubao.mall.common.enums;

public enum ErrorCode {
	// 通用错误
	BAD_REQUEST("COM_400", "请求参数错误"),
	UNAUTHORIZED("COM_401", "未授权访问"),
	FORBIDDEN("COM_403", "禁止访问"),
	NOT_FOUND("COM_404", "资源不存在"),
	RATE_LIMIT_EXCEEDED("COM_429", "请求过于频繁"),

	// 业务错误
	LIKE_ALREADY_EXISTS("LIKE_001", "重复点赞"),
	LIKE_NOT_FOUND("LIKE_002", "点赞记录不存在"),
	LIKE_FOUND_ERROR("LIKE_003", "点赞记录查询失败"),
	LIKE_COUNT_UPDATE_ERROR("LIKE_003", "点赞计数更新冲突"),

	CONTENT_LIMIT_REACHED("CONTENT_003", "内容创建达上限"),
	EMAIL_EXISTS("USER_001", "邮箱已被注册"),
	INVALID_VERIFY_CODE("USER_002", "验证码无效或已过期"),
	USER_DISABLED("USER_004", "用户已被禁用"),
	OLD_PASSWORD_ERROR("USER_005", "原密码错误"),
	USERNAME_PASSWORD_ERROR("USER_003", "用户名或密码错误"),
	INVALID_STATUS("USER_006", "无效的状态值"),
	INVALID_ROLE("USER_007", "无效的角色类型"),

	COMMENT_HAS_NOT_ALLOWED("COMMENT_1001", "内容包含敏感词"),
	COMMENT_NOT_IN_RANGE("COMMENT_1003", "评论字数需在10-500之间"),
	COMMENT_LIMIT_REACHED("COMMENT_1002", "操作过于频繁，请稍后再试"),
	COMMENT_PAGE_SIZE_LIMIT("COMMENT_1004", "每页最大支持50条评论");


	private String code;
	private String message;
	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	// 其他错误码...
}