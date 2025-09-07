package org.doubao.dialog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.dto.MessageRequest;
import org.doubao.dialog.service.entity.AssistantMessage;
import org.doubao.dialog.service.entity.DialogMessage;
import org.doubao.dialog.service.req.MessageSendReq;
import org.doubao.dialog.service.vo.DialogQuery;
import org.doubao.dialog.service.vo.DialogVO;
import org.doubao.dialog.service.vo.MessageVO;

import java.util.List;

/**
 * 消息管理服务接口
 * 定义功能：发送消息、查询历史消息、重发失败消息、清空会话消息、标记消息已读
 */
public interface MessageService extends IService<AssistantMessage> {

	/**
	 * 处理用户消息并生成回复
	 * @param request 用户消息请求
	 * @return 回复消息
	 */
	MessageDTO handleUserMessage(MessageRequest request);

	/**
	 * 获取用户的对话历史列表
	 * @param userId 用户ID
	 * @param pageNum 页码
	 * @param pageSize 每页大小
	 * @return 对话历史分页列表
	 */
	IPage<DialogVO> getUserDialogHistory(Long userId, int pageNum, int pageSize);

	/**
	 * 获取对话详情
	 * @param dialogId 对话ID
	 * @return 对话详情
	 */
	DialogVO getDialogDetail(Long dialogId);

	/**
	 * 管理员获取对话列表
	 * @param query 查询条件
	 * @param pageNum 页码
	 * @param pageSize 每页大小
	 * @return 对话分页列表
	 */
	IPage<DialogVO> getAdminDialogList(DialogQuery query, int pageNum, int pageSize);

	/**
	 * 更新对话状态
	 * @param dialogId 对话ID
	 * @param status 状态
	 * @return 是否成功
	 */
	boolean updateDialogStatus(Long dialogId, Integer status);

	/**
	 * 发送消息（文字/表情）
	 * @param sendReq 消息发送请求参数（sessionId：会话ID，content：内容，contentType：类型）
	 * @param senderId 发送者ID（当前用户）
	 * @return 消息ID（MongoDB的ObjectId）
	 */
	String sendMessage(MessageSendReq sendReq, Long senderId);

	/**
	 * 分页查询会话历史消息
	 * @param sessionId 会话ID
	 * @param userId 当前用户ID（校验会话归属）
	 * @param pageNum 页码（从1开始，倒序：最新消息在第1页）
	 * @param pageSize 页大小
	 * @return 分页消息VO列表
	 */
	Page<MessageVO> getMessageHistory(Long sessionId, Long userId, Integer pageNum, Integer pageSize);

	/**
	 * 重发失败消息
	 * @param msgId 消息ID（MongoDB的ObjectId）
	 * @param senderId 发送者ID（校验消息归属）
	 */
	void resendMessage(String msgId, Long senderId);

	/**
	 * 清空会话消息（物理删除MongoDB中的消息）
	 * @param sessionId 会话ID
	 * @param userId 当前用户ID（校验会话归属）
	 */
	void clearSessionMessages(Long sessionId, Long userId);

	/**
	 * 标记消息为已读
	 * @param sessionId 会话ID
	 * @param receiverId 接收者ID（校验消息归属）
	 * @param msgIds 消息ID列表（为空则标记该会话所有未读消息）
	 */
	void markMessagesAsRead(Long sessionId, Long receiverId, List<String> msgIds);

	/**
	 * 根据消息ID和发送者ID查询消息（内部调用，用于重发校验）
	 * @param msgId 消息ID
	 * @param senderId 发送者ID
	 * @return 消息PO（不存在则返回null）
	 */
	DialogMessage getMessageByIdAndSenderId(String msgId, Long senderId);
}