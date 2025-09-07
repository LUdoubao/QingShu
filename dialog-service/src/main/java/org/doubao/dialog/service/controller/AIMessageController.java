package org.doubao.dialog.service.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.dto.MessageRequest;
import org.doubao.dialog.service.service.MessageService;
import org.doubao.dialog.service.vo.DialogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/dialog/ai")
@Api(tags = "对话消息接口")
public class AIMessageController {

	@Autowired
	private MessageService messageService;

	/**
	 * 发送消息
	 */
	@PostMapping("/message")
	@ApiOperation("发送消息")
	public ResponseEntity<MessageDTO> sendMessage(@Valid @RequestBody MessageRequest request) {
		MessageDTO response = messageService.handleUserMessage(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 获取用户对话历史
	 */
	@GetMapping("/history")
	@ApiOperation("获取用户对话历史")
	public ResponseEntity<IPage<DialogVO>> getDialogHistory(
			@RequestParam(required = false) Long userId,
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "10") int pageSize) {
		IPage<DialogVO> history = messageService.getUserDialogHistory(userId, pageNum, pageSize);
		return ResponseEntity.ok(history);
	}

	/**
	 * 获取对话详情
	 */
	@GetMapping("/{dialogId}")
	@ApiOperation("获取对话详情")
	public ResponseEntity<DialogVO> getDialogDetail(@PathVariable Long dialogId) {
		DialogVO detail = messageService.getDialogDetail(dialogId);
		return ResponseEntity.ok(detail);
	}
}