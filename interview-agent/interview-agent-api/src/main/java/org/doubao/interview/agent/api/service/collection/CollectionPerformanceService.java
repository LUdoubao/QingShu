package org.doubao.interview.agent.api.service.collection;

import org.doubao.interview.agent.api.dto.collection.CollectionPerformanceReportDTO;

/**
 * 集合性能对比服务接口
 * 
 * 【职责】提供 ArrayList 与 LinkedList 的性能对比测试能力
 * 【边界】仅用于面试演示，不应用于生产环境
 * 【线程安全】实现类需保证线程安全
 * 
 * @author interview-agent
 * @date 2026-04-05
 */
public interface CollectionPerformanceService {

    /**
     * 执行完整的性能对比测试
     * 
     * 【输入约束】无参数，使用预设的测试数据量
     * 【输出语义】返回包含多个测试场景的综合报告
     * 【异常场景】内存不足时可能抛出 OutOfMemoryError
     * 【性能注意点】测试过程会创建大量对象，建议在独立环境中运行
     *
     * @return 性能对比报告
     */
    CollectionPerformanceReportDTO runFullComparison();

    /**
     * 执行指定数据量的性能对比测试
     *
     * 【输入约束】dataSize 必须在 [1000, 1000000] 范围内
     * 【输出语义】返回指定数据量下的性能对比结果
     * 【异常场景】
     *   - dataSize 超出范围抛出 IllegalArgumentException
     *   - 内存不足时可能抛出 OutOfMemoryError
     *
     * @param dataSize 测试数据量（元素个数）
     * @return 性能对比报告
     */
    CollectionPerformanceReportDTO runComparisonWithDataSize(int dataSize);
}
