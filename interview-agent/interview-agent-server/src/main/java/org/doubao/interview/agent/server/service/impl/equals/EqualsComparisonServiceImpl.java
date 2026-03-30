package org.doubao.interview.agent.server.service.impl.equals;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.equals.EqualsComparisonRequest;
import org.doubao.interview.agent.api.dto.equals.EqualsComparisonResponse;
import org.doubao.interview.agent.api.service.equals.EqualsComparisonService;
import org.doubao.interview.agent.server.entity.equals.User;
import org.springframework.stereotype.Service;

/**
 * ==和 equals() 区别演示服务实现类
 * <p>
 * 提供==和 equals() 方法比较的详细演示和说明
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@Service
public class EqualsComparisonServiceImpl implements EqualsComparisonService {

    /**
     * 基本类型比较常量
     */
    private static final String BASIC = "BASIC";

    /**
     * 字符串比较常量
     */
    private static final String STRING = "STRING";

    /**
     * 包装类比较常量
     */
    private static final String WRAPPER = "WRAPPER";

    /**
     * 自定义对象比较常量
     */
    private static final String CUSTOM = "CUSTOM";

    /**
     * 演示比较操作
     * <p>
     * 根据请求的比较类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的比较类型
     * @return 演示结果响应，包含比较说明、示例代码、对比表格等
     */
    @Override
    public EqualsComparisonResponse demonstrateComparison(EqualsComparisonRequest request) {
        log.info("开始演示比较操作，comparisonType={}, demoParameter={}", 
                request.getComparisonType(), request.getDemoParameter());

        String comparisonType = request.getComparisonType();
        
        // 根据比较类型分发到不同的演示方法
        if (BASIC.equalsIgnoreCase(comparisonType)) {
            return demonstrateBasicComparison(request);
        } else if (STRING.equalsIgnoreCase(comparisonType)) {
            return demonstrateStringComparison(request);
        } else if (WRAPPER.equalsIgnoreCase(comparisonType)) {
            return demonstrateWrapperComparison(request);
        } else if (CUSTOM.equalsIgnoreCase(comparisonType)) {
            return demonstrateCustomComparison(request);
        } else {
            // 兜底逻辑：未知的比较类型
            log.warn("未知的比较类型：{}, 进入兜底流程", comparisonType);
            return createErrorResponse(comparisonType, "未知的比较类型，可选值：BASIC、STRING、WRAPPER、CUSTOM");
        }
    }

    /**
     * 演示基本类型比较
     * <p>
     * 基本类型只能使用==比较，不能使用 equals()
     * </p>
     *
     * @param request 请求参数
     * @return 基本类型比较的演示结果
     */
    private EqualsComparisonResponse demonstrateBasicComparison(EqualsComparisonRequest request) {
        int a = 10;
        int b = 10;
        int c = 20;

        boolean result1 = (a == b);  // true
        boolean result2 = (a == c);  // false

        String description = "基本类型比较演示：int a=10, b=10, c=20。" +
                "a == b 结果：" + result1 + "（值相等），" +
                "a == c 结果：" + result2 + "（值不等）。" +
                "基本类型只能使用==比较值，不能使用 equals()。";

        String keyPoints = "1. 基本类型（byte、short、int、long、float、double、char、boolean）只能使用==比较\n" +
                          "2. ==比较的是基本类型的实际值\n" +
                          "3. 基本类型没有 equals() 方法\n" +
                          "4. float 和 double 比较时要注意精度问题\n" +
                          "5. char 比较的是 Unicode 值";

        String codeExample = "// 1. int 类型比较\n" +
                            "int a = 10;\n" +
                            "int b = 10;\n" +
                            "System.out.println(a == b);  // true\n\n" +
                            
                            "// 2. double 类型比较（注意精度）\n" +
                            "double x = 0.1;\n" +
                            "double y = 0.1;\n" +
                            "System.out.println(x == y);  // true\n" +
                            "System.out.println(0.1 + 0.2 == 0.3);  // false（精度问题）\n\n" +
                            
                            "// 3. char 类型比较\n" +
                            "char c1 = 'A';\n" +
                            "char c2 = 'A';\n" +
                            "System.out.println(c1 == c2);  // true\n" +
                            "System.out.println('A' == 65);  // true（Unicode 值比较）";

        String comparisonTable = "| 基本类型 | ==比较 | equals() |\n" +
                                "|----------|--------|----------|\n" +
                                "| int | ✓ 比较值 | ✗ 不支持 |\n" +
                                "| double | ✓ 比较值（注意精度） | ✗ 不支持 |\n" +
                                "| char | ✓ 比较 Unicode 值 | ✗ 不支持 |\n" +
                                "| boolean | ✓ 比较 true/false | ✗ 不支持 |\n" +
                                "| long | ✓ 比较值 | ✗ 不支持 |";

        log.info("基本类型比较演示完成，a==b: {}, a==c: {}", result1, result2);

        return EqualsComparisonResponse.builder()
                .comparisonType(BASIC)
                .typeName("基本类型")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .success(true)
                .build();
    }

    /**
     * 演示字符串比较
     * <p>
     * String 是引用类型，==比较地址，equals() 比较内容
     * String 类已经重写了 equals() 方法
     * </p>
     *
     * @param request 请求参数
     * @return 字符串比较的演示结果
     */
    private EqualsComparisonResponse demonstrateStringComparison(EqualsComparisonRequest request) {
        // 字符串字面量（字符串常量池）
        String s1 = "hello";
        String s2 = "hello";
        
        // new 创建的字符串（堆内存）
        String s3 = new String("hello");
        String s4 = new String("hello");

        // ==比较地址
        boolean eq1 = (s1 == s2);  // true（同一个常量池对象）
        boolean eq2 = (s1 == s3);  // false（不同对象）
        boolean eq3 = (s3 == s4);  // false（不同对象）
        
        // equals() 比较内容
        boolean eq4 = s1.equals(s2);  // true
        boolean eq5 = s1.equals(s3);  // true
        boolean eq6 = s3.equals(s4);  // true

        String description = "字符串比较演示：" +
                "s1=\"hello\", s2=\"hello\"（常量池），s3=new String(\"hello\"), s4=new String(\"hello\")。" +
                "s1 == s2: " + eq1 + "（同一常量池对象），" +
                "s1 == s3: " + eq2 + "（不同对象），" +
                "s3 == s4: " + eq3 + "（不同对象）。" +
                "equals() 比较内容：s1.equals(s2): " + eq4 + ", s1.equals(s3): " + eq5 + ", s3.equals(s4): " + eq6 + "。";

        String keyPoints = "1. String 是引用类型，==比较的是内存地址\n" +
                          "2. String 类重写了 equals() 方法，比较的是字符内容\n" +
                          "3. 字符串字面量在常量池中，相同字面量共享同一对象\n" +
                          "4. new 创建的字符串在堆内存中，每次都是新对象\n" +
                          "5. 推荐使用 equals() 比较字符串内容\n" +
                          "6. 使用 intern() 可以手动将字符串放入常量池";

        String codeExample = "// 1. 字符串字面量（常量池）\n" +
                            "String s1 = \"hello\";\n" +
                            "String s2 = \"hello\";\n" +
                            "System.out.println(s1 == s2);        // true\n" +
                            "System.out.println(s1.equals(s2));   // true\n\n" +
                            
                            "// 2. new 创建字符串（堆内存）\n" +
                            "String s3 = new String(\"hello\");\n" +
                            "String s4 = new String(\"hello\");\n" +
                            "System.out.println(s3 == s4);        // false\n" +
                            "System.out.println(s3.equals(s4));   // true\n\n" +
                            
                            "// 3. intern() 方法\n" +
                            "String s5 = new String(\"world\").intern();\n" +
                            "String s6 = \"world\";\n" +
                            "System.out.println(s5 == s6);        // true";

        String comparisonTable = "| 比较方式 | == | equals() |\n" +
                                "|----------|-----|----------|\n" +
                                "| 字面量 vs 字面量 | true（同地址） | true（同内容） |\n" +
                                "| 字面量 vs new | false（不同地址） | true（同内容） |\n" +
                                "| new vs new | false（不同地址） | true（同内容） |\n" +
                                "| 结论 | 不推荐用于内容比较 | 推荐用于内容比较 |";

        log.info("字符串比较演示完成，s1==s2: {}, s1.equals(s3): {}", eq1, eq5);

        return EqualsComparisonResponse.builder()
                .comparisonType(STRING)
                .typeName("字符串")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .success(true)
                .build();
    }

    /**
     * 演示包装类比较
     * <p>
     * 包装类（Integer、Long 等）是引用类型
     * ==比较地址，equals() 比较值
     * 注意整数缓存池的影响（-128~127）
     * </p>
     *
     * @param request 请求参数
     * @return 包装类比较的演示结果
     */
    private EqualsComparisonResponse demonstrateWrapperComparison(EqualsComparisonRequest request) {
        // Integer 缓存池内的值（-128~127）
        Integer i1 = 100;
        Integer i2 = 100;
        
        // Integer 缓存池外的值
        Integer i3 = 200;
        Integer i4 = 200;
        
        // new 创建的 Integer
        Integer i5 = new Integer(100);
        Integer i6 = new Integer(100);

        // ==比较地址
        boolean eq1 = (i1 == i2);  // true（缓存池）
        boolean eq2 = (i3 == i4);  // false（超出缓存范围）
        boolean eq3 = (i1 == i5);  // false（不同对象）
        boolean eq4 = (i5 == i6);  // false（不同对象）
        
        // equals() 比较值
        boolean eq5 = i1.equals(i2);  // true
        boolean eq6 = i3.equals(i4);  // true
        boolean eq7 = i1.equals(i5);  // true

        String description = "包装类比较演示：" +
                "i1=100, i2=100（缓存池），i3=200, i4=200（超缓存），i5=new Integer(100), i6=new Integer(100)。" +
                "i1 == i2: " + eq1 + "（缓存池，-128~127），" +
                "i3 == i4: " + eq2 + "（超出缓存范围），" +
                "i1 == i5: " + eq3 + "（不同对象）。" +
                "equals() 比较值：i1.equals(i2): " + eq5 + ", i3.equals(i4): " + eq6 + ", i1.equals(i5): " + eq7 + "。";

        String keyPoints = "1. 包装类（Integer、Long 等）是引用类型\n" +
                          "2. ==比较的是对象地址，不是数值\n" +
                          "3. Integer 有缓存池（-128~127），范围内的值自动缓存\n" +
                          "4. 超出缓存范围的值，每次自动装箱都是新对象\n" +
                          "5. 包装类重写了 equals() 方法，比较的是实际数值\n" +
                          "6. 强烈建议使用 equals() 比较包装类的值";

        String codeExample = "// 1. 缓存池内（-128~127）\n" +
                            "Integer i1 = 100;\n" +
                            "Integer i2 = 100;\n" +
                            "System.out.println(i1 == i2);      // true（缓存池）\n" +
                            "System.out.println(i1.equals(i2)); // true\n\n" +
                            
                            "// 2. 缓存池外\n" +
                            "Integer i3 = 200;\n" +
                            "Integer i4 = 200;\n" +
                            "System.out.println(i3 == i4);      // false（新对象）\n" +
                            "System.out.println(i3.equals(i4)); // true\n\n" +
                            
                            "// 3. new 创建\n" +
                            "Integer i5 = new Integer(100);\n" +
                            "Integer i6 = new Integer(100);\n" +
                            "System.out.println(i5 == i6);      // false\n" +
                            "System.out.println(i5.equals(i6)); // true";

        String comparisonTable = "| 数值范围 | ==比较 | equals() | 原因 |\n" +
                                "|----------|--------|----------|------|\n" +
                                "| -128~127 | true | true | 缓存池，同一对象 |\n" +
                                "| 超出范围 | false | true | 不同对象，值相同 |\n" +
                                "| new 创建 | false | true | 总是新对象 |\n" +
                                "| 结论 | 不可靠 | 可靠 | 始终使用 equals() |";

        log.info("包装类比较演示完成，i1==i2: {}, i3.equals(i4): {}", eq1, eq6);

        return EqualsComparisonResponse.builder()
                .comparisonType(WRAPPER)
                .typeName("包装类")
                .description(description)
                .keyPoints(keyPoints)
                .codeExample(codeExample)
                .comparisonTable(comparisonTable)
                .success(true)
                .build();
    }

    /**
     * 演示自定义对象比较
     * <p>
     * 自定义对象默认 equals() 比较地址（继承自 Object）
     * 重写 equals() 后可以比较内容
     * 必须同时重写 hashCode() 保持一致性
     * </p>
     *
     * @param request 请求参数
     * @return 自定义对象比较的演示结果
     */
    private EqualsComparisonResponse demonstrateCustomComparison(EqualsComparisonRequest request) {
        // 创建 User 对象
        User user1 = new User(1L, "张三", 25);
        User user2 = new User(1L, "张三", 25);
        User user3 = new User(2L, "李四", 30);
        User user4 = user1;  // 同一引用

        // ==比较地址
        boolean eq1 = (user1 == user2);  // false（不同对象）
        boolean eq2 = (user1 == user3);  // false（不同对象）
        boolean eq3 = (user1 == user4);  // true（同一引用）
        
        // equals() 比较内容（基于 id）
        boolean eq4 = user1.equals(user2);  // true（id 相同）
        boolean eq5 = user1.equals(user3);  // false（id 不同）
        boolean eq6 = user1.equals(user4);  // true

        // hashCode 比较
        int hash1 = user1.hashCode();
        int hash2 = user2.hashCode();
        int hash3 = user3.hashCode();

        String description = "自定义对象比较演示：" +
                "user1=id:1,name:张三，user2=id:1,name:张三，user3=id:2,name:李四，user4=user1（同一引用）。" +
                "==比较：user1==user2: " + eq1 + "，user1==user3: " + eq2 + "，user1==user4: " + eq3 + "。" +
                "equals() 比较：user1.equals(user2): " + eq4 + "（id 相同），user1.equals(user3): " + eq5 + "，user1.equals(user4): " + eq6 + "。" +
                "hashCode: user1=" + hash1 + ", user2=" + hash2 + "（相等）, user3=" + hash3 + "。";

        String keyPoints = "1. 自定义对象默认 equals() 比较地址（Object 类实现）\n" +
                          "2. 重写 equals() 可以定义自己的比较规则（如比较 id）\n" +
                          "3. 重写 equals() 时必须重写 hashCode()，保持一致性\n" +
                          "4. equals() 相等的对象，hashCode() 必须相等\n" +
                          "5. hashCode() 相等的对象，equals() 不一定相等（哈希碰撞）\n" +
                          "6. 集合类（HashMap、HashSet）依赖 equals() 和 hashCode()";

        String codeExample = "// 1. 创建对象\n" +
                            "User user1 = new User(1L, \"张三\", 25);\n" +
                            "User user2 = new User(1L, \"张三\", 25);\n" +
                            "User user3 = new User(2L, \"李四\", 30);\n\n" +
                            
                            "// 2. ==比较地址\n" +
                            "System.out.println(user1 == user2);  // false\n" +
                            "System.out.println(user1 == user1);  // true\n\n" +
                            
                            "// 3. equals() 比较（基于 id）\n" +
                            "System.out.println(user1.equals(user2));  // true\n" +
                            "System.out.println(user1.equals(user3));  // false\n\n" +
                            
                            "// 4. hashCode() 一致性\n" +
                            "System.out.println(user1.hashCode());  // 相同\n" +
                            "System.out.println(user2.hashCode());  // 相同";

        String comparisonTable = "| 比较场景 | == | equals() | hashCode() |\n" +
                                "|----------|-----|----------|------------|\n" +
                                "| 同一引用 | true | true | 相同 |\n" +
                                "| 不同对象，id 相同 | false | true | 相同 |\n" +
                                "| 不同对象，id 不同 | false | false | 不同 |\n" +
                                "| 未重写 equals | 比较地址 | 比较地址 | 基于地址 |\n" +
                                "| 已重写 equals | 比较地址 | 比较内容（如 id） | 基于内容 |";

        log.info("自定义对象比较演示完成，user1.equals(user2): {}, hashCode: {}", eq4, hash1);

        return EqualsComparisonResponse.builder()
                .comparisonType(CUSTOM)
                .typeName("自定义对象")
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
     * 当请求的比较类型无效或发生错误时使用
     * </p>
     *
     * @param comparisonType 比较类型
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    private EqualsComparisonResponse createErrorResponse(String comparisonType, String errorMessage) {
        return EqualsComparisonResponse.builder()
                .comparisonType(comparisonType)
                .typeName("未知")
                .description("演示失败")
                .keyPoints("")
                .codeExample("")
                .comparisonTable("")
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
