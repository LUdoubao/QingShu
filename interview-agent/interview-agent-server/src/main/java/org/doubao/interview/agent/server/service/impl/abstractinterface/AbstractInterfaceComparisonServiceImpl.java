package org.doubao.interview.agent.server.service.impl.abstractinterface;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.abstractinterface.AbstractInterfaceComparisonRequest;
import org.doubao.interview.agent.api.dto.abstractinterface.AbstractInterfaceComparisonResponse;
import org.doubao.interview.agent.api.service.abstractinterface.AbstractInterfaceComparisonService;
import org.springframework.stereotype.Service;

/**
 * 接口和抽象类区别演示服务实现类
 * <p>
 * 提供接口和抽象类的详细对比
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@Service
public class AbstractInterfaceComparisonServiceImpl implements AbstractInterfaceComparisonService {

    /**
     * 抽象类类型常量
     */
    private static final String ABSTRACT_CLASS = "ABSTRACT_CLASS";

    /**
     * 接口类型常量
     */
    private static final String INTERFACE = "INTERFACE";

    /**
     * 演示接口或抽象类特性
     * <p>
     * 根据请求的类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的类型
     * @return 演示结果响应，包含特性说明、对比表格等
     */
    @Override
    public AbstractInterfaceComparisonResponse demonstrateFeature(AbstractInterfaceComparisonRequest request) {
        log.info("开始演示接口或抽象类特性，targetType={}, demoParameter={}", 
                request.getTargetType(), request.getDemoParameter());

        String targetType = request.getTargetType();
        
        // 根据类型分发到不同的演示方法
        if (ABSTRACT_CLASS.equalsIgnoreCase(targetType)) {
            return demonstrateAbstractClass(request);
        } else if (INTERFACE.equalsIgnoreCase(targetType)) {
            return demonstrateInterface(request);
        } else {
            // 兜底逻辑：未知的类型
            log.warn("未知的类型：{}, 进入兜底流程", targetType);
            return createErrorResponse(targetType, "未知的类型，可选值：ABSTRACT_CLASS、INTERFACE");
        }
    }

    /**
     * 演示抽象类特性
     * <p>
     * 抽象类：单继承、可含普通方法、构造器、各种访问修饰符的变量
     * </p>
     *
     * @param request 请求参数
     * @return 抽象类的演示结果
     */
    private AbstractInterfaceComparisonResponse demonstrateAbstractClass(AbstractInterfaceComparisonRequest request) {
        String description = "抽象类是带有抽象方法的类，用于代码复用和建立类层次结构。" +
                "特点：单继承、可含普通方法和抽象方法、有构造器、变量访问修饰符任意。" +
                "体现 is-a 关系（如 Dog is-a Animal）。";

        String keyFeatures = "1. 单继承：一个类只能继承一个抽象类\n" +
                            "2. 方法多样性：可含抽象方法和普通方法\n" +
                            "3. 构造器：可以有构造器供子类调用\n" +
                            "4. 变量灵活：支持各种访问修饰符的变量\n" +
                            "5. is-a 关系：表示本质上的继承关系\n" +
                            "6. 代码复用：提供公共实现给子类";

        String codeExample = "// 1. 定义抽象类\n" +
                            "public abstract class Animal {\n" +
                            "    protected String name;  // 受保护的变量\n" +
                            "    \n" +
                            "    // 构造器\n" +
                            "    public Animal(String name) {\n" +
                            "        this.name = name;\n" +
                            "    }\n" +
                            "    \n" +
                            "    // 抽象方法\n" +
                            "    public abstract void makeSound();\n" +
                            "    \n" +
                            "    // 普通方法\n" +
                            "    public void sleep() {\n" +
                            "        System.out.println(name + \"正在睡觉\");\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 2. 子类继承抽象类\n" +
                            "public class Dog extends Animal {\n" +
                            "    public Dog(String name) {\n" +
                            "        super(name);\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public void makeSound() {\n" +
                            "        System.out.println(name + \"汪汪汪\");\n" +
                            "    }\n" +
                            "}";

        String comparisonTable = "| 特性 | 抽象类 |\n" +
                                "|------|--------|\n" +
                                "| 继承方式 | 单继承 |\n" +
                                "| 方法类型 | 抽象 + 普通 |\n" +
                                "| 构造器 | ✓ 可以有 |\n" +
                                "| 变量修饰符 | 任意 |\n" +
                                "| JDK 版本要求 | 无特殊要求 |\n" +
                                "| 设计理念 | is-a（本质相同） |\n" +
                                "| 代码复用 | 强（提供公共实现） |";

        String usageScenarios = "推荐使用场景：\n" +
                               "1. 多个相关类有共同的行为和属性\n" +
                               "2. 需要提供默认实现给子类\n" +
                               "3. 需要控制子类访问（protected/private）\n" +
                               "4. 需要非公开的方法或字段\n" +
                               "5. 体现 is-a 关系的场景\n" +
                               "\n典型应用：\n" +
                               "- 动物体系（Animal -> Dog/Cat）\n" +
                               "- 图形体系（Shape -> Circle/Rectangle）\n" +
                               "- 模板方法模式";

        log.info("抽象类演示完成");

        return AbstractInterfaceComparisonResponse.builder()
                .targetType(ABSTRACT_CLASS)
                .typeName("抽象类")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .success(true)
                .build();
    }

    /**
     * 演示接口特性
     * <p>
     * 接口：多实现、JDK8+ 可含默认/静态方法、变量默认 public static final
     * </p>
     *
     * @param request 请求参数
     * @return 接口的演示结果
     */
    private AbstractInterfaceComparisonResponse demonstrateInterface(AbstractInterfaceComparisonRequest request) {
        String description = "接口是完全抽象的类型，用于定义行为规范。" +
                "特点：多实现、JDK8+ 可含默认/静态方法、变量默认 public static final。" +
                "体现 like-a 关系（如 Bird can-fly Flyable）。";

        String keyFeatures = "1. 多实现：一个类可以实现多个接口\n" +
                            "2. 方法演进：JDK8+ 支持默认方法和静态方法\n" +
                            "3. 无构造器：不能有构造器\n" +
                            "4. 变量限制：默认 public static final\n" +
                            "5. like-a 关系：表示能力或行为\n" +
                            "6. 完全抽象：强调\"能做什么\"";

        String codeExample = "// 1. 定义接口（JDK 8+）\n" +
                            "public interface Flyable {\n" +
                            "    // 常量（public static final 可省略）\n" +
                            "    int MAX_HEIGHT = 1000;\n" +
                            "    \n" +
                            "    // 抽象方法（public abstract 可省略）\n" +
                            "    void fly();\n" +
                            "    \n" +
                            "    // 默认方法（JDK 8+）\n" +
                            "    default void land() {\n" +
                            "        System.out.println(\"正在降落\");\n" +
                            "    }\n" +
                            "    \n" +
                            "    // 静态方法（JDK 8+）\n" +
                            "    static void info() {\n" +
                            "        System.out.println(\"这是飞行接口\");\n" +
                            "    }\n" +
                            "}\n\n" +
                            
                            "// 2. 实现接口\n" +
                            "public class Bird implements Flyable {\n" +
                            "    @Override\n" +
                            "    public void fly() {\n" +
                            "        System.out.println(\"鸟儿在飞翔\");\n" +
                            "    }\n" +
                            "    \n" +
                            "    // 可以重写默认方法\n" +
                            "    @Override\n" +
                            "    public void land() {\n" +
                            "        System.out.println(\"鸟儿降落在树枝上\");\n" +
                            "    }\n" +
                            "}";

        String comparisonTable = "| 特性 | 接口 |\n" +
                                "|------|------|\n" +
                                "| 继承方式 | 多实现 |\n" +
                                "| 方法类型 | 抽象 + 默认 + 静态（JDK8+） |\n" +
                                "| 构造器 | ✗ 不能有 |\n" +
                                "| 变量修饰符 | public static final |\n" +
                                "| JDK 版本要求 | JDK 8+ 支持默认方法 |\n" +
                                "| 设计理念 | like-a（能力/行为） |\n" +
                                "| 代码复用 | 弱（通过默认方法） |";

        String usageScenarios = "推荐使用场景：\n" +
                               "1. 定义行为规范或契约\n" +
                               "2. 需要多重继承效果\n" +
                               "3. 表示能力或行为（like-a）\n" +
                               "4. 解耦组件间的依赖\n" +
                               "5. 回调函数和事件监听\n" +
                               "\n典型应用：\n" +
                               "- Comparable/Comparator（比较器）\n" +
                               "- Runnable/Callable（线程）\n" +
                               "- List/Set/Map（集合框架）\n" +
                               "- 策略模式、观察者模式";

        log.info("接口演示完成");

        return AbstractInterfaceComparisonResponse.builder()
                .targetType(INTERFACE)
                .typeName("接口")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .usageScenarios(usageScenarios)
                .success(true)
                .build();
    }

    /**
     * 创建错误响应
     * <p>
     * 当请求的类型无效或发生错误时使用
     * </p>
     *
     * @param targetType 目标类型
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    private AbstractInterfaceComparisonResponse createErrorResponse(String targetType, String errorMessage) {
        return AbstractInterfaceComparisonResponse.builder()
                .targetType(targetType)
                .typeName("未知")
                .description("演示失败")
                .keyFeatures("")
                .codeExample("")
                .comparisonTable("")
                .usageScenarios("")
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
