package org.doubao.dialog.service.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.dialog.service.dto.AdminReplyRequest;
import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.entity.AssistantMessage;

/**
 * 管理员服务接口
 * 业务说明：定义管理员对话管理功能的接口，包括发送回复、标记对话状态等
 * 适用场景：管理员后台管理系统、客服对话处理、对话状态管理
 */
public interface AdminService extends IService<AssistantMessage> {

	/**
	 * 管理员发送回复
	 * 业务说明：管理员对用户咨询进行回复，将回复内容保存并推送给用户
	 * @param request 管理员回复请求，包含对话ID、管理员ID、回复内容
	 * @return 回复消息对象，包含回复内容、发送时间等信息
	 */
	MessageDTO sendAdminReply(AdminReplyRequest request);

	/**
	 * 标记对话为已解决
	 * 业务说明：将对话状态标记为已解决，表示问题已处理完毕
	 * @param dialogId 对话ID，标识需要标记的对话
	 * @return 操作是否成功，true=成功，false=失败
	 */
	boolean markDialogResolved(Long dialogId);

	/**
	 * 标记对话为待跟进
	 * 业务说明：将对话状态标记为待跟进，表示需要后续处理
	 * @param dialogId 对话ID，标识需要标记的对话
	 * @return 操作是否成功，true=成功，false=失败
	 */
	boolean markDialogPending(Long dialogId);
}