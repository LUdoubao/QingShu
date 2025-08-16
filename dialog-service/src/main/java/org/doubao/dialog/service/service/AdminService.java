package org.doubao.dialog.service.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.dialog.service.dto.AdminReplyRequest;
import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.entity.AssistantMessage;

/**
 * 管理员服务接口
 */
public interface AdminService extends IService<AssistantMessage> {

	/**
	 * 管理员发送回复
	 * @param request 管理员回复请求
	 * @return 回复消息
	 */
	MessageDTO sendAdminReply(AdminReplyRequest request);

	/**
	 * 标记对话为已解决
	 * @param dialogId 对话ID
	 * @return 是否成功
	 */
	boolean markDialogResolved(Long dialogId);

	/**
	 * 标记对话为待跟进
	 * @param dialogId 对话ID
	 * @return 是否成功
	 */
	boolean markDialogPending(Long dialogId);
}