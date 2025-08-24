package org.doubao.mall.common.enums;

public enum ErrorCode {
	// 通用错误
	BAD_REQUEST("COM_400", "请求参数错误"),
	UNAUTHORIZED("COM_401", "未授权访问"),
	FORBIDDEN("COM_403", "禁止访问"),
	NOT_FOUND("COM_404", "资源不存在"),
	RATE_LIMIT_EXCEEDED("COM_429", "请求过于频繁"),
	PAGE_NUMBER_INVALID("COM_405", "页码必须大于等于1"),
	PAGE_SIZE_INVALID("COM_406", "每页条数必须在1-100之间"),

	// 业务错误
	LIKE_ALREADY_EXISTS("LIKE_001", "重复点赞"),
	LIKE_NOT_FOUND("LIKE_002", "点赞记录不存在"),
	LIKE_FOUND_ERROR("LIKE_003", "点赞记录查询失败"),
	LIKE_COUNT_UPDATE_ERROR("LIKE_003", "点赞计数更新冲突"),

	CONTENT_LIMIT_REACHED("CONTENT_003", "内容创建达上限"),
	CONTENT_EXISTS("CONTENT_001", "本站该引文已存在, 请检查"),

	EMAIL_EXISTS("USER_001", "邮箱已被注册"),
	INVALID_VERIFY_CODE("USER_002", "验证码无效或已过期"),
	USER_DISABLED("USER_004", "用户已被禁用"),
	OLD_PASSWORD_ERROR("USER_005", "原密码错误"),
	USERNAME_PASSWORD_ERROR("USER_003", "用户名或密码错误"),
	INVALID_STATUS("USER_006", "无效的状态值"),
	INVALID_ROLE("USER_007", "无效的角色类型"),
	USERNAME_EXISTS("USER_007", "用户名已被注册"),
	USER_ID_EMPTY("USER_008", "用户ID不能为空"),
	USER_DISABLED_OR_NOT_EXISTS("USER_009", "用户不存在或已被禁用"),
	CANNOT_FOLLOW_YOURSELF("USER_010", "不能关注自己"),
	USER_ALREADY_FOLLOWED("USER_011", "已关注该用户"),
	USER_NOT_FOLLOWED("USER_012", "未关注该用户"),
	USER_TARGET_LIST_EMPTY("USER_013", "目标用户列表不能为空"),
	USER_BATCH_FOLLOW_LIMIT("USER_014", "批量关注最多支持100人"),
	USER_NO_FOLLOW_TARGET("USER_015", "无有效关注目标"),
	USER_NOT_LOGIN("USER_016", "用户未登录"),
	USER_PRIVACY_FOLLOWER_LIST_NOT_OPEN("USER_017", "该用户的粉丝列表未公开"),
	USER_PRIVACY_FOLLOWING_LIST_NOT_OPEN("USER_018", "该用户的关注列表未公开"),
	USER_PRIVACY_QUOTE_LIST_NOT_OPEN("USER_019", "该用户的作品列表未公开"),
	USER_INVALID_VISIBILITY("USER_020", "无效的可见性设置（仅支持1-3）"),



	COMMENT_HAS_NOT_ALLOWED("COMMENT_1001", "内容包含敏感词"),
	COMMENT_NOT_IN_RANGE("COMMENT_1003", "评论字数需在10-500之间"),
	COMMENT_LIMIT_REACHED("COMMENT_1002", "操作过于频繁，请稍后再试"),
	COMMENT_NOT_FOUND("COMMENT_1005", "评论不存在"),
	COMMENT_LIKE_ERROR("COMMENT_1006", "调用点赞服务异常"),
	COMMENT_PAGE_SIZE_LIMIT("COMMENT_1004", "每页最大支持50条评论"),

	LOCAL_FILE_ERROR("FILE_1001", "本地文件上传失败"),
	OSS_FILE_ERROR("FILE_1002", "OSS文件上传失败"),
	FILE_NOT_FOUND("FILE_1004", "文件不存在"),
	DELETE_FAIL("FILE_1005", "删除文件失败"),
	FILE_UPLOAD_FAIL("FILE_1006", "文件上传失败"),
	LOCAL_FOLDER_CREATED_ERROR("FILE_1007", "无法创建本地存储目录"),
	LOCAL_URL_ERROR("FILE_1008", "获取文件url失败");


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