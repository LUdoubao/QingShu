package org.doubao.interview.agent.server.service.impl.method;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.method.MethodFeatureRequest;
import org.doubao.interview.agent.api.dto.method.MethodFeatureResponse;
import org.doubao.interview.agent.api.service.method.MethodFeatureService;
import org.doubao.interview.agent.server.entity.method.Animal;
import org.doubao.interview.agent.server.entity.method.Calculator;
import org.doubao.interview.agent.server.entity.method.Cat;
import org.doubao.interview.agent.server.entity.method.Dog;
import org.springframework.stereotype.Service;

/**
 * 方法重载和重写演示服务实现类
 * <p>
 * 提供方法重载（Overload）和方法重写（Override）的详细演示和说明
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@Service
public class MethodFeatureServiceImpl implements MethodFeatureService {

    /**
     * 方法重载演示类型常量
     */
    private static final String OVERLOAD = "OVERLOAD";

    /**
     * 方法重写演示类型常量
     */
    private static final String OVERRIDE = "OVERRIDE";

    /**
     * 演示方法特性
     * <p>
     * 根据请求的特性类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的特性类型
     * @return 演示结果响应，包含特性说明、示例代码、对比表格等
     */
    @Override
    public MethodFeatureResponse demonstrateFeature(MethodFeatureRequest request) {
        log.info("开始演示方法特性，featureType={}, demoParameter={}", 
                request.getFeatureType(), request.getDemoParameter());

        String featureType = request.getFeatureType();
        
        // 根据特性类型分发到不同的演示方法
        if (OVERLOAD.equalsIgnoreCase(featureType)) {
            return demonstrateOverload(request);
        } else if (OVERRIDE.equalsIgnoreCase(featureType)) {
            return demonstrateOverride(request);
        } else {
            // 兜底逻辑：未知的特性类型
            log.warn("未知的特性类型：{}, 进入兜底流程", featureType);
            return createErrorResponse(featureType, "未知的特性类型，可选值：OVERLOAD、OVERRIDE");
        }
    }

    /**
     * 演示方法重载
     * <p>
     * 方法重载的核心：同一个类中，方法名相同，参数列表不同（参数个数、类型、顺序）
     * 与返回值无关，不能仅通过返回值区分重载方法
     * </p>
     *
     * @param request 请求参数
     * @return 方法重载的演示结果
     */
    private MethodFeatureResponse demonstrateOverload(MethodFeatureRequest request) {
        // 创建 Calculator 对象，演示方法重载
        Calculator calculator = new Calculator();

        // 演示不同的 add 方法调用
        int result1 = calculator.add(10, 20);
        int result2 = calculator.add(10, 20, 30);
        double result3 = calculator.add(10.5, 20.8);
        String result4 = calculator.add("Hello, ", "World!");
        int result5 = calculator.add(1, 2, 3, 4, 5);

        String description = "方法重载演示：Calculator 类中有 5 个同名的 add 方法，但参数列表不同。" +
                "两个整数相加：" + result1 + "，三个整数相加：" + result2 + "，" +
                "两个浮点数相加：" + result3 + "，字符串拼接：\"" + result4 + "\"，" +
                "可变参数相加：" + result5 + "。编译器根据参数自动选择合适的方法。";

        String keyPoints = "1. 发生在同一个类中\n" +
                          "2. 方法名必须相同\n" +
                          "3. 参数列表必须不同（个数、类型、顺序）\n" +
                          "4. 与返回值无关，不能仅通过返回值区分\n" +
                          "5. 构造方法也可以重载\n" +
                          "6. 编译时绑定（静态多态）";

        String codeExample = "// 1. 定义重载方法\n" +
                            "public class Calculator {\n" +
                            "    // 重载方法 1：两个整数相加\n" +
                            "    public int add(int a, int b) {\n" +
                            "        return a + b;\n" +
                            "    }\n" +
                            "    \n" +
                            "    // 重载方法 2：三个整数相加\n" +
                            "    public int add(int a, int b, int c) {\n" +
                            "        return a + b + c;\n" +
                            "    }\n" +
                            "    \n" +
                            "    // 重载方法 3：两个浮点数相加\n" +
                            "    public double add(double a, double b) {\n" +
                            "        return a + b;\n" +
                            "    }\n" +
                            "    \n" +
                            "    // 重载方法 4：字符串拼接\n" +
                            "    public String add(String a, String b) {\n" +
                            "        return a + b;\n" +
                            "    }\n" +
                            "}";

        String comparisonTable = "| 特性 | 方法重载 (Overload) |\n" +
                                "|------|---------------------|\n" +
                                "| 发生范围 | 同一个类中 |\n" +
                                "| 方法名 | 必须相同 |\n" +
                                "| 参数列表 | 必须不同（个数、类型、顺序） |\n" +
                                "| 返回类型 | 可以不同，但不能仅通过返回值区分 |\n" +
                                "| 访问修饰符 | 可以不同 |\n" +
                                "| 绑定方式 | 编译时绑定（静态多态） |\n" +
                                "| 注解 | 不需要 @Override |\n" +
                                "| 典型应用 | 构造函数重载、工具类方法 |";

        log.info("方法重载演示完成，执行了 5 个不同的 add 方法");

        return MethodFeatureResponse.builder()
                .featureType(OVERLOAD)
                .featureName("方法重载")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .success(true)
                .build();
    }

    /**
     * 演示方法重写
     * <p>
     * 方法重写的核心：父子类中，方法签名完全一致（方法名、参数列表、返回类型），
     * 子类提供父类方法的特定实现
     * </p>
     *
     * @param request 请求参数
     * @return 方法重写的演示结果
     */
    private MethodFeatureResponse demonstrateOverride(MethodFeatureRequest request) {
        // 创建子类和父类对象，演示方法重写
        Animal animal = new Animal("通用动物") {
            @Override
            public String makeSound() {
                return "";
            }
        };
        Dog dog = new Dog("旺财", "金毛");
        Cat cat = new Cat("咪咪", "白色");

        // 演示重写的方法调用
        String animalSound = animal.makeSound();
        String dogSound = dog.makeSound();
        String catSound = cat.makeSound();
        
        String animalMove = animal.move();
        String dogMove = dog.move();
        String catMove = cat.move();

        // 演示多态性
        Animal polyDog = new Dog("小黑", "哈士奇");
        String polySound = polyDog.makeSound();

        String description = "方法重写演示：Dog 和 Cat 类重写了 Animal 类的 makeSound() 和 move() 方法。" +
                "Animal: " + animalSound + "，" + animalMove + "。" +
                "Dog: " + dogSound + "，" + dogMove + "。" +
                "Cat: " + catSound + "，" + catMove + "。" +
                "多态演示（父类引用指向子类对象）：" + polySound + "。";

        String keyPoints = "1. 发生在父子类之间\n" +
                          "2. 方法名必须相同\n" +
                          "3. 参数列表必须相同\n" +
                          "4. 返回类型必须相同或是其子类型\n" +
                          "5. 访问权限不能比父类更严格\n" +
                          "6. 不能抛出比父类更多的异常\n" +
                          "7. 使用 @Override 注解确保正确重写\n" +
                          "8. 运行时绑定（动态多态）";

        String codeExample = "// 1. 定义父类\n" +
                            "public abstract class Animal {\n" +
                            "    public String makeSound() {\n" +
                            "        return \"动物发出声音\";\n" +
                            "    }\n" +
                            "}\n\n" +
                            "// 2. 子类重写父类方法\n" +
                            "public class Dog extends Animal {\n" +
                            "    @Override\n" +
                            "    public String makeSound() {\n" +
                            "        return name + \"汪汪汪！\";\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public String move() {\n" +
                            "        return name + \"用四条腿奔跑\";\n" +
                            "    }\n" +
                            "}";

        String comparisonTable = "| 特性 | 方法重写 (Override) |\n" +
                                "|------|---------------------|\n" +
                                "| 发生范围 | 父子类之间 |\n" +
                                "| 方法名 | 必须相同 |\n" +
                                "| 参数列表 | 必须相同 |\n" +
                                "| 返回类型 | 必须相同或是其子类型 |\n" +
                                "| 访问修饰符 | 不能比父类更严格 |\n" +
                                "| 异常处理 | 不能抛出比父类更多的检查异常 |\n" +
                                "| 绑定方式 | 运行时绑定（动态多态） |\n" +
                                "| 注解 | 建议使用 @Override |\n" +
                                "| 典型应用 | 实现多态、定制子类行为 |";

        log.info("方法重写演示完成，Dog 叫声：{}, Cat 叫声：{}", dogSound, catSound);

        return MethodFeatureResponse.builder()
                .featureType(OVERRIDE)
                .featureName("方法重写")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .success(true)
                .build();
    }

    /**
     * 创建错误响应
     * <p>
     * 当请求的特性类型无效或发生错误时使用
     * </p>
     *
     * @param featureType 特性类型
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    private MethodFeatureResponse createErrorResponse(String featureType, String errorMessage) {
        return MethodFeatureResponse.builder()
                .featureType(featureType)
                .featureName("未知")
                .description("演示失败")
                .keyPoints("")
                .codeExample("")
                .comparisonTable("")
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
