package org.doubao.dialog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.dto.MessageRequest;
import org.doubao.dialog.service.entity.AssistantMessage;
import org.doubao.dialog.service.vo.DialogQuery;
import org.doubao.dialog.service.vo.DialogVO;

/**
 * 消息服务接口
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
}