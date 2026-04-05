package org.doubao.interview.agent.server.controller.collection;

import lombok.extern.slf4j.Slf4j;
import org.doubao.mall.common.entity.Result;
import org.doubao.interview.agent.api.dto.collection.CollectionPerformanceReportDTO;
import org.doubao.interview.agent.api.service.collection.CollectionPerformanceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * ArrayList vs LinkedList 性能对比控制器
 * 
 * 【职责】提供 HTTP 接口，暴露集合性能测试能力
 * 【边界】
 *   - 仅用于面试演示和教学，不应用于生产环境
 *   - 测试过程耗时较长，建议设置合理的超时时间
 * 【线程安全】依赖的服务实现类无状态，控制器本身线程安全
 * 
 * @author interview-agent
 * @date 2026-04-05
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/collection")
public class CollectionPerformanceController {

    @Resource
    private CollectionPerformanceService collectionPerformanceService;

    /**
     * 执行完整的性能对比测试
     * 
     * 【输入约束】无参数，使用默认数据量（100,000）
     * 【输出语义】返回包含 5 个测试场景的综合报告
     * 【异常场景】内存不足时可能抛出 OutOfMemoryError
     * 【性能注意点】测试耗时约 1-5 秒，取决于硬件性能
     *
     * @return 性能对比报告
     */
    @GetMapping("/compare/full")
    public Result<CollectionPerformanceReportDTO> runFullComparison() {
        log.info("收到完整性能对比测试请求");
        long startTime = System.currentTimeMillis();

        try {
            CollectionPerformanceReportDTO report = collectionPerformanceService.runFullComparison();
            long elapsed = System.currentTimeMillis() - startTime;
            
            log.info("完整性能对比测试完成, 耗时={}ms", elapsed);
            return Result.success(report);
        } catch (OutOfMemoryError e) {
            log.error("性能测试失败：内存不足, traceId={}", System.currentTimeMillis(), e);
            return Result.error("内存不足，请减少测试数据量或增加 JVM 堆内存");
        } catch (Exception e) {
            log.error("性能测试失败, traceId={}", System.currentTimeMillis(), e);
            return Result.error("测试执行失败: " + e.getMessage());
        }
    }

    /**
     * 执行指定数据量的性能对比测试
     * 
     * 【输入约束】dataSize 必须在 [1000, 1000000] 范围内
     * 【输出语义】返回指定数据量下的性能对比结果
     * 【异常场景】
     *   - dataSize 超出范围返回参数错误
     *   - 内存不足时可能抛出 OutOfMemoryError
     *
     * @param dataSize 测试数据量（元素个数）
     * @return 性能对比报告
     */
    @GetMapping("/compare/custom")
    public Result<CollectionPerformanceReportDTO> runCustomComparison(
            @RequestParam("dataSize") int dataSize) {
        
        log.info("收到自定义数据量性能对比测试请求, dataSize={}", dataSize);
        long startTime = System.currentTimeMillis();

        // 参数校验
        if (dataSize < 1000 || dataSize > 1000000) {
            log.warn("参数校验失败: dataSize={} 超出允许范围 [1000, 1000000]", dataSize);
            return Result.error("数据量必须在 1000 到 1000000 之间");
        }

        try {
            CollectionPerformanceReportDTO report = 
                collectionPerformanceService.runComparisonWithDataSize(dataSize);
            long elapsed = System.currentTimeMillis() - startTime;
            
            log.info("自定义数据量测试完成, dataSize={}, 耗时={}ms", dataSize, elapsed);
            return Result.success(report);
        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (OutOfMemoryError e) {
            log.error("性能测试失败：内存不足, dataSize={}, traceId={}", 
                     dataSize, System.currentTimeMillis(), e);
            return Result.error("内存不足，请减少测试数据量或增加 JVM 堆内存");
        } catch (Exception e) {
            log.error("性能测试失败, dataSize={}, traceId={}", 
                     dataSize, System.currentTimeMillis(), e);
            return Result.error("测试执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取面试知识点总结
     * 
     * 【为什么提供此接口】方便前端直接展示核心考点，无需解析测试数据
     * 【输出语义】返回结构化的面试要点
     *
     * @return 面试知识点总结
     */
    @GetMapping("/knowledge/summary")
    public Result<String> getKnowledgeSummary() {
        log.info("收到面试知识点总结请求");

        String summary = "## ArrayList vs LinkedList 核心区别\n\n" +
                "### 1. 底层数据结构\n" +
                "- ArrayList：动态数组（Object[]）\n" +
                "- LinkedList：双向链表（Node 节点含 prev/next 指针）\n\n" +
                "### 2. 性能特性\n" +
                "- 随机访问：ArrayList O(1) >> LinkedList O(n)\n" +
                "- 尾部增删：两者均为 O(1)（ArrayList 无扩容时）\n" +
                "- 头部增删：LinkedList O(1) >> ArrayList O(n)\n" +
                "- 中间增删：需综合考虑查找+修改成本\n\n" +
                "### 3. 功能差异\n" +
                "- LinkedList 实现 Deque 接口，支持队列/栈操作\n" +
                "- ArrayList 仅实现 List 接口\n\n" +
                "### 4. 内存占用\n" +
                "- ArrayList：连续内存，仅存储元素 + 扩容预留空间\n" +
                "- LinkedList：每个节点额外存储 2 个指针，内存碎片化\n\n" +
                "### 5. 选型原则\n" +
                "- 读多写少 → ArrayList\n" +
                "- 写多读少 → LinkedList\n" +
                "- 需要队列/栈 → LinkedList\n" +
                "- 不确定场景 → 优先 ArrayList";

        return Result.success(summary);
    }
}
