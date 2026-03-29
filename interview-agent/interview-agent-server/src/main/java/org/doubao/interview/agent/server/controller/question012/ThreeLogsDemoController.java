package org.doubao.interview.agent.server.controller.question012;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question012.TransferRequest;
import org.doubao.interview.agent.api.dto.question012.TransferResponse;
import org.doubao.interview.agent.api.service.question012.ThreeLogsDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 三种日志演示 Controller - 提供 RESTful API 接口
 * 
 * 对应面试知识点：问题 012 - undo log、redo log、binlog 的区别
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示 MySQL 事务中三种日志的工作机制
 * 边界：仅用于学习和演示，不提供认证授权等生产功能
 * 线程安全：Controller 本身无状态，线程安全；业务逻辑的并发控制由 Service 层负责
 * 幂等性：转账接口的幂等性由 transactionNo 保证
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question012")
public class ThreeLogsDemoController {

    @Autowired
    private ThreeLogsDemoService threeLogsDemoService;

    /**
     * 执行转账操作 - 演示三种日志的完整流程
     * 
     * 【方法注释】
     * 输入约束：请求参数必须通过校验（fromAccountId、toAccountId 非空，amount>0.01）
     * 输出语义：返回转账结果和三种日志的详细信息
     * 异常场景：参数校验失败返回 400；业务异常返回 500
     * 性能注意点：涉及数据库事务，避免高频调用
     * 
     * @param request 转账请求参数
     * @return 转账响应，包含三种日志的详细信息
     */
    @PostMapping("/transfer")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        log.info("收到转账请求，fromAccountId={}, toAccountId={}, amount={}, simulateException={}", 
                request.getFromAccountId(), request.getToAccountId(), 
                request.getAmount(), request.getSimulateException());
        
        TransferResponse response = threeLogsDemoService.transfer(request);
        
        log.info("转账处理完成，transactionNo={}, success={}", 
                response.getTransactionNo(), response.getSuccess());
        
        return response;
    }

    /**
     * 查询账户信息
     * 
     * 【方法注释】
     * 输入约束：accountId 必须为正整数
     * 输出语义：返回账户的 JSON 格式信息
     * 异常场景：账户不存在返回 null
     * 
     * @param accountId 账户 ID
     * @return 账户信息（JSON 字符串）
     */
    @GetMapping("/account/{accountId}")
    public String getAccountInfo(@PathVariable Long accountId) {
        log.info("查询账户信息，accountId={}", accountId);
        return threeLogsDemoService.getAccountInfo(accountId);
    }

    /**
     * 初始化演示数据
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回初始化结果描述
     * 异常场景：数据库操作失败时抛出异常
     * 性能注意点：会清空已有数据，生产环境慎用
     * 
     * @return 初始化结果描述
     */
    @PostMapping("/init")
    public String initDemoData() {
        log.info("初始化演示数据");
        String result = threeLogsDemoService.initDemoData();
        log.info("演示数据初始化完成");
        return result;
    }

    /**
     * 获取三种日志的详细对比说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 Markdown 格式的对比说明文档
     * 异常场景：无
     * 
     * @return 三种日志的对比说明
     */
    @GetMapping("/comparison")
    public String getLogsComparison() {
        log.info("获取三种日志对比说明");
        return threeLogsDemoService.getLogsComparison();
    }
}
