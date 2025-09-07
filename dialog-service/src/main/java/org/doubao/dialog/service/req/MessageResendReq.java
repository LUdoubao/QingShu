package org.doubao.dialog.service.req;


import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doubao.dialog.service.enums.MsgTypeEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.util.Map;

/**
 * 消息重发请求DTO
 * 用途：接收客户端发起的消息重发请求，包含重发所需的核心参数（关联会话、原消息标识、消息类型等）
 * 适用场景：消息发送失败后重试（如网络波动、服务临时不可用）、手动触发重发
 */
@ApiModel(value = "MessageResendReq", description = "消息重发请求参数")
public class MessageResendReq implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 会话ID
	 * 说明：关联消息所属的会话，确保重发消息进入正确的聊天窗口
	 */
	@NotNull(message = "会话ID不能为空")
	@Positive(message = "会话ID必须为正整数")
	@ApiModelProperty(value = "会话ID（关联dialog_sessions表id）", required = true, example = "123456")
	private Long conversationId;

	/**
	 * 发送者用户ID
	 * 说明：重发消息的发起者，需与原消息发送者一致（避免越权重发）
	 */
	@NotNull(message = "发送者ID不能为空")
	@Positive(message = "发送者ID必须为正整数")
	@ApiModelProperty(value = "发送者用户ID", required = true, example = "10001")
	private Long senderId;

	/**
	 * 原消息ID
	 * 说明：关联需要重发的原始消息（MongoDB的ObjectId或业务自定义ID），用于查询原消息内容/接收者等信息
	 */
	@NotBlank(message = "原消息ID不能为空")
	@ApiModelProperty(value = "原消息ID（关联消息表msg_id）", required = true, example = "60d21b4667d0d8992e610c85")
	private String originalMsgId;

	/**
	 * 消息类型
	 * 说明：与原消息类型保持一致，确保重发消息格式正确（文本/图片/语音/文件）
	 */
	@NotNull(message = "消息类型不能为空")
	@ApiModelProperty(value = "消息类型（TEXT=文本，IMAGE=图片，VOICE=语音，FILE=文件）", required = true, example = "TEXT")
	private MsgTypeEnum msgType;

	/**
	 * 重发消息内容（可选）
	 * 说明：1. 文本消息需传具体内容；2. 媒体消息（图片/语音/文件）传资源URL；3. 为空时默认使用原消息内容
	 */
	@ApiModelProperty(value = "重发消息内容（文本消息传内容，媒体消息传URL）", example = "你好，这是重发的消息～")
	private String content;

	/**
	 * 媒体消息额外参数（可选）
	 * 说明：仅媒体消息（图片/语音/文件）使用，存储资源相关信息（如图片宽高、语音时长、文件大小）
	 * 示例：
	 * {
	 *   "imageWidth": 1080,
	 *   "imageHeight": 1920,
	 *   "voiceDuration": 15,  // 单位：秒
	 *   "fileSize": 204800,   // 单位：字节
	 *   "fileName": "文档.pdf"
	 * }
	 */
	@ApiModelProperty(value = "媒体消息额外参数（图片宽高/语音时长/文件信息等）", hidden = false)
	private Map<String, Object> mediaExtParams;

	/**
	 * 重发原因（可选）
	 * 说明：记录重发触发原因，用于问题排查（如客户端重试、服务端补偿、手动重发）
	 */
	@ApiModelProperty(value = "重发原因", example = "客户端网络波动导致首次发送失败")
	private String resendReason;

	/**
	 * 重发次数（可选）
	 * 说明：当前重发次数（用于限制最大重试次数，避免无限重发），默认0（首次重发）
	 */
	@PositiveOrZero(message = "重发次数不能为负数")
	@ApiModelProperty(value = "重发次数（默认0，用于限制最大重试次数）", example = "0")
	private Integer resendCount = 0;

	/**
	 * 自定义校验：文本消息必须包含内容
	 * 逻辑：当消息类型为TEXT时，content字段不能为空（为空时默认使用原消息内容，但需明确校验）
	 * @return 校验结果（true=通过，false=失败）
	 */
	public boolean validateTextContent() {
		if (MsgTypeEnum.TEXT.equals(this.msgType) && StrUtil.isBlank(this.content)) {
			// 文本消息content为空时，需确保能通过originalMsgId查询到原消息内容
			// 此处可扩展：若原消息查询不到，需抛出异常或返回false
			return false;
		}
		return true;
	}

	/**
	 * 自定义校验：媒体消息必须包含额外参数
	 * 逻辑：图片/语音/文件消息需携带对应的媒体参数（如图片宽高、语音时长）
	 * @return 校验结果（true=通过，false=失败）
	 */
	public boolean validateMediaParams() {
		if (MsgTypeEnum.IMAGE.equals(this.msgType) && (this.mediaExtParams == null
				|| this.mediaExtParams.get("imageWidth") == null
				|| this.mediaExtParams.get("imageHeight") == null)) {
			return false; // 图片消息缺少宽高参数
		}
		if (MsgTypeEnum.VOICE.equals(this.msgType) && (this.mediaExtParams == null
				|| this.mediaExtParams.get("voiceDuration") == null)) {
			return false; // 语音消息缺少时长参数
		}
		if (MsgTypeEnum.FILE.equals(this.msgType) && (this.mediaExtParams == null
				|| this.mediaExtParams.get("fileSize") == null
				|| this.mediaExtParams.get("fileName") == null)) {
			return false; // 文件消息缺少大小/名称参数
		}
		return true;
	}

	public @NotNull(message = "会话ID不能为空") @Positive(message = "会话ID必须为正整数") Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(@NotNull(message = "会话ID不能为空") @Positive(message = "会话ID必须为正整数") Long conversationId) {
		this.conversationId = conversationId;
	}

	public @NotNull(message = "发送者ID不能为空") @Positive(message = "发送者ID必须为正整数") Long getSenderId() {
		return senderId;
	}

	public void setSenderId(@NotNull(message = "发送者ID不能为空") @Positive(message = "发送者ID必须为正整数") Long senderId) {
		this.senderId = senderId;
	}

	public @NotNull(message = "消息类型不能为空") MsgTypeEnum getMsgType() {
		return msgType;
	}

	public void setMsgType(@NotNull(message = "消息类型不能为空") MsgTypeEnum msgType) {
		this.msgType = msgType;
	}

	public @NotBlank(message = "原消息ID不能为空") String getOriginalMsgId() {
		return originalMsgId;
	}

	public void setOriginalMsgId(@NotBlank(message = "原消息ID不能为空") String originalMsgId) {
		this.originalMsgId = originalMsgId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Map<String, Object> getMediaExtParams() {
		return mediaExtParams;
	}

	public void setMediaExtParams(Map<String, Object> mediaExtParams) {
		this.mediaExtParams = mediaExtParams;
	}

	public String getResendReason() {
		return resendReason;
	}

	public void setResendReason(String resendReason) {
		this.resendReason = resendReason;
	}

	public @PositiveOrZero(message = "重发次数不能为负数") Integer getResendCount() {
		return resendCount;
	}

	public void setResendCount(@PositiveOrZero(message = "重发次数不能为负数") Integer resendCount) {
		this.resendCount = resendCount;
	}
}