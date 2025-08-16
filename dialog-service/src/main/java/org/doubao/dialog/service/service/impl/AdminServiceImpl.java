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
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AssistantMessageMapper, AssistantMessage> implements AdminService {

	private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);
	@Autowired
	private AssistantDialogMapper dialogMapper;

	@Autowired
	private AssistantMessageMapper messageMapper;

	@Autowired
	private WebSocketService webSocketService;

	/**
	 * 管理员发送回复
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
