package org.doubao.interview.agent.server.controller.question043;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question043.ScheduledTaskResponse;
import org.doubao.interview.agent.server.service.impl.question043.BasicScheduledTaskDemo;
import org.doubao.interview.agent.server.service.impl.question043.AdvancedScheduledTaskDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Spring 任务调度演示 Controller
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示@Scheduled 的各种使用场景和底层原理
 * 边界：仅用于学习和演示目的，不应用于生产环境
 * 线程安全：Controller 本身无状态，线程安全
 * 
 * 【面试知识点 - 问题 043】
 * 这个 Controller 展示了：
 * 1. fixedRate、fixedDelay、cron 三种定时方式的区别
 * 2. 异步任务的配置和执行
 * 3. 超时控制的实现方式
 * 4. 并发控制和分布式锁的应用
 * 5. 生产环境的最佳实践
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question043")
public class ScheduledTaskController {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Autowired
    private BasicScheduledTaskDemo basicTaskDemo;
    
    @Autowired
    private AdvancedScheduledTaskDemo advancedTaskDemo;
    
    /**
     * 获取基础定时任务说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 fixedRate、fixedDelay、cron 的详细说明和区别
     * 异常场景：无
     * 性能注意点：无
     * 
     * @return 详细说明文本
     */
    @GetMapping("/basic/explanation")
    public String getBasicExplanation() {
        log.info("[ScheduledTaskController] 获取基础定时任务说明");
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== @Scheduled 基础用法详解 ===\n\n");
        
        sb.append("【一、三种定时方式对比】\n\n");
        
        sb.append("1. fixedRate - 固定频率执行\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("定义：从上一次【开始执行】的时间点计算间隔\n");
        sb.append("示例：@Scheduled(fixedRate = 5000) // 每 5 秒执行一次\n\n");
        
        sb.append("时间轴演示：\n");
        sb.append("0s  ━━━━[执行 2 秒]━━━━ 2s ━━━[等待 3 秒]━━━ 5s ━━━━[执行 2 秒]━━━━ 7s ...\n");
        sb.append("      ↑开始                  ↑开始                  ↑开始\n\n");
        
        sb.append("特点：\n");
        sb.append("- 强调\"每隔多久执行一次\"\n");
        sb.append("- 总周期 = max(任务耗时，设定间隔)\n");
        sb.append("- 适合实时性要求高的场景\n\n");
        
        sb.append("适用场景：\n");
        sb.append("✓ 数据采集\n");
        sb.append("✓ 心跳检测\n");
        sb.append("✓ 实时监控\n\n\n");
        
        sb.append("2. fixedDelay - 固定延迟执行\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("定义：从上一次【结束执行】的时间点计算间隔\n");
        sb.append("示例：@Scheduled(fixedDelay = 5000) // 执行完后等 5 秒\n\n");
        
        sb.append("时间轴演示：\n");
        sb.append("0s  ━━━━[执行 3 秒]━━━━ 3s ━━━[等待 5 秒]━━━ 8s ━━━━[执行 3 秒]━━━━ 11s ...\n");
        sb.append("      ↑开始   ↑结束                    ↑开始   ↑结束\n\n");
        
        sb.append("特点：\n");
        sb.append("- 强调\"执行完后休息多久\"\n");
        sb.append("- 总周期 = 任务耗时 + 设定间隔\n");
        sb.append("- 保证两次任务之间有足够的休息\n\n");
        
        sb.append("适用场景：\n");
        sb.append("✓ 邮件发送（避免频繁）\n");
        sb.append("✓ 文件处理（资源恢复）\n");
        sb.append("✓ 第三方 API 调用（遵守限流）\n\n\n");
        
        sb.append("3. cron - Cron 表达式执行\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("定义：使用 Cron 表达式精确指定执行时间\n");
        sb.append("示例：@Scheduled(cron = \"0/10 * * * * *\") // 每 10 秒执行\n\n");
        
        sb.append("Cron 表达式格式（6 位，秒级精度）：\n");
        sb.append("┌───────────── 秒 (0-59)\n");
        sb.append("│ ┌─────────── 分 (0-59)\n");
        sb.append("│ │ ┌───────── 时 (0-23)\n");
        sb.append("│ │ │ ┌─────── 日 (1-31)\n");
        sb.append("│ │ │ │ ┌───── 月 (1-12)\n");
        sb.append("│ │ │ │ │ ┌─── 周 (0-7, 0 和 7=周日)\n");
        sb.append("* * * * * *\n\n");
        
        sb.append("常用示例：\n");
        sb.append("- \"0 0 8 * * ?\" → 每天早上 8 点执行\n");
        sb.append("- \"0 0/30 * * * ?\" → 每 30 分钟执行\n");
        sb.append("- \"0 0 2 * * ?\" → 每天凌晨 2 点执行\n");
        sb.append("- \"0 0 9-17 * * MON-FRI\" → 工作日 9-17 点每小时执行\n");
        sb.append("- \"0 0 0 1 * ?\" → 每月 1 号凌晨执行\n\n");
        
        sb.append("适用场景：\n");
        sb.append("✓ 报表生成（固定时间点）\n");
        sb.append("✓ 数据清理（低峰期执行）\n");
        sb.append("✓ 业务高峰期避让\n\n\n");
        
        sb.append("【二、核心区别总结】\n\n");
        sb.append("假设任务执行耗时 3 秒，调度间隔设置为 5 秒：\n\n");
        
        sb.append("┌─────────────┬──────────────┬──────────────┐\n");
        sb.append("│ 方式        │ 执行时间点   │ 总周期       │\n");
        sb.append("├─────────────┼──────────────┼──────────────┤\n");
        sb.append("│ fixedRate   │ 0,5,10,15... │ 5 秒          │\n");
        sb.append("│ fixedDelay  │ 0,8,16,24... │ 3+5=8 秒      │\n");
        sb.append("│ cron        │ 精确控制     │ 按表达式     │\n");
        sb.append("└─────────────┴──────────────┴──────────────┘\n\n");
        
        sb.append("【三、选择建议】\n\n");
        sb.append("1. 需要高频率、规律性执行 → fixedRate\n");
        sb.append("2. 任务可能耗时较长 → fixedDelay\n");
        sb.append("3. 需要在特定时间执行 → cron\n");
        sb.append("4. 复杂业务场景 → cron（最灵活）\n");
        
        return sb.toString();
    }
    
    /**
     * 获取高级特性说明
     */
    @GetMapping("/advanced/explanation")
    public String getAdvancedExplanation() {
        log.info("[ScheduledTaskController] 获取高级特性说明");
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== @Scheduled 高级特性与生产实践 ===\n\n");
        
        sb.append("【一、异步执行】\n\n");
        sb.append("问题：默认情况下，@Scheduled 任务是串行执行的。如果一个任务很慢，会阻塞其他任务。\n\n");
        
        sb.append("解决方案：使用@Async 注解\n");
        sb.append("@Async\n");
        sb.append("@Scheduled(fixedRate = 3000)\n");
        sb.append("public void asyncTask() { ... }\n\n");
        
        sb.append("注意事项：\n");
        sb.append("1. 必须配置线程池，否则使用 SimpleAsyncTaskExecutor（不推荐）\n");
        sb.append("2. 在启动类添加@EnableAsync 启用异步支持\n");
        sb.append("3. 线程池大小要合理设置（根据 CPU 核数和任务类型）\n\n");
        
        sb.append("线程池配置示例：\n");
        sb.append("@Bean(name = \"taskScheduler\")\n");
        sb.append("public ThreadPoolTaskScheduler taskScheduler() {\n");
        sb.append("    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();\n");
        sb.append("    scheduler.setPoolSize(10);  // 线程池大小\n");
        sb.append("    scheduler.setThreadNamePrefix(\"scheduled-task-\");\n");
        sb.append("    scheduler.setWaitForTasksToCompleteOnShutdown(true);\n");
        sb.append("    scheduler.setAwaitTerminationSeconds(60);\n");
        sb.append("    return scheduler;\n");
        sb.append("}\n\n\n");
        
        sb.append("【二、超时控制】\n\n");
        sb.append("必要性：防止任务执行时间过长，影响后续调度或其他资源。\n\n");
        
        sb.append("实现方式：使用 CompletableFuture\n");
        sb.append("@Scheduled(fixedRate = 10000)\n");
        sb.append("public void taskWithTimeout() {\n");
        sb.append("    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {\n");
        sb.append("        // 业务逻辑\n");
        sb.append("    });\n");
        sb.append("    try {\n");
        sb.append("        future.get(8, TimeUnit.SECONDS); // 超时限制 8 秒\n");
        sb.append("    } catch (TimeoutException e) {\n");
        sb.append("        log.error(\"任务超时\", e);\n");
        sb.append("        // 告警、记录数据库等\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("最佳实践：\n");
        sb.append("✓ 所有定时任务都应该有超时控制\n");
        sb.append("✓ 超时时间略小于任务间隔\n");
        sb.append("✓ 超时要记录日志并告警\n");
        sb.append("✓ 考虑是否需要重试机制\n\n\n");
        
        sb.append("【三、并发控制（单机）】\n\n");
        sb.append("问题：当任务执行时间 > 调度间隔时，可能出现多个任务实例同时执行。\n\n");
        
        sb.append("解决方案：使用 AtomicBoolean\n");
        sb.append("private AtomicBoolean isRunning = new AtomicBoolean(false);\n\n");
        sb.append("@Scheduled(fixedRate = 5000)\n");
        sb.append("public void concurrentTask() {\n");
        sb.append("    if (!isRunning.compareAndSet(false, true)) {\n");
        sb.append("        log.warn(\"前一个任务还在执行，跳过本次\");\n");
        sb.append("        return;\n");
        sb.append("    }\n");
        sb.append("    try {\n");
        sb.append("        // 业务逻辑\n");
        sb.append("    } finally {\n");
        sb.append("        isRunning.set(false); // 释放锁\n");
        sb.append("    }\n");
        sb.append("}\n\n\n");
        
        sb.append("【四、分布式锁（多实例）】\n\n");
        sb.append("问题：应用部署多个实例时，同一个任务会在所有节点同时执行。\n\n");
        
        sb.append("解决方案：使用 Redis 分布式锁\n");
        sb.append("@Scheduled(cron = \"0/15 * * * * *\")\n");
        sb.append("public void distributedTask() {\n");
        sb.append("    String lockKey = \"lock:my-task\";\n");
        sb.append("    String lockValue = getInstanceId();\n\n");
        sb.append("    boolean acquired = tryAcquireLock(lockKey, lockValue);\n");
        sb.append("    if (!acquired) {\n");
        sb.append("        log.warn(\"未获取到锁，跳过执行\");\n");
        sb.append("        return;\n");
        sb.append("    }\n");
        sb.append("    try {\n");
        sb.append("        // 业务逻辑\n");
        sb.append("    } finally {\n");
        sb.append("        releaseLock(lockKey, lockValue);\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("Redis 实现（Lua 脚本保证原子性）：\n");
        sb.append("// 获取锁\n");
        sb.append("String script = \n");
        sb.append("  \"if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then \" +\n");
        sb.append("  \"  redis.call('expire', KEYS[1], 30); return 1; \" +\n");
        sb.append("  \"else return 0; end\";\n");
        sb.append("Long result = redisTemplate.execute(script, ...);\n\n");
        sb.append("// 释放锁\n");
        sb.append("String script = \n");
        sb.append("  \"if redis.call('get', KEYS[1]) == ARGV[1] then \" +\n");
        sb.append("  \"  return redis.call('del', KEYS[1]); else return 0; end\";\n\n");
        
        sb.append("生产环境建议：\n");
        sb.append("✓ 使用 Redisson 等成熟框架\n");
        sb.append("✓ 考虑锁的续期机制（WatchDog）\n");
        sb.append("✓ 考虑主从切换时的锁安全性\n");
        sb.append("✓ 设置合理的锁过期时间\n\n\n");
        
        sb.append("【五、错误处理】\n\n");
        sb.append("Spring 默认行为：\n");
        sb.append("- 任务抛出异常时，会被 TaskScheduler 捕获\n");
        sb.append("- 不会影响其他任务的执行\n");
        sb.append("- 但当前任务的下一次调度会正常进行\n\n");
        
        sb.append("最佳实践：\n");
        sb.append("@Scheduled(fixedRate = 5000)\n");
        sb.append("public void robustTask() {\n");
        sb.append("    try {\n");
        sb.append("        // 业务逻辑\n");
        sb.append("    } catch (Exception e) {\n");
        sb.append("        log.error(\"任务执行失败\", e);\n");
        sb.append("        // 记录数据库、发送告警等\n");
        sb.append("        // 注意：不要重新抛出异常，让调度器继续调度\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("【六、动态控制（扩展）】\n\n");
        sb.append("可以使用 SchedulingConfigurer 接口实现动态任务：\n");
        sb.append("@Component\n");
        sb.append("public class DynamicScheduleConfig implements SchedulingConfigurer {\n");
        sb.append("    @Override\n");
        sb.append("    public void configureTasks(ScheduledTaskRegistrar registrar) {\n");
        sb.append("        registrar.addTriggerTask(\n");
        sb.append("            () -> doSomething(),\n");
        sb.append("            (context) -> {\n");
        sb.append("                // 动态计算下次执行时间\n");
        sb.append("                return getNextExecutionTime();\n");
        sb.append("            }\n");
        sb.append("        );\n");
        sb.append("    }\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    /**
     * 获取任务执行统计
     */
    @GetMapping("/statistics")
    public String getStatistics() {
        log.info("[ScheduledTaskController] 获取任务执行统计");
        String basicStats = basicTaskDemo.getTaskStatistics();
        return basicStats + "\n注意：高级任务的统计信息需要通过日志查看";
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        log.info("[ScheduledTaskController] 健康检查，当前时间={}", sdf.format(new Date()));
        return "Question043: Spring Scheduled Tasks - OK | Time: " + sdf.format(new Date());
    }
}
