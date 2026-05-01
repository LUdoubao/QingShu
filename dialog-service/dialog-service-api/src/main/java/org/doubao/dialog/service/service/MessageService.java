package org.doubao.dialog.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.dialog.service.entity.DialogMessage;
import org.doubao.dialog.service.req.MessageSendReq;
import org.doubao.dialog.service.vo.MessageVO;

import java.util.List;

public interface MessageService {

	String sendMessage(MessageSendReq sendReq, Long senderId);

	Page<MessageVO> getMessageHistory(Long sessionId, Long userId, Integer pageNum, Integer pageSize);

	void resendMessage(String msgId, Long senderId);

	void clearSessionMessages(Long sessionId, Long userId);

	void markMessagesAsRead(Long sessionId, Long receiverId, List<String> msgIds);

	DialogMessage getMessageByIdAndSenderId(String msgId, Long senderId);
}
