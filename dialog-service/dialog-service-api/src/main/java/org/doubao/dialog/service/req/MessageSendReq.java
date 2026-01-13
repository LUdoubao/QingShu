package org.doubao.dialog.service.req;





import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 消息发送请求参数对象
 * 前端调用/dialog/message/send接口时，需传递此参数
 * 用于指定消息所属会话、内容、类型等核心信息，支持文字和表情类型消息
 * 业务说明：定义用户发送消息时的请求参数，包含会话信息、消息内容、消息类型等必要字段
 */
//@ApiModel(description = "消息发送请求参数，包含会话ID、消息内容、消息类型等")
public class MessageSendReq {

	/**
	 * 会话ID
	 * 业务规则：
	 * 1. 必须传递已存在的会话ID（需通过dialog_sessions表校验有效性）
	 * 2. 当前登录用户必须是该会话的参与者（userId=会话owner或targetId）
	 * 3. 会话未被逻辑删除（is_deleted=0）
	 * 业务说明：标识消息所属的会话，用于消息归类和会话历史管理
	 * 数据校验：不能为空，必须为正整数
	 * 使用场景：用户在特定会话中发送消息
	 */
	@NotNull(message = "会话ID不能为空，请传递合法的会话ID")
	private Long sessionId;

	/**
	 * 消息内容
	 * 业务规则：
	 * 1. 文字消息（TEXT）：支持中英文、数字、符号，需过滤敏感词（后端统一处理）
	 * 2. 表情消息（EMOJI）：传递表情编码（如“[微笑]”“[大笑]”，需符合后端表情编码规范）
	 * 3. 内容长度限制：最大500字符（避免消息过大导致传输延迟）
	 * 业务说明：消息的具体内容，支持文字和表情两种类型
	 * 数据校验：不能为空，最大500字符
	 * 使用场景：用户输入的聊天内容，包括文字和表情
	 */
	@NotBlank(message = "消息内容不能为空，请输入文字或选择表情")
	@Size(max = 500, message = "消息内容过长，最大支持500字符")
	private String content;

	/**
	 * 消息类型
	 * 枚举约束：仅支持TEXT（文字消息）和EMOJI（表情消息）
	 * 业务规则：
	 * 1. TEXT类型：后端会进行敏感词过滤、内容长度二次校验
	 * 2. EMOJI类型：后端会校验表情编码合法性（是否在预设表情列表中）
	 * 业务说明：标识消息的类型，用于消息处理和前端渲染
	 * 数据校验：不能为空，仅支持TEXT和EMOJI
	 * 使用场景：区分文字消息和表情消息的处理逻辑
	 */
	@NotBlank(message = "消息类型不能为空，请选择TEXT或EMOJI")
	private String contentType;

	/**
	 * 消息扩展字段（可选）
	 * 业务规则：非必填，无扩展信息时可传null或空字符串
	 * 业务说明：存储消息的扩展属性，用于支持高级消息功能
	 * 数据格式：JSON字符串格式
	 * 使用场景：消息引用、重要性标记、消息分类等高级功能
	 */
	private String extInfo;

	public @NotNull(message = "会话ID不能为空，请传递合法的会话ID") Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(@NotNull(message = "会话ID不能为空，请传递合法的会话ID") Long sessionId) {
		this.sessionId = sessionId;
	}

	public @NotBlank(message = "消息内容不能为空，请输入文字或选择表情") @Size(max = 500, message = "消息内容过长，最大支持2000字符") String getContent() {
		return content;
	}

	public void setContent(@NotBlank(message = "消息内容不能为空，请输入文字或选择表情") @Size(max = 500, message = "消息内容过长，最大支持2000字符") String content) {
		this.content = content;
	}

	public @NotBlank(message = "消息类型不能为空，请选择TEXT或EMOJI") String getContentType() {
		return contentType;
	}

	public void setContentType(@NotBlank(message = "消息类型不能为空，请选择TEXT或EMOJI") String contentType) {
		this.contentType = contentType;
	}

	public String getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(String extInfo) {
		this.extInfo = extInfo;
	}
}
