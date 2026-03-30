package org.doubao.interview.agent.server.service.impl.oop;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.oop.OopFeatureRequest;
import org.doubao.interview.agent.api.dto.oop.OopFeatureResponse;
import org.doubao.interview.agent.api.service.oop.OopFeatureService;
import org.doubao.interview.agent.server.entity.oop.Animal;
import org.doubao.interview.agent.server.entity.oop.Cat;
import org.doubao.interview.agent.server.entity.oop.Dog;
import org.doubao.interview.agent.server.entity.oop.User;
import org.springframework.stereotype.Service;

/**
 * 面向对象三大特征演示服务实现类
 * <p>
 * 提供封装、继承、多态三大特征的详细演示和说明
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@Service
public class OopFeatureServiceImpl implements OopFeatureService {

    /**
     * 封装特性演示类型常量
     */
    private static final String ENCAPSULATION = "ENCAPSULATION";

    /**
     * 继承特性演示类型常量
     */
    private static final String INHERITANCE = "INHERITANCE";

    /**
     * 多态特性演示类型常量
     */
    private static final String POLYMORPHISM = "POLYMORPHISM";

    /**
     * 演示面向对象特征
     * <p>
     * 根据请求的特征类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的特征类型
     * @return 演示结果响应，包含特征说明、示例代码等
     */
    @Override
    public OopFeatureResponse demonstrateFeature(OopFeatureRequest request) {
        log.info("开始演示面向对象特征，featureType={}, demoParameter={}", 
                request.getFeatureType(), request.getDemoParameter());

        String featureType = request.getFeatureType();
        
        // 根据特征类型分发到不同的演示方法
        if (ENCAPSULATION.equalsIgnoreCase(featureType)) {
            return demonstrateEncapsulation(request);
        } else if (INHERITANCE.equalsIgnoreCase(featureType)) {
            return demonstrateInheritance(request);
        } else if (POLYMORPHISM.equalsIgnoreCase(featureType)) {
            return demonstratePolymorphism(request);
        } else {
            // 兜底逻辑：未知的特征类型
            log.warn("未知的特征类型：{}, 进入兜底流程", featureType);
            return createErrorResponse(featureType, "未知的特征类型，可选值：ENCAPSULATION、INHERITANCE、POLYMORPHISM");
        }
    }

    /**
     * 演示封装特性
     * <p>
     * 封装的核心：隐藏内部实现细节，保护数据安全，提供公共访问接口
     * </p>
     *
     * @param request 请求参数
     * @return 封装特性的演示结果
     */
    private OopFeatureResponse demonstrateEncapsulation(OopFeatureRequest request) {
        // 创建 User 对象，演示封装特性
        User user = new User(1L, "张三", "zhangsan@example.com");
        
        // 演示通过 getter/setter 访问和修改数据
        String originalName = user.getName();
        user.setName("李四");
        String newName = user.getName();

        // 演示封装的数据验证功能（邮箱格式校验）
        String validationDemo = "";
        try {
            user.setEmail("invalid-email");
        } catch (IllegalArgumentException e) {
            validationDemo = "数据验证成功：" + e.getMessage();
        }

        String description = "封装特性演示：User 类将属性私有化，通过公共的 getter/setter 方法访问和修改数据。" +
                "在 setEmail() 方法中可以进行数据验证，保证数据的有效性。" +
                "初始姓名：" + originalName + "，修改后姓名：" + newName + "。" + validationDemo;

        String keyPoints = "1. 属性私有化（private），隐藏内部实现细节\n" +
                          "2. 提供公共的 getter/setter 方法，控制数据访问\n" +
                          "3. 在 setter 方法中可以进行数据验证，保证数据安全\n" +
                          "4. 便于修改和维护，不影响外部调用代码";

        String codeExample = "// 1. 创建 User 对象\n" +
                            "User user = new User(1L, \"张三\", \"zhangsan@example.com\");\n\n" +
                            "// 2. 通过 getter 方法获取数据\n" +
                            "String name = user.getName();\n\n" +
                            "// 3. 通过 setter 方法修改数据\n" +
                            "user.setName(\"李四\");\n\n" +
                            "// 4. setter 方法中的数据验证\n" +
                            "try {\n" +
                            "    user.setEmail(\"invalid-email\"); // 会抛出异常\n" +
                            "} catch (IllegalArgumentException e) {\n" +
                            "    log.error(\"邮箱格式不正确：{}\", e.getMessage());\n" +
                            "}";

        log.info("封装特性演示完成，用户信息：id={}, name={}, email={}", 
                user.getId(), user.getName(), user.getEmail());

        return OopFeatureResponse.builder()
                .featureType(ENCAPSULATION)
                .featureName("封装")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .success(true)
                .build();
    }

    /**
     * 演示继承特性
     * <p>
     * 继承的核心：子类复用父类的属性和方法，提高代码复用性
     * </p>
     *
     * @param request 请求参数
     * @return 继承特性的演示结果
     */
    private OopFeatureResponse demonstrateInheritance(OopFeatureRequest request) {
        // 创建 Dog 对象，演示继承特性
        Dog dog = new Dog("旺财", 3, "金毛");
        
        // 创建 Cat 对象，演示继承特性
        Cat cat = new Cat("咪咪", 2, "白色");

        // 演示子类继承父类的属性和方法
        String dogInfo = dog.getInfo();
        String dogSound = dog.makeSound();
        String dogMove = dog.move();
        
        String catInfo = cat.getInfo();
        String catSound = cat.makeSound();
        String catMove = cat.move();

        // 演示子类特有的方法
        String dogSpecial = dog.guardHome();
        String catSpecial = cat.catchMouse();

        String description = "继承特性演示：Dog 类和 Cat 类继承自动物类 Animal，复用了父类的属性（name、age）和方法（getInfo、move）。" +
                "狗的信息：" + dogInfo + "，叫声：" + dogSound + "，移动：" + dogMove + "，特有行为：" + dogSpecial + "。" +
                "猫的信息：" + catInfo + "，叫声：" + catSound + "，移动：" + catMove + "，特有行为：" + catSpecial + "。";

        String keyPoints = "1. 使用 extends 关键字实现继承\n" +
                          "2. 子类自动拥有父类的非私有属性和方法\n" +
                          "3. 可以通过 super 关键字调用父类构造器或方法\n" +
                          "4. 可以添加自己特有的属性和方法\n" +
                          "5. Java 支持单继承，一个类只能有一个直接父类";

        String codeExample = "// 1. 定义抽象父类 Animal\n" +
                            "public abstract class Animal {\n" +
                            "    protected String name;\n" +
                            "    protected Integer age;\n" +
                            "    public abstract String makeSound();\n" +
                            "}\n\n" +
                            "// 2. 创建子类 Dog 继承 Animal\n" +
                            "public class Dog extends Animal {\n" +
                            "    private String breed; // 子类特有属性\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public String makeSound() {\n" +
                            "        return name + \"汪汪汪！\";\n" +
                            "    }\n" +
                            "}";

        log.info("继承特性演示完成，狗：{}, 猫：{}", dogInfo, catInfo());

        return OopFeatureResponse.builder()
                .featureType(INHERITANCE)
                .featureName("继承")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .success(true)
                .build();
    }

    /**
     * 演示多态特性
     * <p>
     * 多态的核心：同一行为的不同子类实现不同表现，基于方法重写或接口实现
     * </p>
     *
     * @param request 请求参数
     * @return 多态特性的演示结果
     */
    private OopFeatureResponse demonstratePolymorphism(OopFeatureRequest request) {
        // 演示多态：父类引用指向子类对象
        Animal animal1 = new Dog("旺财", 3, "金毛");
        Animal animal2 = new Cat("咪咪", 2, "白色");

        // 演示多态性：同样的方法调用，执行的是子类的实现
        String animal1Sound = animal1.makeSound();
        String animal1Move = animal1.move();
        
        String animal2Sound = animal2.makeSound();
        String animal2Move = animal2.move();

        // 演示 instanceof 判断实际类型
        String typeCheck1 = animal1 instanceof Dog ? "animal1 是 Dog 类型" : "animal1 不是 Dog 类型";
        String typeCheck2 = animal2 instanceof Cat ? "animal2 是 Cat 类型" : "animal2 不是 Cat 类型";

        // 演示向下转型调用子类特有方法
        String dogSpecialMethod = "";
        if (animal1 instanceof Dog) {
            Dog dog = (Dog) animal1;
            dogSpecialMethod = "向下转型后调用特有方法：" + dog.guardHome();
        }

        String description = "多态特性演示：父类 Animal 引用指向子类 Dog 和 Cat 对象。" +
                "调用相同的方法 makeSound()，但执行的是各自子类的实现。" +
                "animal1(Dog): " + animal1Sound + ", " + animal1Move + "。" +
                "animal2(Cat): " + animal2Sound + ", " + animal2Move + "。" +
                typeCheck1 + "，" + typeCheck2 + "。" + dogSpecialMethod;

        String keyPoints = "1. 父类引用可以指向子类对象（Animal animal = new Dog()）\n" +
                          "2. 方法调用时执行的是子类的实际实现（动态绑定）\n" +
                          "3. 多态存在的三要素：继承、重写、父类引用指向子类对象\n" +
                          "4. 可以通过 instanceof 判断对象的实际类型\n" +
                          "5. 向下转型可以调用子类特有方法";

        String codeExample = "// 1. 父类引用指向子类对象\n" +
                            "Animal animal1 = new Dog(\"旺财\", 3, \"金毛\");\n" +
                            "Animal animal2 = new Cat(\"咪咪\", 2, \"白色\");\n\n" +
                            "// 2. 调用同一个方法，执行不同的实现\n" +
                            "System.out.println(animal1.makeSound()); // 输出：旺财汪汪汪！\n" +
                            "System.out.println(animal2.makeSound()); // 输出：咪咪喵喵喵~\n\n" +
                            "// 3. 使用 instanceof 判断类型并向下转型\n" +
                            "if (animal1 instanceof Dog) {\n" +
                            "    Dog dog = (Dog) animal1;\n" +
                            "    dog.guardHome(); // 调用子类特有方法\n" +
                            "}";

        log.info("多态特性演示完成，animal1 叫声：{}, animal2 叫声：{}", animal1Sound, animal2Sound);

        return OopFeatureResponse.builder()
                .featureType(POLYMORPHISM)
                .featureName("多态")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .success(true)
                .build();
    }

    /**
     * 创建错误响应
     * <p>
     * 当请求的特征类型无效或发生错误时使用
     * </p>
     *
     * @param featureType 特征类型
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    private OopFeatureResponse createErrorResponse(String featureType, String errorMessage) {
        return OopFeatureResponse.builder()
                .featureType(featureType)
                .featureName("未知")
                .description("演示失败")
                .keyPoints("")
                .codeExample("")
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 辅助方法：获取猫的日志信息
     * <p>
     * 为了避免 lambda 表达式中变量必须是 effectively final 的限制
     * </p>
     *
     * @return 猫的日志信息字符串
     */
    private String catInfo() {
        Cat cat = new Cat("咪咪", 2, "白色");
        return cat.getInfo();
    }
}
