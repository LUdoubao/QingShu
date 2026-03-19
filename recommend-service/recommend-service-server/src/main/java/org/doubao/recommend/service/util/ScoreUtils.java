package org.doubao.recommend.service.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 评分工具类
 * 提供推荐系统中常用的评分计算和归一化方法
 * 包括时间衰减、数值归一化、对数变换等功能
 */
public final class ScoreUtils {
    
    /**
     * 私有构造函数，防止实例化
     */
    private ScoreUtils() {}

    /**
     * 计算时间衰减因子
     * 基于事件发生时间与当前时间的间隔（小时）计算衰减系数
     * 公式：1.0 / (1.0 + hours / 24.0)
     * 特点：
     * - 刚发生的事件（hours=0）：衰减因子 = 1.0
     * - 1 天前的事件：衰减因子 ≈ 0.5
     * - 7 天前的事件：衰减因子 ≈ 0.125
     * 用于降低旧事件对推荐结果的影响
     *
     * @param time 事件发生时间
     * @return 时间衰减因子（范围：0.0 ~ 1.0）
     */
    public static double timeDecay(LocalDateTime time) {
        if (time == null) {
            return 0.5;  // 默认值，当时间为空时返回中等衰减
        }
        // 计算距离现在的小时数，最小为 0
        long hours = Math.max(0, Duration.between(time, LocalDateTime.now()).toHours());
        // 使用反比例函数计算衰减因子
        return 1.0 / (1.0 + hours / 24.0);
    }

    /**
     * 将数值归一化到 [0, 1] 区间
     * 使用 min-max 归一化方法，将任意范围的数值映射到标准区间
     * 公式：(value - min) / (max - min)
     * 应用场景：
     * - 将不同量纲的分数统一为标准分
     * - 便于多维度分数的加权求和
     *
     * @param value 待归一化的数值
     * @param min 最小值
     * @param max 最大值
     * @return 归一化后的值（范围：0.0 ~ 1.0），如果 max <= min 则返回 0.0
     */
    public static double normalize(double value, double min, double max) {
        if (max <= min) return 0.0;  // 避免除以 0 或负数
        // 归一化并限制在 [0, 1] 范围内
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }

    /**
     * 安全的 log1p 对数变换
     * log1p(x) = ln(1 + x)，适用于处理包含 0 的非负数值
     * 特点：
     * - 可以处理 0 值（log1p(0) = 0）
     * - 对大数值进行压缩，避免过度影响
     * - 单调递增函数，保持相对大小关系
     * 应用场景：
     * - 内容长度奖励（避免过长内容获得过高分数）
     * - 计数类特征的平滑处理
     *
     * @param x 输入值（必须 >= 0）
     * @return log1p(x) 的结果
     */
    public static double safeLog1p(long x) {
        // 确保输入非负，避免数学错误
        return Math.log1p(Math.max(0L, x));
    }
}
