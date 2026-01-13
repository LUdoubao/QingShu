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
 * 业务说明：定义消息管理的核心服务接口，包括用户消息处理、历史消息查询、消息状态管理等
 * 适用场景：用户消息发送、对话历史管理、消息状态控制、消息重发机制
 */
public interface MessageService extends IService<AssistantMessage> {

	/**
	 * 处理用户消息并生成回复
	 * 业务说明：处理用户发送的消息，通过AI或知识库生成回复并返回
	 * @param request 用户消息请求，包含消息内容、用户ID、模块类型等信息
	 * @return 回复消息对象，包含AI或管理员的回复内容
	 */
	MessageDTO handleUserMessage(MessageRequest request);

	/**
	 * 获取用户的对话历史列表
	 * 业务说明：分页获取指定用户的所有对话历史记录
	 * @param userId 用户ID，标识需要获取历史的用户
	 * @param pageNum 页码，从1开始
	 * @param pageSize 每页大小，控制返回记录数量
	 * @return 对话历史分页列表，包含对话标题、状态、最后消息等信息
	 */
	IPage<DialogVO> getUserDialogHistory(Long userId, int pageNum, int pageSize);

	/**
	 * 获取对话详情
	 * 业务说明：获取指定对话的详细信息
	 * @param dialogId 对话ID，标识需要获取详情的对话
	 * @return 对话详情对象，包含对话信息和最后一条消息
	 */
	DialogVO getDialogDetail(Long dialogId);

	/**
	 * 管理员获取对话列表
	 * 业务说明：管理员分页获取对话列表，支持条件查询
	 * @param query 查询条件，包含状态、用户ID、时间范围等
	 * @param pageNum 页码，从1开始
	 * @param pageSize 每页大小，控制返回记录数量
	 * @return 对话分页列表，用于管理员后台管理
	 */
	IPage<DialogVO> getAdminDialogList(DialogQuery query, int pageNum, int pageSize);

	/**
	 * 更新对话状态
	 * 业务说明：更新指定对话的状态（活跃、已解决、待跟进）
	 * @param dialogId 对话ID，标识需要更新状态的对话
	 * @param status 新的状态值，参考DialogStatusEnum
	 * @return 操作是否成功，true=成功，false=失败
	 */
	boolean updateDialogStatus(Long dialogId, Integer status);

	/**
	 * 发送消息（文字/表情）
	 * 业务说明：发送文字或表情消息到指定会话
	 * @param sendReq 消息发送请求参数，包含会话ID、内容、类型等信息
	 * @param senderId 发送者ID，标识消息发送方
	 * @return 消息ID（MongoDB的ObjectId），用于消息唯一标识
	 */
	String sendMessage(MessageSendReq sendReq, Long senderId);

	/**
	 * 分页查询会话历史消息
	 * 业务说明：分页获取指定会话的历史消息记录
	 * @param sessionId 会话ID，标识需要查询的会话
	 * @param userId 当前用户ID，用于校验会话归属权限
	 * @param pageNum 页码，从1开始，按时间倒序排列
	 * @param pageSize 页大小，控制返回消息数量
	 * @return 分页消息VO列表，包含消息内容、发送者、时间等信息
	 */
	Page<MessageVO> getMessageHistory(Long sessionId, Long userId, Integer pageNum, Integer pageSize);

	/**
	 * 重发失败消息
	 * 业务说明：重发发送失败的消息
	 * @param msgId 消息ID（MongoDB的ObjectId），标识需要重发的消息
	 * @param senderId 发送者ID，用于校验消息归属权限
	 */
	void resendMessage(String msgId, Long senderId);

	/**
	 * 清空会话消息（物理删除MongoDB中的消息）
	 * 业务说明：清空指定会话中的所有消息记录
	 * @param sessionId 会话ID，标识需要清空消息的会话
	 * @param userId 当前用户ID，用于校验会话归属权限
	 */
	void clearSessionMessages(Long sessionId, Long userId);

	/**
	 * 标记消息为已读
	 * 业务说明：将指定消息标记为已读状态
	 * @param sessionId 会话ID，标识消息所属会话
	 * @param receiverId 接收者ID，用于校验消息归属权限
	 * @param msgIds 消息ID列表，为空则标记该会话所有未读消息
	 */
	void markMessagesAsRead(Long sessionId, Long receiverId, List<String> msgIds);

	/**
	 * 根据消息ID和发送者ID查询消息（内部调用，用于重发校验）
	 * 业务说明：根据消息ID和发送者ID查询消息记录，用于权限校验
	 * @param msgId 消息ID，标识需要查询的消息
	 * @param senderId 发送者ID，用于校验消息归属
	 * @return 消息PO对象，不存在则返回null
	 */
	DialogMessage getMessageByIdAndSenderId(String msgId, Long senderId);
}