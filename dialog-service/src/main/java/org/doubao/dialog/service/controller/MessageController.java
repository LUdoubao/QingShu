package org.doubao.dialog.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.doubao.dialog.service.req.MessageClearReq;
import org.doubao.dialog.service.req.MessageResendReq;
import org.doubao.dialog.service.req.MessageSendReq;
import org.doubao.dialog.service.service.MessageService;
import org.doubao.dialog.service.vo.MessageVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;

/**
 * 消息管理控制器
 * 提供接口：发送消息、查询历史消息、重发失败消息、清空会话消息、标记消息已读
 */
@RestController
@RequestMapping("/dialog/message")
@Api(tags = "消息管理接口")
public class MessageController {

    @Autowired
    private MessageService messageService;


    /**
     * 发送消息（文字/表情）
     * @param sendReq 消息发送请求参数（sessionId：会话ID，content：内容，contentType：类型）
     * @return 消息ID（MongoDB的ObjectId）
     */
    @PostMapping("/send")
    @ApiOperation(value = "发送消息", notes = "支持文字（TEXT）和表情（EMOJI），表情内容用编码格式如[微笑]")
    public Result<String> sendMessage(
            @ApiParam(value = "消息发送参数", required = true) @RequestBody MessageSendReq sendReq
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();

        // 2. 调用Service发送消息
        String msgId = messageService.sendMessage(sendReq, userId);

        // 3. 返回结果
        return Result.success(msgId);
    }

    /**
     * 分页查询会话历史消息
     * @param sessionId 会话ID
     * @param pageNum 页码（默认1，倒序：最新消息在第1页）
     * @param pageSize 页大小（默认10）
     * @return 分页消息列表
     */
    @GetMapping("/history")
    @ApiOperation(value = "查询历史消息", notes = "按发送时间倒序排列，下拉加载更多时递增pageNum")
    public Result<Page<MessageVO>> getMessageHistory(
            @ApiParam(value = "会话ID", required = true) @RequestParam Long sessionId,
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 调用Service查询历史消息
        Page<MessageVO> messagePage = messageService.getMessageHistory(sessionId, userId, pageNum, pageSize);

        // 3. 返回结果
        return Result.success(messagePage);
    }

    /**
     * 重发失败消息
     * @param resendReq 消息重发请求参数（msgId：消息ID）
     * @return 操作结果
     */
    @PutMapping("/resend")
    @ApiOperation(value = "重发失败消息", notes = "仅支持状态为FAILED的消息，重发后状态更新为SENT")
    public Result<Void> resendMessage(
            @ApiParam(value = "消息重发参数", required = true) @RequestBody MessageResendReq resendReq
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 调用Service重发消息
        messageService.resendMessage(resendReq.getOriginalMsgId(), userId);

        // 3. 返回结果
        return Result.success();
    }

    /**
     * 清空会话消息（物理删除MongoDB中的消息）
     * @param clearReq 消息清空请求参数（sessionId：会话ID）
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    @ApiOperation(value = "清空会话消息", notes = "永久删除该会话所有消息，不可恢复")
    public Result<Void> clearSessionMessages(
            @ApiParam(value = "消息清空参数", required = true) @RequestBody MessageClearReq clearReq
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();

        // 2. 调用Service清空消息
        messageService.clearSessionMessages(clearReq.getConversationId(), userId);

        // 3. 返回结果
        return Result.success();
    }

    /**
     * 标记消息为已读
     * @param sessionId 会话ID
     * @param msgIds 消息ID列表（为空则标记该会话所有未读消息）
     * @return 操作结果
     */
    @PutMapping("/read")
    @ApiOperation(value = "标记消息为已读", notes = "msgIds为空时标记会话所有未读消息，不为空时仅标记指定消息")
    public Result<Void> markMessagesAsRead(
            @ApiParam(value = "会话ID", required = true) @RequestParam Long sessionId,
            @ApiParam(value = "消息ID列表（可选）") @RequestParam(required = false) java.util.List<String> msgIds
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 处理空列表（避免NullPointerException）
        if (msgIds == null) {
            msgIds = new ArrayList<>();
        }

        // 3. 调用Service标记已读
        messageService.markMessagesAsRead(sessionId, userId, msgIds);

        // 4. 返回结果
        return Result.success();
    }
}
  