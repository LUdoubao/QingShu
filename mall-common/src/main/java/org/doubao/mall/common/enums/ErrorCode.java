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
	TOKEN_ERROR("COM_501", "Token格式错误（需Bearer前缀）"),
	TOKEN_INVALID("COM_502", "Token无效或已过期"),

	// 业务错误
	LIKE_ALREADY_EXISTS("LIKE_001", "重复点赞"),
	LIKE_NOT_FOUND("LIKE_002", "点赞记录不存在"),
	LIKE_FOUND_ERROR("LIKE_003", "点赞记录查询失败"),
	LIKE_COUNT_UPDATE_ERROR("LIKE_003", "点赞计数更新冲突"),

	CONTENT_LIMIT_REACHED("CONTENT_003", "内容创建达上限"),
	CONTENT_EXISTS("CONTENT_001", "本站该引文已存在, 请检查"),

	TAG_NAME_EMPTY("TAG_001", "标签名称不能为空"),
	TAG_NAME_EXIST( "TAG_002", "标签名称已存在"),

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
	USER_INVALID_VISIBILITY("USER_020", "无效的可见性设置"),
	USER_INVALID_NEW_PASSWORD("USER_021", "新密码需包含字母和数字，且至少8位,最多20位"),
	USER_NICKNAME_EXISTS("USER_022",  "昵称已被注册"),
	USER_CHAT_PRIVACY_NOT_OPEN("USER_023",  "由于对方隐私设置, 您无权发起聊天"),
	USER_BLOCK_SELF("USER_024",  "不能拉黑自己"),
	USER_BLOCK_EXISTS("USER_024",  "该用户已在黑名单中"),
	USER_BLOCK_NOT_EXISTS("USER_025",  "该用户不在黑名单中"),
	USER_EMAIL_NOT_EXISTS("USER_026",  "该邮箱未注册"),
	VERIFY_CODE_ERROR("USER_027",  "验证码错误"),
	USER_INVALID_FIRST_CATEGORY("USER_028",  "无效的一级分类"),
	USER_INVALID_SECOND_CATEGORY("USER_029",  "无效的二级分类"),
	USER_INVALID_THIRD_CATEGORY("USER_030",  "无效的三级分类"),



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
	LOCAL_URL_ERROR("FILE_1008", "获取文件url失败"),

	FAVORITE_EXIST("fav_1001", "收藏已存在"),
	FAVORITE_FOLDER_NOT_EXIST("fav_1002", "收藏夹不存在"),

	QUOTE_NOT_FOUND("quote_1001", "引文不存在或待审核"),

	AI_LIMIT_EXCEEDED_TODAY("ai_001", "今日AI服务调用次数已达上限"),
	AI_LIMIT_EXCEEDED_SYSTEM("ai_002", "系统调用已达今日上限，请明天再试"),


	SEARCH_TYPE_NOT_SUPPORT("search_001", "不支持的搜索类型"),


	DIALOG_SESSION_SELF_CREATE("dialog_001", "不能与自己创建会话"),
	DIALOG_SESSION_NOT_EXIST("dialog_002", "会话不存在或已删除"),
	DIALOG_SESSION_NOT_ALLOW_DELETE("dialog_004", "AI助手会话不允许删除"),
	DIALOG_SESSION_ALREADY_TOP("dialog_003", "会话已处于该置顶状态"),
	DIALOG_SESSION_NOT_ALLOW_SEE("dialog_005", "无权限查看该会话消息"),
	DIALOG_MESSAGE_NOT_EXIST("dialog_006", "消息不存在或已删除"),
	DIALOG_MESSAGE_NOT_ALLOW_RESEND( "dialog_007", "仅失败状态的消息可重发"),
	DIALOG_MESSAGE_NOT_ALLOW_RESEND_QUERY( "dialog_008", "消息重发后查询失败"),
	DIALOG_MESSAGE_NOT_EXIST_IN_SESSION( "dialog_009", "会话不存在，无法重发消息"),
	DIALOG_MESSAGE_NOT_ALLOW_MARK_READ( "dialog_010", "无权限标记该会话消息为已读"),
	DIALOG_MESSAGE_NOT_ALLOW( "dialog_011", "无权限操作该消息"),
	DIALOG_MESSAGE_NOT_ALLOW_SEND( "dialog_012", "无权限在该会话发送消息"),
	DIALOG_MESSAGE_CONTENT_EMPTY( "dialog_013", "消息内容不能为空"),
	DIALOG_MESSAGE_CONTENT_TOO_LONG( "dialog_014", "文字消息长度不能超过500字"),
	DIALOG_MESSAGE_CACHE_EXPIRE_TIME_INVALID( "dialog_016", "会话缓存过期时间必须大于0秒"),
	DIALOG_MESSAGE_CONTENT_INVALID( "dialog_015", "表情消息格式错误（需符合[表情名]格式，如[微笑]）");




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