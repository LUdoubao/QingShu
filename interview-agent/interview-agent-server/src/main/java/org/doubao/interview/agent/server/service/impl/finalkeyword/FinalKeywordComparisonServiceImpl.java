package org.doubao.interview.agent.server.service.impl.finalkeyword;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.finalkeyword.FinalKeywordComparisonRequest;
import org.doubao.interview.agent.api.dto.finalkeyword.FinalKeywordComparisonResponse;
import org.doubao.interview.agent.api.service.finalkeyword.FinalKeywordComparisonService;
import org.springframework.stereotype.Service;

/**
 * final、finally、finalize 区别演示服务实现类
 * <p>
 * 提供三个关键字的详细对比
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@Service
public class FinalKeywordComparisonServiceImpl implements FinalKeywordComparisonService {

    /**
     * final 关键字类型常量
     */
    private static final String FINAL = "FINAL";

    /**
     * finally 关键字类型常量
     */
    private static final String FINALLY = "FINALLY";

    /**
     * finalize 关键字类型常量
     */
    private static final String FINALIZE = "FINALIZE";

    /**
     * 演示 final、finally、finalize 特性
     * <p>
     * 根据请求的关键字类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的关键字类型
     * @return 演示结果响应，包含特性说明、对比表格等
     */
    @Override
    public FinalKeywordComparisonResponse demonstrateFeature(FinalKeywordComparisonRequest request) {
        log.info("开始演示 final/finally/finalize 特性，keywordType={}, demoParameter={}", 
                request.getKeywordType(), request.getDemoParameter());

        String keywordType = request.getKeywordType();
        
        // 根据类型分发到不同的演示方法
        if (FINAL.equalsIgnoreCase(keywordType)) {
            return demonstrateFinal(request);
        } else if (FINALLY.equalsIgnoreCase(keywordType)) {
            return demonstrateFinally(request);
        } else if (FINALIZE.equalsIgnoreCase(keywordType)) {
            return demonstrateFinalize(request);
        } else {
            // 兜底逻辑：未知的类型
            log.warn("未知的关键字类型：{}, 进入兜底流程", keywordType);
            return createErrorResponse(keywordType, "未知的类型，可选值：FINAL、FINALLY、FINALIZE");
        }
    }

    /**
     * 演示 final 关键字
     * <p>
     * final: 修饰类（不可继承）、方法（不可重写）、变量（常量）
     * </p>
     *
     * @param request 请求参数
     * @return final 的演示结果
     */
    private FinalKeywordComparisonResponse demonstrateFinal(FinalKeywordComparisonRequest request) {
        String description = "final 是修饰符，用于表示\"最终的、不可变的\"。" +
                "特点：修饰类（不可继承）、方法（不可重写）、变量（常量）。" +
                "核心：一旦赋值就不能修改（引用类型指引用不可变）。";

        String keyFeatures = "1. 修饰类：该类不能被继承（如 String 类）\n" +
                            "2. 修饰方法：该方法不能被子类重写\n" +
                            "3. 修饰变量：\n" +
                            "   - 基本类型：值不能修改\n" +
                            "   - 引用类型：引用地址不能修改\n" +
                            "4. 修饰参数：方法内部不能修改参数值\n" +
                            "5. 延迟初始化：可以在构造器或代码块中赋值";

        String codeExample = "// 1. final 修饰类（不可继承）\n" +
                            "public final class MathUtil {\n" +
                            "    // 工具类，不允许被继承\n" +
                            "    public static int add(int a, int b) {\n" +
                            "        return a + b;\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 2. final 修饰方法（不可重写）\n" +
                            "public class Parent {\n" +
                            "    public final void show() {\n" +
                            "        System.out.println(\"父类方法\");\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 3. final 修饰变量（常量）\n" +
                            "public class Constants {\n" +
                            "    // 编译时常量\n" +
                            "    public static final double PI = 3.14159;\n" +
                            "    \n" +
                            "    // 实例常量\n" +
                            "    private final String id;\n" +
                            "    \n" +
                            "    // 构造器中赋值\n" +
                            "    public Constants(String id) {\n" +
                            "        this.id = id;  // 只能赋值一次\n" +
                            "    }\n" +
                            "    \n" +
                            "    // final 修饰数组\n" +
                            "    private final int[] numbers = {1, 2, 3};\n" +
                            "    \n" +
                            "    public void test() {\n" +
                            "        // numbers = new int[]{4,5}; // ❌ 错误：不能修改引用\n" +
                            "        numbers[0] = 100;  // ✓ 正确：可以修改数组元素\n" +
                            "    }\n" +
                            "}";

        String comparisonTable = "| 修饰对象 | 作用 |\n" +
                                "|----------|------|\n" +
                                "| 类 | 不能被继承 |\n" +
                                "| 方法 | 不能被重写 |\n" +
                                "| 基本类型变量 | 值不能修改 |\n" +
                                "| 引用类型变量 | 引用地址不能修改 |\n" +
                                "| 数组 | 数组引用不能修改，元素可改 |\n" +
                                "| 参数 | 方法内部不能修改参数值 |";

        String usageScenarios = "推荐使用场景：\n" +
                               "1. 定义常量（static final）\n" +
                               "2. 工具类（不允许继承）\n" +
                               "3. 不希望被子类重写的方法\n" +
                               "4. 保证引用安全的场景\n" +
                               "5. 匿名内部类的参数（JDK 8+）\n" +
                               "\n典型应用：\n" +
                               "- String 类（final 类）\n" +
                               "- 包装类（Integer、Double 等）\n" +
                               "- 配置常量（public static final）\n" +
                               "- Collections.unmodifiableXXX() 返回的对象";

        String precautions = "注意事项：\n" +
                           "1. final 不保证线程安全（只是引用不可变）\n" +
                           "2. final 对象的内容可能可变（如 final List）\n" +
                           "3. final 不影响性能（早期版本有优化，现在不明显）\n" +
                           "4. 构造函数中可以给 final 变量赋值\n" +
                           "5. 匿名内部类访问局部变量需要 final（JDK 8+ 隐式 effective final）";

        log.info("final 关键字演示完成");

        return FinalKeywordComparisonResponse.builder()
                .keywordType(FINAL)
                .keywordName("final")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .precautions(precautions)
                .success(true)
                .build();
    }

    /**
     * 演示 finally 关键字
     * <p>
     * finally: 异常处理关键字，配合 try 使用，代码必执行
     * </p>
     *
     * @param request 请求参数
     * @return finally 的演示结果
     */
    private FinalKeywordComparisonResponse demonstrateFinally(FinalKeywordComparisonRequest request) {
        String description = "finally 是异常处理机制的一部分，与 try-catch 配合使用。" +
                "特点：finally 块中的代码几乎总是会执行（除了 System.exit() 等极端情况）。" +
                "核心：用于资源清理，如关闭文件、数据库连接等。";

        String keyFeatures = "1. 必须与 try 配合使用（try-catch-finally 或 try-finally）\n" +
                            "2. finally 块中的代码几乎总是执行\n" +
                            "3. 无论是否发生异常都会执行\n" +
                            "4. 即使 try 中有 return 也会先执行 finally\n" +
                            "5. 用于资源清理和释放\n" +
                            "6. JDK7+ 推荐使用 try-with-resources 替代";

        String codeExample = "// 1. 基本的 try-catch-finally\n" +
                            "public void readFile(String path) {\n" +
                            "    BufferedReader reader = null;\n" +
                            "    try {\n" +
                            "        reader = new BufferedReader(new FileReader(path));\n" +
                            "        String line = reader.readLine();\n" +
                            "        System.out.println(line);\n" +
                            "    } catch (IOException e) {\n" +
                            "        log.error(\"读取文件失败\", e);\n" +
                            "    } finally {\n" +
                            "        // 无论如何都会执行，用于关闭资源\n" +
                            "        if (reader != null) {\n" +
                            "            try {\n" +
                            "                reader.close();\n" +
                            "            } catch (IOException e) {\n" +
                            "                log.error(\"关闭流失败\", e);\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 2. finally 中有 return 的情况（不推荐）\n" +
                            "public int testReturn() {\n" +
                            "    try {\n" +
                            "        int result = 10 / 0;\n" +
                            "        return 1;  // 不会执行到这里\n" +
                            "    } catch (ArithmeticException e) {\n" +
                            "        return 2;  // 准备返回 2\n" +
                            "    } finally {\n" +
                            "        return 3;  // ❌ 覆盖 catch 中的 return，返回 3\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 3. JDK7+ try-with-resources（推荐）\n" +
                            "public void readFileModern(String path) {\n" +
                            "    // 自动关闭资源，无需 finally\n" +
                            "    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {\n" +
                            "        String line = reader.readLine();\n" +
                            "        System.out.println(line);\n" +
                            "    } catch (IOException e) {\n" +
                            "        log.error(\"读取文件失败\", e);\n" +
                            "    }\n" +
                            "}";

        String comparisonTable = "| 特性 | 说明 |\n" +
                                "|------|------|\n" +
                                "| 执行时机 | try-catch 之后 |\n" +
                                "| 是否必执行 | 是（除极端情况） |\n" +
                                "| 与 try 关系 | 必须配合使用 |\n" +
                                "| return 影响 | finally 中的 return 会覆盖其他的 |\n" +
                                "| 主要用途 | 资源清理 |\n" +
                                "| 替代方案 | try-with-resources（JDK7+） |";

        String usageScenarios = "推荐使用场景：\n" +
                               "1. 关闭 IO 流（文件、网络）\n" +
                               "2. 关闭数据库连接\n" +
                               "3. 释放锁（ReentrantLock.unlock()）\n" +
                               "4. 清理临时资源\n" +
                               "5. 恢复现场（如切换 ClassLoader）\n" +
                               "\n现代替代方案：\n" +
                               "- try-with-resources（JDK7+）：自动关闭 Closeable 资源\n" +
                               "- try-with-resources 支持多个资源\n" +
                               "- 更简洁，不易出错";

        String precautions = "注意事项：\n" +
                           "1. 不要在 finally 中使用 return（会覆盖 try/catch 的结果）\n" +
                           "2. finally 中抛异常会覆盖 try 中的异常\n" +
                           "3. System.exit(0) 会阻止 finally 执行\n" +
                           "4. 线程死亡（Thread.stop()）不会执行 finally\n" +
                           "5. CPU 强制关闭进程不会执行 finally\n" +
                           "6. JDK7+ 优先使用 try-with-resources\n" +
                           "7. finally 中避免复杂逻辑（保持简单清理）";

        log.info("finally 关键字演示完成");

        return FinalKeywordComparisonResponse.builder()
                .keywordType(FINALLY)
                .keywordName("finally")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .precautions(precautions)
                .success(true)
                .build();
    }

    /**
     * 演示 finalize 关键字
     * <p>
     * finalize: Object 方法，GC 回收前执行，已废弃
     * </p>
     *
     * @param request 请求参数
     * @return finalize 的演示结果
     */
    private FinalKeywordComparisonResponse demonstrateFinalize(FinalKeywordComparisonRequest request) {
        String description = "finalize() 是 Object 类的方法，在对象被垃圾回收前由 JVM 调用。" +
                "特点：用于清理资源，但行为不确定，性能差，已废弃。" +
                "核心：JDK9+ 标记为 Deprecated，不推荐使用。";

        String keyFeatures = "1. Object 类的方法，所有类都继承\n" +
                            "2. GC 回收对象前会自动调用\n" +
                            "3. 调用时机不确定（依赖 GC）\n" +
                            "4. 可能被调用多次或不执行\n" +
                            "5. 性能开销大（增加对象生命周期）\n" +
                            "6. JDK9+ 已废弃（@Deprecated）\n" +
                            "7. 推荐使用 Cleaner 或 PhantomReference 替代";

        String codeExample = "// 1. finalize() 的传统用法（已过时）\n" +
                            "public class LegacyResource {\n" +
                            "    private String resourceName;\n" +
                            "    \n" +
                            "    public LegacyResource(String name) {\n" +
                            "        this.resourceName = name;\n" +
                            "        System.out.println(\"创建资源：\" + name);\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    @Deprecated  // JDK9+ 标记为废弃\n" +
                            "    protected void finalize() throws Throwable {\n" +
                            "        try {\n" +
                            "            // 清理资源（不推荐）\n" +
                            "            System.out.println(\"清理资源：\" + resourceName);\n" +
                            "            resourceName = null;\n" +
                            "        } finally {\n" +
                            "            super.finalize();  // 必须调用父类方法\n" +
                            "        }\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 2. 现代替代方案：AutoCloseable（推荐）\n" +
                            "public class ModernResource implements AutoCloseable {\n" +
                            "    private String resourceName;\n" +
                            "    \n" +
                            "    public ModernResource(String name) {\n" +
                            "        this.resourceName = name;\n" +
                            "        System.out.println(\"创建资源：\" + name);\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public void close() {\n" +
                            "        // 确定性清理（推荐）\n" +
                            "        System.out.println(\"清理资源：\" + resourceName);\n" +
                            "        resourceName = null;\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 3. 使用 try-with-resources\n" +
                            "public void useResource() {\n" +
                            "    try (ModernResource resource = new ModernResource(\"test\")) {\n" +
                            "        // 使用资源\n" +
                            "    }  // 自动调用 close()\n" +
                            "}";

        String comparisonTable = "| 特性 | finalize() |\n" +
                                "|------|------------|\n" +
                                "| 所属类 | Object |\n" +
                                "| 调用时机 | GC 回收前（不确定） |\n" +
                                "| 是否可靠 | 否（可能不执行） |\n" +
                                "| 性能影响 | 差（延长对象生命周期） |\n" +
                                "| JDK 状态 | JDK9+ @Deprecated |\n" +
                                "| 替代方案 | Cleaner、PhantomReference |";

        String usageScenarios = "❌ 不推荐使用场景：\n" +
                               "1. 任何需要确定性清理的场景\n" +
                               "2. 关闭文件、网络连接等资源\n" +
                               "3. 释放内存以外的资源\n" +
                               "\n✅ 现代替代方案：\n" +
                               "1. AutoCloseable + try-with-resources（JDK7+）\n" +
                               "2. java.lang.ref.Cleaner（JDK9+）\n" +
                               "3. PhantomReference（虚引用）\n" +
                               "4. 显式的 close() 方法\n" +
                               "\n典型应用：\n" +
                               "- FileInputStream 等实现了 AutoCloseable\n" +
                               "- DatabaseConnection 实现 close() 方法\n" +
                               "- Releasable 资源的显式管理";

        String precautions = "重要警告：\n" +
                           "1. ❌ 不要依赖 finalize() 做资源清理\n" +
                           "2. ❌ finalize() 可能永远不被调用\n" +
                           "3. ❌ finalize() 可能导致对象\"复活\"（不推荐）\n" +
                           "4. ❌ finalize() 性能开销大\n" +
                           "5. ❌ finalize() 中抛异常会被忽略\n" +
                           "6. ✅ 使用 AutoCloseable 接口\n" +
                           "7. ✅ 使用 try-with-resources\n" +
                           "8. ✅ 使用 Cleaner（JDK9+）";

        log.info("finalize 关键字演示完成");

        return FinalKeywordComparisonResponse.builder()
                .keywordType(FINALIZE)
                .keywordName("finalize")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .precautions(precautions)
                .success(true)
                .build();
    }

    /**
     * 创建错误响应
     * <p>
     * 当请求的类型无效或发生错误时使用
     * </p>
     *
     * @param keywordType 关键字类型
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    private FinalKeywordComparisonResponse createErrorResponse(String keywordType, String errorMessage) {
        return FinalKeywordComparisonResponse.builder()
                .keywordType(keywordType)
                .keywordName("未知")
                .description("演示失败")
                .keyFeatures("")
                .codeExample("")
                .comparisonTable("")
                .usageScenarios("")
                .precautions("")
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
