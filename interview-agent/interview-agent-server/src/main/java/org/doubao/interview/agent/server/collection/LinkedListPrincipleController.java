package org.doubao.interview.agent.server.collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * LinkedList 原理验证控制器
 * 提供 REST 接口演示 LinkedList 的核心底层实现
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/collection/linkedlist")
public class LinkedListPrincipleController {

    @Autowired
    private LinkedListPrincipleDemo linkedListPrincipleDemo;

    /**
     * 演示 LinkedList 核心操作
     */
    @GetMapping("/core-operations")
    public Map<String, Object> testCoreOperations() {
        log.info("开始演示 LinkedList 核心操作...");
        linkedListPrincipleDemo.demonstrateCoreOperations();
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "LinkedList 核心操作演示成功，请查看控制台日志。");
        return result;
    }

    /**
     * 演示 Deque 特性
     */
    @GetMapping("/deque-features")
    public Map<String, Object> testDequeFeatures() {
        log.info("开始演示 LinkedList 双端队列特性...");
        linkedListPrincipleDemo.demonstrateDequeFeatures();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "LinkedList 双端队列特性演示成功，请查看控制台日志。");
        return result;
    }

    /**
     * 演示 SimpleLinkedList 自定义实现
     */
    @GetMapping("/simple-implementation")
    public Map<String, Object> testSimpleImplementation() {
        log.info("开始演示 SimpleLinkedList 自定义实现...");
        String summary = linkedListPrincipleDemo.demonstrateSimpleImplementation();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "SimpleLinkedList 演示成功。");
        result.put("data", summary);
        return result;
    }

    /**
     * 获取 LinkedList 底层原理总结
     */
    @GetMapping("/principle-summary")
    public Map<String, Object> getPrincipleSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("1. 底层结构", "基于双向链表实现，每个节点包含元素、前驱指针和后继指针。");
        summary.put("2. 核心指针", "维护 first 头指针和 last 尾指针，支持 O(1) 的头尾操作。");
        summary.put("3. 扩容机制", "无扩容，元素动态添加，不需要预先分配内存。");
        summary.put("4. 操作性能", "头尾增删 O(1)，随机访问/中间增删 O(n)。");
        summary.put("5. 查询优化", "根据索引位置决定从头还是从尾遍历（二分思想）。");
        summary.put("6. 线程安全", "非线程安全，无锁设计，并发修改会抛出 ConcurrentModificationException。");
        summary.put("7. 适用场景", "写多读少、频繁操作头尾元素的场景（如队列、栈、消息队列）。");
        return summary;
    }
}
