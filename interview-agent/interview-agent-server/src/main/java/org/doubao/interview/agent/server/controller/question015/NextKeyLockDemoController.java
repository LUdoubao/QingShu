package org.doubao.interview.agent.server.controller.question015;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question015.QueryRequest;
import org.doubao.interview.agent.api.dto.question015.QueryResponse;
import org.doubao.interview.agent.api.service.question015.NextKeyLockDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Next-Key Lock 演示 Controller - 提供 RESTful API 接口
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示 Next-Key Lock 如何避免幻读
 * 边界：仅用于学习和演示，不提供认证授权等生产功能
 * 线程安全：Controller 本身无状态，线程安全；业务逻辑的并发控制由 Service 层负责
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question015")
public class NextKeyLockDemoController {

    @Autowired
    private NextKeyLockDemoService nextKeyLockDemoService;

    /**
     * 执行查询操作 - 演示 Next-Key Lock 的效果
     * 
     * 【方法注释】
     * 输入约束：请求参数必须通过校验（sessionId 非空，queryType 正确）
     * 输出语义：返回查询结果和使用的锁信息
     * 异常场景：参数校验失败返回 400；业务异常返回 500
     * 性能注意点：涉及数据库事务和锁，避免高频调用
     * 
     * @param request 查询请求参数
     * @return 查询响应，包含锁信息和是否检测到幻读
     */
    @PostMapping("/query")
    public QueryResponse query(@Valid @RequestBody QueryRequest request) {
        log.info("收到查询请求，sessionId={}, queryType={}, useNextKeyLock={}", 
                request.getSessionId(), request.getQueryType(), 
                request.getUseNextKeyLock());
        
        QueryResponse response = nextKeyLockDemoService.query(request);
        
        log.info("查询处理完成，sessionId={}, resultCount={}, phantomDetected={}", 
                response.getSessionId(), response.getResultCount(), 
                response.getPhantomDetected());
        
        return response;
    }

    /**
     * 模拟插入操作
     * 
     * 【方法注释】
     * 输入约束：sessionId、userName、balance 不能为空
     * 输出语义：返回插入结果
     * 异常场景：违反唯一约束时抛出异常
     * 
     * @param sessionId 会话 ID
     * @param userName 用户名称
     * @param balance 初始余额
     * @return 插入结果描述
     */
    @PostMapping("/insert")
    public String simulateInsert(
            @RequestParam String sessionId,
            @RequestParam String userName,
            @RequestParam String balance) {
        log.info("收到插入请求，sessionId={}, userName={}", sessionId, userName);
        return nextKeyLockDemoService.simulateInsert(sessionId, userName, balance);
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
        String result = nextKeyLockDemoService.initDemoData();
        log.info("演示数据初始化完成");
        return result;
    }

    /**
     * 获取 Next-Key Lock 的详细说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 Markdown 格式的说明文档
     * 异常场景：无
     * 
     * @return Next-Key Lock 的详细说明
     */
    @GetMapping("/explanation")
    public String getNextKeyLockExplanation() {
        log.info("获取 Next-Key Lock 说明");
        return nextKeyLockDemoService.getNextKeyLockExplanation();
    }
}
