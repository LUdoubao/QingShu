package org.doubao.dialog.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 消息发送请求参数对象
 * 前端调用/dialog/message/send接口时，需传递此参数
 * 用于指定消息所属会话、内容、类型等核心信息，支持文字和表情类型消息
 */
@ApiModel(description = "消息发送请求参数，包含会话ID、消息内容、消息类型等")
public class MessageSendReq {

	/**
	 * 会话ID
	 * 业务规则：
	 * 1. 必须传递已存在的会话ID（需通过dialog_sessions表校验有效性）
	 * 2. 当前登录用户必须是该会话的参与者（userId=会话owner或targetId）
	 * 3. 会话未被逻辑删除（is_deleted=0）
	 */
	@NotNull(message = "会话ID不能为空，请传递合法的会话ID")
	@ApiModelProperty(
			value = "消息所属会话ID（当前用户必须是会话参与者）",
			required = true,
			example = "456",
			notes = "会话ID可通过会话列表接口（/dialog/session/list）获取，无效会话ID会导致发送失败"
	)
	private Long sessionId;

	/**
	 * 消息内容
	 * 业务规则：
	 * 1. 文字消息（TEXT）：支持中英文、数字、符号，需过滤敏感词（后端统一处理）
	 * 2. 表情消息（EMOJI）：传递表情编码（如“[微笑]”“[大笑]”，需符合后端表情编码规范）
	 * 3. 内容长度限制：最大500字符（避免消息过大导致传输延迟）
	 */
	@NotBlank(message = "消息内容不能为空，请输入文字或选择表情")
	@Size(max = 500, message = "消息内容过长，最大支持500字符")
	@ApiModelProperty(
			value = "消息内容（文字消息直接传文本，表情消息传表情编码如[微笑]）",
			required = true,
			example = "你好，这是一条测试消息！",
			notes = "文字消息支持500字符以内，表情消息需使用后端定义的编码格式"
	)
	private String content;

	/**
	 * 消息类型
	 * 枚举约束：仅支持TEXT（文字消息）和EMOJI（表情消息）
	 * 业务规则：
	 * 1. TEXT类型：后端会进行敏感词过滤、内容长度二次校验
	 * 2. EMOJI类型：后端会校验表情编码合法性（是否在预设表情列表中）
	 */
	@NotBlank(message = "消息类型不能为空，请选择TEXT或EMOJI")
	@ApiModelProperty(
			value = "消息类型，固定枚举值：TEXT（文字消息）、EMOJI（表情消息）",
			required = true,
			example = "TEXT",
			allowableValues = "TEXT,EMOJI",
			notes = "EMOJI类型需传递后端支持的表情编码，非法编码会被转为文字显示"
	)
	private String contentType;

	/**
	 * 消息扩展字段（可选）
	 * 用途：存储消息附加信息，如消息引用（引用历史消息ID）、消息标记（重要/普通）等
	 * 格式：JSON字符串（如{"quoteMsgId":"60d21b4667d0d8992e610c85","isImportant":false}）
	 * 业务规则：非必填，无扩展信息时可传null或空字符串
	 */
	@ApiModelProperty(
			value = "消息扩展字段（可选，JSON格式），如引用历史消息、消息重要性标记等",
			required = false,
			example = "{\"quoteMsgId\":\"60d21b4667d0d8992e610c85\",\"isImportant\":false}",
			notes = "无扩展信息时可省略此参数，后端会默认处理为null"
	)
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
