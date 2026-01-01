package org.doubao.dialog.service.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.dialog.service.dto.AdminReplyRequest;
import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.entity.AssistantDialog;
import org.doubao.dialog.service.entity.AssistantMessage;
import org.doubao.dialog.service.enums.DialogStatusEnum;
import org.doubao.dialog.service.enums.SenderTypeEnum;
import org.doubao.dialog.service.mapper.AssistantDialogMapper;
import org.doubao.dialog.service.mapper.AssistantMessageMapper;
import org.doubao.dialog.service.service.AdminService;
import org.doubao.dialog.service.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 管理员服务实现类
 * 业务说明：实现管理员对话管理功能，包括发送回复、标记对话状态等操作
 * 核心功能：1. 管理员发送回复 2. 对话状态管理 3. 实时消息推送
 * 适用场景：
 * 1. 管理员对话管理功能
 * 2. 客服对话处理
 * 3. 对话状态管理
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AssistantMessageMapper, AssistantMessage> implements AdminService {

	/** 日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);
	/** 对话Mapper，用于对话信息的持久化操作 */
	@Autowired
	private AssistantDialogMapper dialogMapper;

	/** 消息Mapper，用于消息信息的持久化操作 */
	@Autowired
	private AssistantMessageMapper messageMapper;

	/** WebSocket服务，用于实时消息推送 */
	@Autowired
	private WebSocketService webSocketService;

	/**
	 * 管理员发送回复
	 * 业务说明：管理员对用户咨询进行回复，将回复内容保存到数据库并实时推送给用户
	 * 业务流程：
	 * 1. 保存管理员发送的消息到数据库
	 * 2. 更新对话状态为"活跃"（ACTIVE），表示正在处理中
	 * 3. 通过WebSocket实时推送消息给对应的用户
	 * 事务说明：使用事务确保消息保存和状态更新的一致性
	 * 异常处理：发生异常时回滚所有操作
	 * 参数校验：请求对象不能为空，对话ID、管理员ID、内容不能为空
	 * 数据处理：消息保存到数据库，对话状态更新为活跃状态
	 * @param request 管理员回复请求，包含对话ID、管理员ID、回复内容等信息
	 * @return 回复消息对象，包含对话ID、内容、发送时间等信息
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public MessageDTO sendAdminReply(AdminReplyRequest request) {
		log.info("管理员回复: {}", request);

		// 1. 保存管理员消息
		saveMessage(request.getDialogId(), request.getAdminId(), request.getContent(), SenderTypeEnum.ADMIN.getCode());

		// 2. 更新对话状态为"处理中"
		AssistantDialog dialog = new AssistantDialog();
		dialog.setId(request.getDialogId());
		dialog.setStatus(DialogStatusEnum.ACTIVE.getCode());
		dialog.setUpdatedTime(LocalDateTime.now());
		dialogMapper.updateById(dialog);

		// 3. 推送消息给用户
		Long userId = dialogMapper.selectUserIdByDialogId(request.getDialogId());
		MessageDTO reply = new MessageDTO(
				request.getDialogId(),
				request.getContent(),
				true,
				LocalDateTime.now(),
				SenderTypeEnum.ADMIN.getCode()
		);
		webSocketService.pushToUser(userId, reply);
		return reply;
	}

	/**
	 * 标记对话为已解决
	 * 业务说明：将指定对话标记为已解决状态，表示问题已处理完毕
	 * 业务流程：
	 * 1. 更新对话状态为"已解决"（RESOLVED）
	 * 2. 通过WebSocket通知用户问题已解决
	 * 事务说明：使用事务确保状态更新的原子性
	 * 异常处理：发生异常时回滚操作
	 * 参数校验：对话ID不能为空
	 * 数据处理：对话状态更新为已解决状态，向用户推送解决通知
	 * @param dialogId 对话ID，标识需要标记的对话
	 * @return 操作是否成功，true=成功，false=失败
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean markDialogResolved(Long dialogId) {
		AssistantDialog dialog = new AssistantDialog();
		dialog.setId(dialogId);
		dialog.setStatus(DialogStatusEnum.RESOLVED.getCode());
		dialog.setUpdatedTime(LocalDateTime.now());
		int rows = dialogMapper.updateById(dialog);

		// 通知用户问题已解决
		if (rows > 0) {
			Long userId = dialogMapper.selectUserIdByDialogId(dialogId);
			if (userId != null) {

				// TODO builder
				MessageDTO message = new MessageDTO();
				// MessageDTO message = MessageDTO.builder()
				// 		.dialogId(dialogId)
				// 		.content("您的问题已标记为已解决，如有其他疑问请继续提问。")
				// 		.isAdmin(true)
				// 		.senderType(SenderTypeEnum.ADMIN.getCode())
				// 		.sendTime(LocalDateTime.now())
				// 		.build();
				webSocketService.pushToUser(userId, message);
			}
		}

		return rows > 0;
	}

	/**
	 * 标记对话为待跟进
	 * 业务说明：将指定对话标记为待跟进状态，表示需要后续处理
	 * 业务流程：
	 * 1. 更新对话状态为"待跟进"（PENDING）
	 * 2. 通过WebSocket通知用户问题待跟进
	 * 事务说明：使用事务确保状态更新的原子性
	 * 异常处理：发生异常时回滚操作
	 * 参数校验：对话ID不能为空
	 * 数据处理：对话状态更新为待跟进状态，向用户推送待跟进通知
	 * @param dialogId 对话ID，标识需要标记的对话
	 * @return 操作是否成功，true=成功，false=失败
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean markDialogPending(Long dialogId) {
		AssistantDialog dialog = new AssistantDialog();
		dialog.setId(dialogId);
		dialog.setStatus(DialogStatusEnum.PENDING.getCode());
		dialog.setUpdatedTime(LocalDateTime.now());
		int rows = dialogMapper.updateById(dialog);

		// 通知用户问题待跟进
		if (rows > 0) {
			Long userId = dialogMapper.selectUserIdByDialogId(dialogId);
			if (userId != null) {
				// TODO builder
				MessageDTO message = new MessageDTO();
				// MessageDTO message = MessageDTO.builder()
				// 		.dialogId(dialogId)
				// 		.content("您的问题已记录，我们将尽快处理，请耐心等待。")
				// 		.isAdmin(true)
				// 		.senderType(SenderTypeEnum.ADMIN.getCode())
				// 		.sendTime(LocalDateTime.now())
				// 		.build();
				webSocketService.pushToUser(userId, message);
			}
		}

		return rows > 0;
	}

	/**
	 * 保存消息
	 * 业务说明：将消息保存到数据库，供后续查询和管理使用
	 * 业务流程：
	 * 1. 创建消息实体对象
	 * 2. 设置消息属性（对话ID、发送者ID、内容、发送者类型、发送时间）
	 * 3. 插入数据库
	 * 参数校验：对话ID、发送者ID、内容不能为空
	 * 数据处理：发送时间设置为当前时间
	 * @param dialogId 对话ID，标识消息所属对话
	 * @param senderId 发送者ID，标识消息发送方
	 * @param content 消息内容，存储实际的消息文本
	 * @param senderType 发送者类型，标识是用户还是管理员发送
	 */
	private void saveMessage(Long dialogId, Long senderId, String content, int senderType) {
		AssistantMessage message = new AssistantMessage();
		message.setDialogId(dialogId);
		message.setSenderId(senderId);
		message.setContent(content);
		message.setSenderType(senderType);
		message.setSendTime(LocalDateTime.now());
		messageMapper.insert(message);
	}
}