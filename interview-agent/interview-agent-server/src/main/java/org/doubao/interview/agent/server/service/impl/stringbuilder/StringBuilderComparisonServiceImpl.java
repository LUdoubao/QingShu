package org.doubao.interview.agent.server.service.impl.stringbuilder;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.stringbuilder.StringBuilderComparisonRequest;
import org.doubao.interview.agent.api.dto.stringbuilder.StringBuilderComparisonResponse;
import org.doubao.interview.agent.api.service.stringbuilder.StringBuilderComparisonService;
import org.springframework.stereotype.Service;

/**
 * String、StringBuffer、StringBuilder 区别演示服务实现类
 * <p>
 * 提供三种字符串类的详细对比和性能测试
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@Service
public class StringBuilderComparisonServiceImpl implements StringBuilderComparisonService {

    /**
     * String 类型常量
     */
    private static final String STRING = "STRING";

    /**
     * StringBuffer 类型常量
     */
    private static final String STRINGBUFFER = "STRINGBUFFER";

    /**
     * StringBuilder 类型常量
     */
    private static final String STRINGBUILDER = "STRINGBUILDER";

    /**
     * 默认循环次数（用于性能测试）
     */
    private static final int DEFAULT_LOOP_COUNT = 10000;

    /**
     * 演示字符串类特性
     * <p>
     * 根据请求的字符串类类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的字符串类类型
     * @return 演示结果响应，包含特性说明、性能测试、对比表格等
     */
    @Override
    public StringBuilderComparisonResponse demonstrateFeature(StringBuilderComparisonRequest request) {
        log.info("开始演示字符串类特性，stringType={}, loopCount={}", 
                request.getStringType(), request.getLoopCount());

        String stringType = request.getStringType();
        
        // 根据字符串类型分发到不同的演示方法
        if (STRING.equalsIgnoreCase(stringType)) {
            return demonstrateString(request);
        } else if (STRINGBUFFER.equalsIgnoreCase(stringType)) {
            return demonstrateStringBuffer(request);
        } else if (STRINGBUILDER.equalsIgnoreCase(stringType)) {
            return demonstrateStringBuilder(request);
        } else {
            // 兜底逻辑：未知的字符串类型
            log.warn("未知的字符串类型：{}, 进入兜底流程", stringType);
            return createErrorResponse(stringType, "未知的字符串类型，可选值：STRING、STRINGBUFFER、STRINGBUILDER");
        }
    }

    /**
     * 演示 String 类特性
     * <p>
     * String 是不可变字符序列，每次修改都会创建新对象
     * </p>
     *
     * @param request 请求参数
     * @return String 类的演示结果
     */
    private StringBuilderComparisonResponse demonstrateString(StringBuilderComparisonRequest request) {
        int loopCount = request.getLoopCount() != null ? request.getLoopCount() : DEFAULT_LOOP_COUNT;
        
        // 性能测试：字符串拼接
        long startTime = System.currentTimeMillis();
        String result = "";
        for (int i = 0; i < loopCount; i++) {
            result += String.valueOf(i);
        }
        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;

        String description = "String 是不可变字符序列，每次修改（如拼接）都会创建新的 String 对象。" +
                "性能测试：循环 " + loopCount + " 次进行字符串拼接，耗时 " + costTime + "ms。" +
                "由于每次都创建新对象，内存开销大，效率相对较低。";

        String keyFeatures = "1. 不可变性：String 一旦创建，其值就不能改变\n" +
                            "2. 线程安全：不可变性天然保证线程安全\n" +
                            "3. 字符串常量池：支持字符串共享，节省内存\n" +
                            "4. 效率较低：每次修改都创建新对象\n" +
                            "5. final 修饰：不能被继承";

        String codeExample = "// 1. String 的不可变性\n" +
                            "String str = \"hello\";\n" +
                            "str.concat(\" world\");  // str 仍然是 \"hello\"\n" +
                            "System.out.println(str);  // 输出：hello\n\n" +
                            
                            "// 2. 字符串拼接（低效）\n" +
                            "String result = \"\";\n" +
                            "for (int i = 0; i < 10000; i++) {\n" +
                            "    result += i;  // 每次创建新对象\n" +
                            "}\n\n" +
                            
                            "// 3. 字符串常量池\n" +
                            "String s1 = \"hello\";\n" +
                            "String s2 = \"hello\";\n" +
                            "System.out.println(s1 == s2);  // true（同一对象）";

        String comparisonTable = "| 特性 | String |\n" +
                                "|------|--------|\n" +
                                "| 可变性 | 不可变 |\n" +
                                "| 线程安全 | ✓ 安全 |\n" +
                                "| 同步锁 | ✗ 无 |\n" +
                                "| 性能 | 低（单线程大量拼接） |\n" +
                                "| 内存占用 | 高（频繁创建对象） |\n" +
                                "| 适用场景 | 少量操作、常量、多线程共享 |";

        String usageScenarios = "推荐使用场景：\n" +
                               "1. 字符串不需要频繁修改\n" +
                               "2. 作为常量使用\n" +
                               "3. 多线程环境下的共享数据\n" +
                               "4. 需要作为 HashMap/HashSet 的 key\n" +
                               "\n不推荐场景：\n" +
                               "1. 循环中大量字符串拼接\n" +
                               "2. 频繁修改字符串内容";

        log.info("String 演示完成，循环次数：{}, 耗时：{}ms", loopCount, costTime);

        return StringBuilderComparisonResponse.builder()
                .stringType(STRING)
                .typeName("String")
                .description(description)
                .keyFeatures(keyFeatures)
                .performanceTime(costTime)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .success(true)
                .build();
    }

    /**
     * 演示 StringBuffer 类特性
     * <p>
     * StringBuffer 是可变字符序列，线程安全（synchronized），效率中等
     * </p>
     *
     * @param request 请求参数
     * @return StringBuffer 类的演示结果
     */
    private StringBuilderComparisonResponse demonstrateStringBuffer(StringBuilderComparisonRequest request) {
        int loopCount = request.getLoopCount() != null ? request.getLoopCount() : DEFAULT_LOOP_COUNT;
        
        // 性能测试：字符串拼接
        long startTime = System.currentTimeMillis();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < loopCount; i++) {
            buffer.append(i);
        }
        String result = buffer.toString();
        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;

        String description = "StringBuffer 是可变字符序列，线程安全（方法有 synchronized 修饰）。" +
                "性能测试：循环 " + loopCount + " 次进行字符串拼接，耗时 " + costTime + "ms。" +
                "由于有同步锁开销，在单线程环境下效率低于 StringBuilder。";

        String keyFeatures = "1. 可变性：可以在原对象上修改，不创建新对象\n" +
                            "2. 线程安全：方法有 synchronized 修饰\n" +
                            "3. 效率中等：有同步锁开销\n" +
                            "4. 追加高效：append() 方法直接在末尾添加\n" +
                            "5. 支持链式调用：append() 返回 this";

        String codeExample = "// 1. StringBuffer 的基本使用\n" +
                            "StringBuffer buffer = new StringBuffer();\n" +
                            "buffer.append(\"hello\");\n" +
                            "buffer.append(\" \");\n" +
                            "buffer.append(\"world\");\n" +
                            "System.out.println(buffer);  // 输出：hello world\n\n" +
                            
                            "// 2. 线程安全示例\n" +
                            "// 多个线程同时 append 不会出现并发问题\n" +
                            "StringBuffer buffer = new StringBuffer();\n" +
                            "// thread1: buffer.append(\"A\");\n" +
                            "// thread2: buffer.append(\"B\");\n" +
                            "// 安全，但有效率开销\n\n" +
                            
                            "// 3. 链式调用\n" +
                            "buffer.append(\"A\").append(\"B\").append(\"C\");\n" +
                            "System.out.println(buffer);  // 输出：ABC";

        String comparisonTable = "| 特性 | StringBuffer |\n" +
                                "|------|---------------|\n" +
                                "| 可变性 | 可变 |\n" +
                                "| 线程安全 | ✓ 安全（synchronized） |\n" +
                                "| 同步锁 | ✓ 有 |\n" +
                                "| 性能 | 中等（有锁开销） |\n" +
                                "| 内存占用 | 低（不频繁创建对象） |\n" +
                                "| 适用场景 | 多线程、字符串频繁修改 |";

        String usageScenarios = "推荐使用场景：\n" +
                               "1. 多线程环境下的字符串拼接\n" +
                               "2. 字符串需要频繁修改\n" +
                               "3. 对线程安全有要求\n" +
                               "4. 循环中的字符串拼接\n" +
                               "\n不推荐场景：\n" +
                               "1. 单线程环境（StringBuilder 更高效）\n" +
                               "2. 字符串不需要修改";

        log.info("StringBuffer 演示完成，循环次数：{}, 耗时：{}ms", loopCount, costTime);

        return StringBuilderComparisonResponse.builder()
                .stringType(STRINGBUFFER)
                .typeName("StringBuffer")
                .description(description)
                .keyFeatures(keyFeatures)
                .performanceTime(costTime)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .success(true)
                .build();
    }

    /**
     * 演示 StringBuilder 类特性
     * <p>
     * StringBuilder 是可变字符序列，线程不安全，效率最高
     * </p>
     *
     * @param request 请求参数
     * @return StringBuilder 类的演示结果
     */
    private StringBuilderComparisonResponse demonstrateStringBuilder(StringBuilderComparisonRequest request) {
        int loopCount = request.getLoopCount() != null ? request.getLoopCount() : DEFAULT_LOOP_COUNT;
        
        // 性能测试：字符串拼接
        long startTime = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < loopCount; i++) {
            builder.append(i);
        }
        String result = builder.toString();
        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;

        String description = "StringBuilder 是可变字符序列，线程不安全（没有同步锁），效率最高。" +
                "性能测试：循环 " + loopCount + " 次进行字符串拼接，耗时 " + costTime + "ms。" +
                "在单线程环境下是最佳选择。";

        String keyFeatures = "1. 可变性：可以在原对象上修改，不创建新对象\n" +
                            "2. 线程不安全：没有 synchronized 修饰\n" +
                            "3. 效率最高：无同步锁开销\n" +
                            "4. 追加高效：append() 方法直接在末尾添加\n" +
                            "5. 支持链式调用：append() 返回 this\n" +
                            "6. JDK 5.0 引入";

        String codeExample = "// 1. StringBuilder 的基本使用\n" +
                            "StringBuilder builder = new StringBuilder();\n" +
                            "builder.append(\"hello\");\n" +
                            "builder.append(\" \");\n" +
                            "builder.append(\"world\");\n" +
                            "System.out.println(builder);  // 输出：hello world\n\n" +
                            
                            "// 2. 高效拼接（单线程）\n" +
                            "StringBuilder builder = new StringBuilder();\n" +
                            "for (int i = 0; i < 10000; i++) {\n" +
                            "    builder.append(i);  // 高效\n" +
                            "}\n" +
                            "String result = builder.toString();\n\n" +
                            
                            "// 3. 其他常用方法\n" +
                            "builder.insert(0, \"Start: \");  // 插入\n" +
                            "builder.delete(0, 6);           // 删除\n" +
                            "builder.reverse();              // 反转";

        String comparisonTable = "| 特性 | StringBuilder |\n" +
                                "|------|----------------|\n" +
                                "| 可变性 | 可变 |\n" +
                                "| 线程安全 | ✗ 不安全 |\n" +
                                "| 同步锁 | ✗ 无 |\n" +
                                "| 性能 | 高（无锁开销） |\n" +
                                "| 内存占用 | 低（不频繁创建对象） |\n" +
                                "| 适用场景 | 单线程、字符串频繁修改 |";

        String usageScenarios = "推荐使用场景：\n" +
                               "1. 单线程环境下的字符串拼接\n" +
                               "2. 字符串需要频繁修改\n" +
                               "3. 循环中的字符串拼接\n" +
                               "4. 对性能要求较高的场景\n" +
                               "\n不推荐场景：\n" +
                               "1. 多线程环境（需要额外同步）\n" +
                               "2. 字符串不需要修改（用 String）";

        log.info("StringBuilder 演示完成，循环次数：{}, 耗时：{}ms", loopCount, costTime);

        return StringBuilderComparisonResponse.builder()
                .stringType(STRINGBUILDER)
                .typeName("StringBuilder")
                .description(description)
                .keyFeatures(keyFeatures)
                .performanceTime(costTime)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .success(true)
                .build();
    }

    /**
     * 创建错误响应
     * <p>
     * 当请求的字符串类型无效或发生错误时使用
     * </p>
     *
     * @param stringType 字符串类型
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    private StringBuilderComparisonResponse createErrorResponse(String stringType, String errorMessage) {
        return StringBuilderComparisonResponse.builder()
                .stringType(stringType)
                .typeName("未知")
                .description("演示失败")
                .keyFeatures("")
                .performanceTime(0L)
                .codeExample("")
                .comparisonTable("")
                .usageScenarios("")
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
