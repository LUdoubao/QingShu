package org.doubao.interview.agent.server.controller.question014;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question014.PurchaseRequest;
import org.doubao.interview.agent.api.dto.question014.PurchaseResponse;
import org.doubao.interview.agent.api.service.question014.InnodbLockDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * InnoDB 锁演示 Controller - 提供 RESTful API 接口
 * 
 * 对应面试知识点：问题 014 - InnoDB 锁的分类
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示 InnoDB 各种锁的使用场景
 * 边界：仅用于学习和演示，不提供认证授权等生产功能
 * 线程安全：Controller 本身无状态，线程安全；业务逻辑的并发控制由 Service 层负责
 * 幂等性：购买接口的幂等性由 orderNo 保证
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question014")
public class InnodbLockDemoController {

    @Autowired
    private InnodbLockDemoService innodbLockDemoService;

    /**
     * 执行购买操作 - 演示 InnoDB 锁的使用
     * 
     * 【方法注释】
     * 输入约束：请求参数必须通过校验（userId、productId 非空，quantity>0）
     * 输出语义：返回购买结果和使用的锁信息
     * 异常场景：参数校验失败返回 400；业务异常返回 500
     * 性能注意点：涉及数据库事务和锁，避免高频调用
     * 
     * @param request 购买请求参数
     * @return 购买响应，包含锁的详细信息
     */
    @PostMapping("/purchase")
    public PurchaseResponse purchase(@Valid @RequestBody PurchaseRequest request) {
        log.info("收到购买请求，userId={}, productId={}, quantity={}, lockMode={}", 
                request.getUserId(), request.getProductId(), 
                request.getQuantity(), request.getLockMode());
        
        PurchaseResponse response = innodbLockDemoService.purchase(request);
        
        log.info("购买处理完成，orderNo={}, success={}", 
                response.getOrderNo(), response.getSuccess());
        
        return response;
    }

    /**
     * 查询商品信息
     * 
     * 【方法注释】
     * 输入约束：productId 必须为正整数
     * 输出语义：返回商品的 JSON 格式信息
     * 异常场景：商品不存在返回 null
     * 
     * @param productId 商品 ID
     * @return 商品信息（JSON 字符串）
     */
    @GetMapping("/product/{productId}")
    public String getProductInfo(@PathVariable Long productId) {
        log.info("查询商品信息，productId={}", productId);
        return innodbLockDemoService.getProductInfo(productId);
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
        String result = innodbLockDemoService.initDemoData();
        log.info("演示数据初始化完成");
        return result;
    }

    /**
     * 获取 InnoDB 锁的详细分类说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 Markdown 格式的对比说明文档
     * 异常场景：无
     * 
     * @return InnoDB 锁的分类说明
     */
    @GetMapping("/classification")
    public String getLockClassification() {
        log.info("获取 InnoDB 锁分类说明");
        return innodbLockDemoService.getLockClassification();
    }
}
