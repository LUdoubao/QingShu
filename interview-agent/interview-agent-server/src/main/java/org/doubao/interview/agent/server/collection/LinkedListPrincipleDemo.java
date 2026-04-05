package org.doubao.interview.agent.server.collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.LinkedList;
import java.util.List;
import java.util.Deque;

/**
 * LinkedList 原理演示 Service
 * 展示 Java 原生 LinkedList 的核心特性及其底层逻辑
 */
@Slf4j
@Service
public class LinkedListPrincipleDemo {

    /**
     * 演示核心操作：增、删、改、查
     */
    public void demonstrateCoreOperations() {
        log.info("--- 演示 LinkedList 核心操作 ---");
        // LinkedList 实现了 List 接口
        List<String> list = new LinkedList<>();

        // 1. 添加元素 (默认尾部添加，O(1))
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        log.info("初始列表: {}", list);

        // 2. 指定位置添加 (需遍历定位，O(n))
        list.add(1, "Orange");
        log.info("在索引 1 插入 Orange 后: {}", list);

        // 3. 随机访问 (由于是链表，需遍历定位，O(n))
        String fruit = list.get(2);
        log.info("索引 2 的元素是: {}", fruit);

        // 4. 删除元素 (需遍历定位，O(n))
        list.remove("Banana");
        log.info("删除 Banana 后: {}", list);

        // 5. 修改元素 (需遍历定位，O(n))
        list.set(0, "Red Apple");
        log.info("修改索引 0 后的列表: {}", list);
    }

    /**
     * 演示双端队列 (Deque) 特性
     * LinkedList 实现了 Deque 接口，可作为队列、栈、双端队列使用
     */
    public void demonstrateDequeFeatures() {
        log.info("--- 演示 LinkedList 双端队列 (Deque) 特性 ---");
        Deque<String> deque = new LinkedList<>();

        // 1. 队列 (FIFO: First-In-First-Out)
        deque.offer("Queue-Task1");
        deque.offer("Queue-Task2");
        log.info("队列入队后: {}", deque);
        log.info("队列出队 (poll): {}", deque.poll());
        log.info("出队后剩余: {}", deque);

        // 2. 栈 (LIFO: Last-In-First-Out)
        deque.push("Stack-Element1");
        deque.push("Stack-Element2");
        log.info("栈入栈后 (push): {}", deque);
        log.info("栈出栈 (pop): {}", deque.pop());
        log.info("出栈后剩余: {}", deque);

        // 3. 双端操作
        deque.addFirst("Head-Element");
        deque.addLast("Tail-Element");
        log.info("双端操作后: {}", deque);
    }

    /**
     * 演示简易自定义实现 SimpleLinkedList 的底层逻辑
     */
    public String demonstrateSimpleImplementation() {
        log.info("--- 演示自定义 SimpleLinkedList 底层原理实现 ---");
        SimpleLinkedList<Integer> simpleList = new SimpleLinkedList<>();

        // 1. 头尾高效添加 (O(1))
        simpleList.add(10);
        simpleList.add(20);
        simpleList.addFirst(5);
        log.info("SimpleLinkedList 添加后: {}", simpleList);

        // 2. 随机访问 (内部包含二分优化遍历)
        Integer val = simpleList.get(1);
        log.info("SimpleLinkedList 索引 1 的值: {}", val);

        // 3. 中间删除 (O(n))
        simpleList.remove(1);
        log.info("SimpleLinkedList 删除索引 1 后: {}", simpleList);

        return "SimpleLinkedList 演示成功，结果：" + simpleList.toString();
    }
}
