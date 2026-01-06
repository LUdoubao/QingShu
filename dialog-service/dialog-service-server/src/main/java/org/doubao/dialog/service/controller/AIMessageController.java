package org.doubao.dialog.service.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;


import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.dto.MessageRequest;
import org.doubao.dialog.service.service.MessageService;
import org.doubao.dialog.service.vo.DialogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * AI消息控制器
 * 适用场景：
 * 1. 用户与AI助手的对话交互
 * 2. AI对话历史记录管理
 * 3. AI回复消息处理
 * 业务说明：处理用户与AI助手之间的消息交互，包括消息发送、历史记录查询等
 */
@RestController
@RequestMapping("/dialog/ai")
//@Api(tags = "对话消息接口")
public class AIMessageController {

	@Autowired
	private MessageService messageService;

	/**
	 * 发送消息到AI助手
	 * 业务说明：接收用户发送的消息，通过AI服务处理并返回AI助手的回复
	 * @param request 消息请求参数，包含用户ID、消息内容、模块类型等信息
	 * @return AI助手的回复消息，包含回复内容、时间等信息
	 * 接口路径：POST /dialog/ai/message
	 * 访问权限：需用户认证
	 * 业务流程：接收请求→调用AI服务→返回AI回复
	 */
	@PostMapping("/message")
	//@ApiOperation("发送消息到AI助手")
	public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody MessageRequest request) {
		MessageDTO response = messageService.handleUserMessage(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 获取用户AI对话历史
	 * 业务说明：分页获取当前用户的AI对话历史记录
	 * @param userId 用户ID，标识需要获取历史的用户
	 * @param pageNum 页码，从1开始，默认为1
	 * @param pageSize 每页大小，默认为10
	 * @return 分页的对话历史列表，包含对话标题、状态、最后消息等信息
	 * 接口路径：GET /dialog/ai/history
	 * 访问权限：需用户认证
	 * 业务流程：参数校验→查询AI对话历史→返回分页结果
	 */
	@GetMapping("/history")
	//@ApiOperation("获取用户AI对话历史")
	public ResponseEntity<IPage<DialogVO>> getDialogHistory(
			@RequestParam(required = false) Long userId,
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "10") int pageSize) {
		IPage<DialogVO> history = messageService.getUserDialogHistory(userId, pageNum, pageSize);
		return ResponseEntity.ok(history);
	}

	/**
	 * 获取AI对话详情
	 * 业务说明：获取指定AI对话的详细信息
	 * @param dialogId 对话ID，标识需要获取详情的AI对话
	 * @return 对话详情对象，包含对话信息和最后一条消息
	 * 接口路径：GET /dialog/ai/{dialogId}
	 * 访问权限：需用户认证且为对话所有者
	 * 业务流程：参数校验→查询对话详情→返回结果
	 */
	@GetMapping("/{dialogId}")
	//@ApiOperation("获取AI对话详情")
	public ResponseEntity<DialogVO> getDialogDetail(@PathVariable Long dialogId) {
		DialogVO detail = messageService.getDialogDetail(dialogId);
		return ResponseEntity.ok(detail);
	}
}