package org.doubao.interview.agent.api.dto.collection;

import lombok.Data;
import java.io.Serializable;

/**
 * ArrayList vs LinkedList 性能对比结果 DTO
 * 
 * 【职责】封装两种 List 实现的性能测试数据，用于面试演示和性能分析
 * 【边界】仅用于展示性能差异，不包含业务逻辑
 * 【线程安全】不可变对象，天然线程安全
 * 
 * @author interview-agent
 * @date 2026-04-05
 */
@Data
public class CollectionPerformanceDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 测试场景名称（如：随机访问、尾部添加、中间插入等）
     */
    private String scenario;

    /**
     * ArrayList 耗时（毫秒）
     */
    private Long arrayListTimeMs;

    /**
     * LinkedList 耗时（毫秒）
     */
    private Long linkedListTimeMs;

    /**
     * 性能倍数（LinkedList / ArrayList，值越大表示 ArrayList 越快）
     */
    private Double performanceRatio;

    /**
     * 性能优势方（"ArrayList" / "LinkedList" / "相当"）
     */
    private String winner;

    /**
     * 测试数据量
     */
    private Integer dataSize;

    /**
     * 补充说明（解释性能差异的原因）
     */
    private String explanation;

    /**
     * 构造完整的性能对比结果
     *
     * @param scenario 测试场景
     * @param arrayListTime ArrayList 耗时
     * @param linkedListTime LinkedList 耗时
     * @param dataSize 数据量
     * @param explanation 说明
     */
    public CollectionPerformanceDTO(String scenario, Long arrayListTime, 
                                     Long linkedListTime, Integer dataSize, 
                                     String explanation) {
        this.scenario = scenario;
        this.arrayListTimeMs = arrayListTime;
        this.linkedListTimeMs = linkedListTime;
        this.dataSize = dataSize;
        this.explanation = explanation;
        
        // 计算性能比率，避免除零错误
        if (arrayListTime > 0 && linkedListTime > 0) {
            this.performanceRatio = Math.round((double) linkedListTime / arrayListTime * 100.0) / 100.0;
            if (arrayListTime < linkedListTime) {
                this.winner = "ArrayList";
            } else if (linkedListTime < arrayListTime) {
                this.winner = "LinkedList";
            } else {
                this.winner = "相当";
            }
        } else {
            this.performanceRatio = 0.0;
            this.winner = "无法比较";
        }
    }

    /**
     * 默认构造器（JSON 反序列化需要）
     */
    public CollectionPerformanceDTO() {
    }
}
