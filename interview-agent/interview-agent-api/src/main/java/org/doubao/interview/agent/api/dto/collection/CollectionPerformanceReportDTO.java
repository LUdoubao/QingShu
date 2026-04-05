package org.doubao.interview.agent.api.dto.collection;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 集合性能测试综合报告 DTO
 * 
 * 【职责】汇总多个场景的性能测试结果，形成完整的对比报告
 * 【边界】用于一次性返回所有测试场景的结果，便于前端展示
 * 【线程安全】不可变对象，天然线程安全
 * 
 * @author interview-agent
 * @date 2026-04-05
 */
@Data
public class CollectionPerformanceReportDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 各场景性能对比结果列表
     */
    private List<CollectionPerformanceDTO> results;

    /**
     * 测试总结（核心结论）
     */
    private String summary;

    /**
     * 推荐场景说明
     */
    private String recommendation;

    public CollectionPerformanceReportDTO() {
    }

    public CollectionPerformanceReportDTO(List<CollectionPerformanceDTO> results, 
                                          String summary, 
                                          String recommendation) {
        this.results = results;
        this.summary = summary;
        this.recommendation = recommendation;
    }
}
