package org.doubao.dialog.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.doubao.dialog.service.req.SessionCreateReq;
import org.doubao.dialog.service.req.SessionTopReq;
import org.doubao.dialog.service.service.SessionService;
import org.doubao.dialog.service.vo.SessionVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 会话管理控制器
 * 提供接口：创建会话、查询会话列表、置顶/取消置顶、删除会话、未读清零
 */
@RestController
@RequestMapping("/dialog/session")
@Api(tags = "会话管理接口")
public class SessionController {

    @Autowired
    private SessionService sessionService;


    /**
     * 创建会话（用户-用户/用户-AI）
     * @param createReq 会话创建请求参数（targetId：目标ID，sessionType：会话类型）
     * @return 会话ID
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建会话", notes = "支持用户会话（USER）和AI会话（AI），AI会话targetId固定为10000")
    public Result<Long> createSession(
            @ApiParam(value = "会话创建参数", required = true) @RequestBody SessionCreateReq createReq
    ) {
        // 1. 解析用户ID
        Long reqUserId;
        UserInfo user = UserContext.getUser();

        if (user == null) {
            reqUserId = createReq.getUserId();
            if (reqUserId == null) {
                throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
            }
        } else {
            reqUserId = Long.valueOf(user.getId());
        }

        // 2. 调用Service创建会话
        Long sessionId = sessionService.createSession(reqUserId, createReq);

        // 3. 返回结果
        return Result.success(sessionId);
    }

    /**
     * 分页查询用户会话列表
     * @param pageNum 页码（默认1）
     * @param pageSize 页大小（默认20）
     * @return 分页会话列表（含未读计数、最后一条消息预览）
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询会话列表", notes = "按“置顶>最后消息时间倒序”排序，返回会话详情")
    public Result<Page<SessionVO>> getSessionList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 调用Service查询会话列表
        Page<SessionVO> sessionPage = sessionService.getSessionList(userId, pageNum, pageSize);

        // 3. 返回结果
        return Result.success(sessionPage);
    }

    /**
     * 会话置顶/取消置顶
     * @param topReq 置顶请求参数（sessionId：会话ID，isTop：1=置顶，0=取消）
     * @return 操作结果
     */
    @PostMapping("/top")
    @ApiOperation(value = "会话置顶/取消置顶", notes = "isTop=1置顶，isTop=0取消，置顶会话会排在列表最前")
    public Result<Void> updateSessionTop(
            @ApiParam(value = "置顶参数", required = true) @RequestBody SessionTopReq topReq
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 调用Service更新置顶状态
        sessionService.updateSessionTop(userId, topReq.getSessionId(), topReq.getIsTop());

        // 3. 返回结果
        return Result.success();
    }

    /**
     * 删除会话（逻辑删除）
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @GetMapping("/delete/{sessionId}")
    @ApiOperation(value = "删除会话", notes = "逻辑删除（保留消息记录），重新创建会话可恢复历史消息")
    public Result<Void> deleteSession(
            @ApiParam(value = "会话ID", required = true) @PathVariable Long sessionId
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 调用Service删除会话
        sessionService.deleteSession(userId, sessionId);

        // 3. 返回结果
        return Result.success();
    }
    /**
     * 隐藏会话
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @GetMapping("/hidden/{sessionId}")
    @ApiOperation(value = "删除会话", notes = "逻辑删除（保留消息记录），重新创建会话可恢复历史消息")
    public Result<Void> hiddenSession(
            @ApiParam(value = "会话ID", required = true) @PathVariable Long sessionId
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 调用Service删除会话
        sessionService.hiddenSession(userId, sessionId);

        // 3. 返回结果
        return Result.success();
    }

    /**
     * 会话未读清零
     * @param sessionId 会话ID
     * @return 清零前的未读计数
     */
    @PostMapping("/unread/clear/{sessionId}")
    @ApiOperation(value = "会话未读清零", notes = "进入会话后调用，将该会话未读消息数置为0")
    public Result<Void> clearSessionUnread(
            @ApiParam(value = "会话ID", required = true) @PathVariable Long sessionId
    ) {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();


        // 2. 调用Service未读清零
        sessionService.clearSessionUnread(userId, sessionId);

        // 3. 返回结果
        return Result.success();
    }

    @PostMapping("/unread/clearAll")
    public Result<Void> clearAllUnread() {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();

        // 2. 调用Service未读清零
        sessionService.clearAllSessionUnread(userId);

        // 3. 返回结果
        return Result.success();
    }

    @GetMapping("/unreadCount/get")
    @ApiOperation(value = "会话未读总数")
    public Result<Integer> getUnreadCount() {
        // 1. 解析用户ID
        Long userId = UserContext.getUserId();

        // 2. 调用Service未读清零
        Integer unreadCount = sessionService.getUnreadCount(userId);

        // 3. 返回结果
        return Result.success(unreadCount);
    }
}
  