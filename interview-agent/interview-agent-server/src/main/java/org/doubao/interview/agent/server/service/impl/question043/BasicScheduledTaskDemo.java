package org.doubao.interview.agent.server.service.impl.question043;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基础定时任务演示服务
 * 
 * 【类注释】
 * 职责：演示@Scheduled 的三种基本使用方式：fixedRate、fixedDelay、cron
 * 边界：仅用于学习和演示，实际业务中需要更完善的错误处理和监控
 * 线程安全：使用 AtomicLong 保证计数器的线程安全
 * 是否幂等：每个任务都是幂等的
 * 
 * 【面试知识点 - 问题 043】
 * 这个类展示了 Spring 定时任务的三种基本模式：
 * 1. fixedRate: 固定频率执行，从上一次开始执行的时间点计算间隔
 * 2. fixedDelay: 固定延迟执行，从上一次结束执行的时间点计算间隔
 * 3. cron: 使用 Cron 表达式灵活定义执行时间
 * 
 * 【核心区别】
 * - fixedRate: 强调"每隔多久执行一次"，不管任务是否执行完
 * - fixedDelay: 强调"执行完后间隔多久再执行"，保证任务间有足够间隔
 * - cron: 精确控制"在什么时间点执行"，适合业务高峰期避让
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class BasicScheduledTaskDemo {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    // 任务执行计数器（使用 AtomicLong 保证线程安全）
    private AtomicLong fixedRateCount = new AtomicLong(0);
    private AtomicLong fixedDelayCount = new AtomicLong(0);
    private AtomicLong cronCount = new AtomicLong(0);
    
    // 记录上次执行完成时间（用于演示 fixedDelay）
    private volatile long lastCompletionTime = 0;
    
    /**
     * 固定频率任务
     * 
     * 【关键代码段注释】
     * fixedRate = 5000 的含义：
     * 1. 每隔 5 秒执行一次，从上一次"开始执行"的时间点计算
     * 2. 如果任务执行耗时 2 秒，那么等待 3 秒后再次执行
     * 3. 如果任务执行耗时超过 5 秒，会立即执行下一次（没有等待）
     * 4. 默认情况下，任务是串行执行的，前一个任务没执行完不会启动下一个
     * 
     * 【适用场景】
     * - 数据采集：每隔固定时间采集一次数据
     * - 心跳检测：定期发送心跳包
     * - 实时性要求高的场景
     * 
     * 【注意事项】
     * - 如果任务可能长时间运行，要配置异步执行（否则会影响其他任务）
     * - 要考虑任务执行时间超过间隔时间的情况
     */
    @Scheduled(fixedRate = 5000000)
    public void executeFixedRate() {
        long startTime = System.currentTimeMillis();
        fixedRateCount.incrementAndGet();
        
        log.info("[FixedRate] ===== 开始执行第 {} 次 =====", fixedRateCount.get());
        log.info("[FixedRate] 当前时间：{}", sdf.format(new Date()));
        
        // 模拟业务处理：耗时 2 秒
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[FixedRate] 任务被中断", e);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("[FixedRate] 执行完成，耗时={}ms, 下次执行间隔=5 秒-fixedRate", duration);
    }
    
    /**
     * 固定延迟任务
     * 
     * 【关键代码段注释】
     * fixedDelay = 5000 的含义：
     * 1. 任务执行完成后，等待 5 秒再执行下一次
     * 2. 总间隔 = 任务执行时间 + 5 秒等待时间
     * 3. 保证两次任务之间有足够的休息时间
     * 
     * 【与 fixedRate 的关键区别】
     * 假设任务执行耗时 2 秒：
     * - fixedRate(5000): 每 5 秒执行一次（0s 开始，5s 开始，10s 开始...）
     * - fixedDelay(5000): 每次执行完后等 5 秒（0s 开始并执行到 2s，7s 开始并执行到 9s，14s 开始...）
     * 
     * 【适用场景】
     * - 邮件发送：避免频繁发送，每次发送后要有间隔
     * - 文件处理：给系统资源恢复的时间
     * - 第三方 API 调用：遵守速率限制
     */
    @Scheduled(fixedDelay = 5000000)
    public void executeFixedDelay() {
        long startTime = System.currentTimeMillis();
        fixedDelayCount.incrementAndGet();
        
        long waitTime = (lastCompletionTime > 0) ? (startTime - lastCompletionTime) : 0;
        
        log.info("[FixedDelay] ===== 开始执行第 {} 次 (距离上次完成已过去{}ms) =====", 
                fixedDelayCount.get(), waitTime);
        log.info("[FixedDelay] 当前时间：{}", sdf.format(new Date()));
        
        // 模拟业务处理：耗时 3 秒
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[FixedDelay] 任务被中断", e);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        lastCompletionTime = System.currentTimeMillis();
        
        log.info("[FixedDelay] 执行完成，耗时={}ms, 等待{}ms 后执行下一次", duration, 5000);
    }
    
    /**
     * Cron 表达式任务
     * 
     * 【关键代码段注释】
     * cron = "0/10 * * * * *" 的含义（6 位格式，秒级精度）：
     * 格式：秒 分 时 日 月 周
     * 0/10 * * * * * = 每 10 秒执行一次（在第 0、10、20、30、40、50 秒）
     * 
     * Cron 表达式详解：
     * - 第 1 位（秒）：0-59
     * - 第 2 位（分）：0-59
     * - 第 3 位（时）：0-23
     * - 第 4 位（日）：1-31
     * - 第 5 位（月）：1-12
     * - 第 6 位（周）：0-7（0 和 7 都代表周日）
     * 
     * 常用示例：
     * - "0 0 8 * * ?" = 每天早上 8 点执行
     * - "0 0/30 * * * ?" = 每 30 分钟执行
     * - "0 0 2 * * ?" = 每天凌晨 2 点执行
     * - "0 0 9-17 * * MON-FRI" = 工作日朝九晚五每小时执行
     * 
     * 【适用场景】
     * - 报表生成：每天凌晨执行
     * - 数据清理：每周执行一次
     * - 业务高峰期避让：只在特定时间段执行
     */
    @Scheduled(cron = "0/60 * * * * *")
    public void executeCron() {
        cronCount.incrementAndGet();
        
        log.info("[Cron] ★★★★★ 执行第 {} 次 (Cron: 每 10 秒) ★★★★★", cronCount.get());
        log.info("[Cron] 当前时间：{}", sdf.format(new Date()));
        log.info("[Cron] 精确到秒的定时任务");
    }
    
    /**
     * 获取任务执行统计
     */
    public String getTaskStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 任务执行统计 ===\n");
        sb.append(String.format("FixedRate 任务：执行 %d 次\n", fixedRateCount.get()));
        sb.append(String.format("FixedDelay 任务：执行 %d 次\n", fixedDelayCount.get()));
        sb.append(String.format("Cron 任务：执行 %d 次\n", cronCount.get()));
        return sb.toString();
    }
}
