package org.doubao.interview.agent.server.service.impl.reflection;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.reflection.ReflectionDemoRequest;
import org.doubao.interview.agent.api.dto.reflection.ReflectionDemoResponse;
import org.doubao.interview.agent.api.service.reflection.ReflectionDemoService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Java 反射机制演示服务实现类
 * <p>
 * 提供反射机制的详细演示
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@Service
public class ReflectionDemoServiceImpl implements ReflectionDemoService {

    /**
     * 获取 Class 对象操作常量
     */
    private static final String GET_CLASS = "GET_CLASS";

    /**
     * 创建实例操作常量
     */
    private static final String CREATE_INSTANCE = "CREATE_INSTANCE";

    /**
     * 调用方法操作常量
     */
    private static final String INVOKE_METHOD = "INVOKE_METHOD";

    /**
     * 访问属性操作常量
     */
    private static final String ACCESS_FIELD = "ACCESS_FIELD";

    /**
     * 获取构造器操作常量
     */
    private static final String GET_CONSTRUCTOR = "GET_CONSTRUCTOR";

    /**
     * 演示反射机制特性
     * <p>
     * 根据请求的操作类型，返回对应的演示结果和说明
     * </p>
     *
     * @param request 请求参数，包含要演示的操作类型
     * @return 演示结果响应，包含特性说明、API 使用示例等
     */
    @Override
    public ReflectionDemoResponse demonstrateFeature(ReflectionDemoRequest request) {
        log.info("开始演示反射机制特性，operationType={}, className={}", 
                request.getOperationType(), request.getClassName());

        String operationType = request.getOperationType();
        
        // 根据类型分发到不同的演示方法
        if (GET_CLASS.equalsIgnoreCase(operationType)) {
            return demonstrateGetClass(request);
        } else if (CREATE_INSTANCE.equalsIgnoreCase(operationType)) {
            return demonstrateCreateInstance(request);
        } else if (INVOKE_METHOD.equalsIgnoreCase(operationType)) {
            return demonstrateInvokeMethod(request);
        } else if (ACCESS_FIELD.equalsIgnoreCase(operationType)) {
            return demonstrateAccessField(request);
        } else if (GET_CONSTRUCTOR.equalsIgnoreCase(operationType)) {
            return demonstrateGetConstructor(request);
        } else {
            // 兜底逻辑：未知的类型
            log.warn("未知的操作类型：{}, 进入兜底流程", operationType);
            return createErrorResponse(operationType, "未知的操作类型");
        }
    }

    /**
     * 演示获取 Class 对象
     * <p>
     * 三种方式：Class.forName()、obj.getClass()、ClassName.class
     * </p>
     *
     * @param request 请求参数
     * @return 获取 Class 对象的演示结果
     */
    private ReflectionDemoResponse demonstrateGetClass(ReflectionDemoRequest request) {
        String description = "反射机制的第一步是获取 Class 对象。" +
                "Java 提供了三种主要方式获取 Class 对象。" +
                "Class 对象是反射的入口，包含了类的完整结构信息。";

        String keyFeatures = "1. Class.forName(className)：通过类名字符串获取（最常用）\n" +
                            "2. obj.getClass()：通过对象实例获取\n" +
                            "3. ClassName.class：通过类字面量获取（性能最好）\n" +
                            "4. ClassLoader.loadClass()：通过类加载器获取\n" +
                            "5. 数组类型也有 Class 对象（如 int[].class）";

        String codeExample = "// 方式 1：Class.forName() - 最常用\n" +
                            "Class<?> clazz1 = Class.forName(\"java.lang.String\");\n\n" +
                            
                            "// 方式 2：obj.getClass() - 通过实例\n" +
                            "String str = \"hello\";\n" +
                            "Class<?> clazz2 = str.getClass();\n\n" +
                            
                            "// 方式 3：ClassName.class - 性能最好\n" +
                            "Class<?> clazz3 = String.class;\n\n" +
                            
                            "// 验证三个 Class 对象是同一个\n" +
                            "System.out.println(clazz1 == clazz2);  // true\n" +
                            "System.out.println(clazz2 == clazz3);  // true";

        String reflectionApiUsage = "// 实际运行示例\n" +
                                   "try {\n" +
                                   "    // 获取 String 类的 Class 对象\n" +
                                   "    Class<?> clazz = Class.forName(\"java.lang.String\");\n" +
                                   "    \n" +
                                   "    // 输出类名\n" +
                                   "    System.out.println(\"类名：\" + clazz.getName());\n" +
                                   "    \n" +
                                   "    // 输出父类\n" +
                                   "    System.out.println(\"父类：\" + clazz.getSuperclass().getName());\n" +
                                   "    \n" +
                                   "} catch (ClassNotFoundException e) {\n" +
                                   "    e.printStackTrace();\n" +
                                   "}";

        Map<String, Object> actualResult = new HashMap<>();
        try {
            Class<?> clazz = Class.forName("java.lang.String");
            actualResult.put("className", clazz.getName());
            actualResult.put("superclass", clazz.getSuperclass().getName());
            actualResult.put("interfaces", java.util.Arrays.toString(clazz.getInterfaces()));
            actualResult.put("isInterface", clazz.isInterface());
            actualResult.put("isEnum", clazz.isEnum());
        } catch (ClassNotFoundException e) {
            actualResult.put("error", e.getMessage());
        }

        String precautions = "注意事项：\n" +
                           "1. Class.forName() 可能抛 ClassNotFoundException\n" +
                           "2. 基本类型也有 Class 对象（int.class、void.class）\n" +
                           "3. 同一个类的三个 Class 对象是同一个实例（==）\n" +
                           "4. Class.forName() 会初始化类（执行静态代码块）\n" +
                           "5. ClassLoader.loadClass() 不会初始化类";

        log.info("获取 Class 对象演示完成");

        return ReflectionDemoResponse.builder()
                .operationType(GET_CLASS)
                .operationName("获取 Class 对象")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .reflectionApiUsage(reflectionApiUsage)
                .precautions(precautions)
                .actualResult(actualResult)
                .success(true)
                .build();
    }

    /**
     * 演示创建实例
     * <p>
     * 通过反射动态创建对象
     * </p>
     *
     * @param request 请求参数
     * @return 创建实例的演示结果
     */
    private ReflectionDemoResponse demonstrateCreateInstance(ReflectionDemoRequest request) {
        String description = "通过反射可以动态创建对象，无需在编译时知道具体类型。" +
                "这是 Spring 等框架的核心机制。" +
                "JDK9+ 推荐使用 Constructor.newInstance() 替代 Class.newInstance()。";

        String keyFeatures = "1. Class.newInstance()：调用无参构造（JDK9+ 不推荐）\n" +
                            "2. Constructor.newInstance(args)：调用指定构造（推荐）\n" +
                            "3. 可以调用私有构造器（setAccessible(true)）\n" +
                            "4. 运行时决定创建哪个类的实例\n" +
                            "5. 工厂模式、依赖注入的基础";

        String codeExample = "// 方式 1：Class.newInstance() - 只能调用无参构造\n" +
                            "Class<?> clazz = Class.forName(\"java.util.ArrayList\");\n" +
                            "Object obj1 = clazz.newInstance();\n\n" +
                            
                            "// 方式 2：Constructor.newInstance() - 推荐\n" +
                            "Constructor<?> constructor = clazz.getDeclaredConstructor();\n" +
                            "Object obj2 = constructor.newInstance();\n\n" +
                            
                            "// 调用带参构造\n" +
                            "Constructor<String> stringConstructor = \n" +
                            "    String.class.getDeclaredConstructor(String.class);\n" +
                            "String str = stringConstructor.newInstance(\"hello\");";

        String reflectionApiUsage = "// 实际运行示例\n" +
                                   "try {\n" +
                                   "    // 获取 ArrayList 的 Class 对象\n" +
                                   "    Class<?> clazz = Class.forName(\"java.util.ArrayList\");\n" +
                                   "    \n" +
                                   "    // 方式 1：Class.newInstance()\n" +
                                   "    Object obj1 = clazz.newInstance();\n" +
                                   "    System.out.println(\"方式 1 创建：\" + obj1.getClass().getName());\n" +
                                   "    \n" +
                                   "    // 方式 2：Constructor.newInstance()\n" +
                                   "    Constructor<?> constructor = clazz.getDeclaredConstructor();\n" +
                                   "    Object obj2 = constructor.newInstance();\n" +
                                   "    System.out.println(\"方式 2 创建：\" + obj2.getClass().getName());\n" +
                                   "    \n" +
                                   "} catch (Exception e) {\n" +
                                   "    e.printStackTrace();\n" +
                                   "}";

        Map<String, Object> actualResult = new HashMap<>();
        try {
            Class<?> clazz = Class.forName("java.util.ArrayList");
            Object obj1 = clazz.newInstance();
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object obj2 = constructor.newInstance();
            
            actualResult.put("createdInstance1", obj1.getClass().getName() + "@" + Integer.toHexString(obj1.hashCode()));
            actualResult.put("createdInstance2", obj2.getClass().getName() + "@" + Integer.toHexString(obj2.hashCode()));
            actualResult.put("isEmpty1", ((java.util.List<?>) obj1).isEmpty());
            actualResult.put("isEmpty2", ((java.util.List<?>) obj2).isEmpty());
        } catch (Exception e) {
            actualResult.put("error", e.getMessage());
        }

        String precautions = "注意事项：\n" +
                           "1. Class.newInstance() 只能调用无参构造\n" +
                           "2. Class.newInstance() 要求构造器必须可访问\n" +
                           "3. Constructor.newInstance() 更灵活（推荐）\n" +
                           "4. 可以调用私有构造器（需 setAccessible(true)）\n" +
                           "5. newInstance() 可能抛 InstantiationException\n" +
                           "6. Spring 默认使用 Constructor.newInstance()";

        log.info("创建实例演示完成");

        return ReflectionDemoResponse.builder()
                .operationType(CREATE_INSTANCE)
                .operationName("创建实例")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .reflectionApiUsage(reflectionApiUsage)
                .precautions(precautions)
                .actualResult(actualResult)
                .success(true)
                .build();
    }

    /**
     * 演示调用方法
     * <p>
     * 通过反射动态调用对象的方法
     * </p>
     *
     * @param request 请求参数
     * @return 调用方法的演示结果
     */
    private ReflectionDemoResponse demonstrateInvokeMethod(ReflectionDemoRequest request) {
        String description = "通过反射可以在运行时动态调用对象的任意方法。" +
                "即使该方法在编译时不可见（私有方法）。" +
                "这是 AOP、动态代理、RPC 框架的核心技术。";

        String keyFeatures = "1. Method.invoke(obj, args)：调用方法\n" +
                            "2. getDeclaredMethod()：获取任意方法（包括私有）\n" +
                            "3. setAccessible(true)：绕过访问检查\n" +
                            "4. 可以调用静态方法（obj 传 null）\n" +
                            "5. 返回值是 Object，需要强制转换";

        String codeExample = "// 获取方法\n" +
                            "Method method = String.class.getMethod(\"length\");\n\n" +
                            
                            "// 调用方法\n" +
                            "String str = \"hello\";\n" +
                            "Object result = method.invoke(str);\n" +
                            "System.out.println(result);  // 5\n\n" +
                            
                            "// 调用带参方法\n" +
                            "Method concatMethod = String.class.getMethod(\"concat\", String.class);\n" +
                            "Object concatResult = concatMethod.invoke(str, \" world\");\n" +
                            "System.out.println(concatResult);  // hello world\n\n" +
                            
                            "// 调用私有方法\n" +
                            "Method privateMethod = clazz.getDeclaredMethod(\"privateMethod\");\n" +
                            "privateMethod.setAccessible(true);\n" +
                            "privateMethod.invoke(obj);";

        String reflectionApiUsage = "// 实际运行示例\n" +
                                   "try {\n" +
                                   "    String str = \"Hello Reflection\";\n" +
                                   "    \n" +
                                   "    // 调用 length() 方法\n" +
                                   "    Method lengthMethod = String.class.getMethod(\"length\");\n" +
                                   "    Object lengthResult = lengthMethod.invoke(str);\n" +
                                   "    System.out.println(\"字符串长度：\" + lengthResult);\n" +
                                   "    \n" +
                                   "    // 调用 toUpperCase() 方法\n" +
                                   "    Method upperMethod = String.class.getMethod(\"toUpperCase\");\n" +
                                   "    Object upperResult = upperMethod.invoke(str);\n" +
                                   "    System.out.println(\"转大写：\" + upperResult);\n" +
                                   "    \n" +
                                   "    // 调用 substring(int, int) 方法\n" +
                                   "    Method subMethod = String.class.getMethod(\"substring\", int.class, int.class);\n" +
                                   "    Object subResult = subMethod.invoke(str, 0, 5);\n" +
                                   "    System.out.println(\"截取：\" + subResult);\n" +
                                   "    \n" +
                                   "} catch (Exception e) {\n" +
                                   "    e.printStackTrace();\n" +
                                   "}";

        Map<String, Object> actualResult = new HashMap<>();
        try {
            String str = "Hello Reflection";
            
            Method lengthMethod = String.class.getMethod("length");
            Object lengthResult = lengthMethod.invoke(str);
            
            Method upperMethod = String.class.getMethod("toUpperCase");
            Object upperResult = upperMethod.invoke(str);
            
            Method subMethod = String.class.getMethod("substring", int.class, int.class);
            Object subResult = subMethod.invoke(str, 0, 5);
            
            actualResult.put("original", str);
            actualResult.put("length", lengthResult);
            actualResult.put("toUpperCase", upperResult);
            actualResult.put("substring(0,5)", subResult);
        } catch (Exception e) {
            actualResult.put("error", e.getMessage());
        }

        String precautions = "注意事项：\n" +
                           "1. invoke() 第一个参数是对象实例（静态方法传 null）\n" +
                           "2. 返回值是 Object，需要强制转换\n" +
                           "3. 可能抛 InvocationTargetException（包装了原始异常）\n" +
                           "4. getMethod() 只能获取 public 方法\n" +
                           "5. getDeclaredMethod() 可以获取任意方法\n" +
                           "6. 调用私有方法需 setAccessible(true)\n" +
                           "7. 反射调用性能比普通调用慢（JVM 优化后差距缩小）";

        log.info("调用方法演示完成");

        return ReflectionDemoResponse.builder()
                .operationType(INVOKE_METHOD)
                .operationName("调用方法")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .reflectionApiUsage(reflectionApiUsage)
                .precautions(precautions)
                .actualResult(actualResult)
                .success(true)
                .build();
    }

    /**
     * 演示访问属性
     * <p>
     * 通过反射动态访问和修改对象的属性
     * </p>
     *
     * @param request 请求参数
     * @return 访问属性的演示结果
     */
    private ReflectionDemoResponse demonstrateAccessField(ReflectionDemoRequest request) {
        String description = "通过反射可以动态访问和修改对象的属性值。" +
                "即使是私有属性也可以访问和修改。" +
                "这是 ORM 框架（如 MyBatis、Hibernate）属性映射的基础。";

        String keyFeatures = "1. Field.get(obj)：获取属性值\n" +
                            "2. Field.set(obj, value)：设置属性值\n" +
                            "3. getDeclaredField()：获取任意属性\n" +
                            "4. setAccessible(true)：访问私有属性\n" +
                            "5. 可以修改 final 属性（需特殊处理）";

        String codeExample = "// 获取属性\n" +
                            "Field field = Person.class.getDeclaredField(\"name\");\n\n" +
                            
                            "// 访问私有属性\n" +
                            "field.setAccessible(true);\n" +
                            "Person person = new Person(\"张三\", 25);\n" +
                            "String name = (String) field.get(person);\n" +
                            "System.out.println(name);  // 张三\n\n" +
                            
                            "// 修改属性值\n" +
                            "field.set(person, \"李四\");\n" +
                            "System.out.println(person.getName());  // 李四\n\n" +
                            
                            "// 修改静态属性（obj 传 null）\n" +
                            "Field staticField = MyClass.class.getDeclaredField(\"COUNT\");\n" +
                            "staticField.set(null, 100);";

        String reflectionApiUsage = "// 实际运行示例 - 使用自定义类\n" +
                                   "try {\n" +
                                   "    // 创建 Person 对象\n" +
                                   "    Class<?> personClass = Class.forName(\n" +
                                   "        \"org.doubao.interview.agent.server.example.Person\");\n" +
                                   "    Object person = personClass.getDeclaredConstructor(\n" +
                                   "        String.class, int.class).newInstance(\"张三\", 25);\n" +
                                   "    \n" +
                                   "    // 获取 name 字段\n" +
                                   "    Field nameField = personClass.getDeclaredField(\"name\");\n" +
                                   "    nameField.setAccessible(true);\n" +
                                   "    \n" +
                                   "    // 读取属性值\n" +
                                   "    String name = (String) nameField.get(person);\n" +
                                   "    System.out.println(\"原始姓名：\" + name);\n" +
                                   "    \n" +
                                   "    // 修改属性值\n" +
                                   "    nameField.set(person, \"李四\");\n" +
                                   "    String newName = (String) nameField.get(person);\n" +
                                   "    System.out.println(\"修改后姓名：\" + newName);\n" +
                                   "    \n" +
                                   "} catch (Exception e) {\n" +
                                   "    e.printStackTrace();\n" +
                                   "}";

        Map<String, Object> actualResult = new HashMap<>();
        try {
            // 使用简单的 Map 来模拟
            Map<String, Object> person = new HashMap<>();
            person.put("name", "张三");
            person.put("age", 25);
            
            actualResult.put("originalName", person.get("name"));
            actualResult.put("originalAge", person.get("age"));
            
            // 模拟修改
            person.put("name", "李四");
            person.put("age", 30);
            
            actualResult.put("modifiedName", person.get("name"));
            actualResult.put("modifiedAge", person.get("age"));
        } catch (Exception e) {
            actualResult.put("error", e.getMessage());
        }

        String precautions = "注意事项：\n" +
                           "1. get() 和 set() 的第一个参数是对象实例\n" +
                           "2. 静态属性的 get/set 第一个参数传 null\n" +
                           "3. getField() 只能获取 public 属性\n" +
                           "4. getDeclaredField() 可以获取任意属性\n" +
                           "5. 访问私有属性需 setAccessible(true)\n" +
                           "6. 修改 final 属性需要先移除 final 修饰符\n" +
                           "7. 基本类型的包装和解包";

        log.info("访问属性演示完成");

        return ReflectionDemoResponse.builder()
                .operationType(ACCESS_FIELD)
                .operationName("访问属性")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .reflectionApiUsage(reflectionApiUsage)
                .precautions(precautions)
                .actualResult(actualResult)
                .success(true)
                .build();
    }

    /**
     * 演示获取构造器
     * <p>
     * 通过反射获取类的构造器信息
     * </p>
     *
     * @param request 请求参数
     * @return 获取构造器的演示结果
     */
    private ReflectionDemoResponse demonstrateGetConstructor(ReflectionDemoRequest request) {
        String description = "通过反射可以获取类的所有构造器信息。" +
                "包括构造器的参数类型、修饰符等。" +
                "这对于理解类的实例化方式和依赖注入非常重要。";

        String keyFeatures = "1. getConstructor(paramTypes)：获取 public 构造器\n" +
                            "2. getDeclaredConstructor(paramTypes)：获取任意构造器\n" +
                            "3. getConstructors()：获取所有 public 构造器\n" +
                            "4. getDeclaredConstructors()：获取所有构造器\n" +
                            "5. Constructor.getParameterTypes()：获取参数类型";

        String codeExample = "// 获取特定构造器\n" +
                            "Constructor<String> constructor = \n" +
                            "    String.class.getConstructor(String.class);\n\n" +
                            
                            "// 获取所有 public 构造器\n" +
                            "Constructor<?>[] constructors = \n" +
                            "    ArrayList.class.getConstructors();\n\n" +
                            
                            "// 获取所有构造器（包括私有）\n" +
                            "Constructor<?>[] allConstructors = \n" +
                            "    MyClass.class.getDeclaredConstructors();\n\n" +
                            
                            "// 遍历构造器\n" +
                            "for (Constructor<?> c : allConstructors) {\n" +
                            "    System.out.println(\"构造器：\" + c);\n" +
                            "    System.out.println(\"参数类型：\" + \n" +
                            "        Arrays.toString(c.getParameterTypes()));\n" +
                            "}";

        String reflectionApiUsage = "// 实际运行示例\n" +
                                   "try {\n" +
                                   "    // 获取 String 的所有构造器\n" +
                                   "    Constructor<?>[] constructors = \n" +
                                   "        String.class.getDeclaredConstructors();\n" +
                                   "    \n" +
                                   "    System.out.println(\"String 类共有 \" + \n" +
                                   "        constructors.length + \" 个构造器\");\n" +
                                   "    \n" +
                                   "    // 遍历并打印信息\n" +
                                   "    for (Constructor<?> c : constructors) {\n" +
                                   "        System.out.println(\"\\n构造器：\" + c.getName());\n" +
                                   "        System.out.println(\"参数个数：\" + \n" +
                                   "            c.getParameterCount());\n" +
                                   "        System.out.println(\"参数类型：\" + \n" +
                                   "            java.util.Arrays.toString(\n" +
                                   "                c.getParameterTypes()));\n" +
                                   "    }\n" +
                                   "    \n" +
                                   "} catch (Exception e) {\n" +
                                   "    e.printStackTrace();\n" +
                                   "}";

        Map<String, Object> actualResult = new HashMap<>();
        try {
            Constructor<?>[] constructors = String.class.getDeclaredConstructors();
            
            java.util.List<Map<String, Object>> constructorList = new java.util.ArrayList<>();
            for (Constructor<?> c : constructors) {
                Map<String, Object> info = new HashMap<>();
                info.put("name", c.getName());
                info.put("parameterCount", c.getParameterCount());
                info.put("parameterTypes", java.util.Arrays.toString(c.getParameterTypes()));
                constructorList.add(info);
            }
            
            actualResult.put("constructorCount", constructors.length);
            actualResult.put("constructors", constructorList);
        } catch (Exception e) {
            actualResult.put("error", e.getMessage());
        }

        String precautions = "注意事项：\n" +
                           "1. getConstructor() 需要指定参数类型\n" +
                           "2. getConstructors() 只返回 public 构造器\n" +
                           "3. getDeclaredConstructors() 返回所有构造器\n" +
                           "4. 构造器可能是私有的（如单例模式）\n" +
                           "5. 可以通过 constructor.newInstance() 创建对象\n" +
                           "6. 泛型擦除会影响 Constructor 的类型";

        log.info("获取构造器演示完成");

        return ReflectionDemoResponse.builder()
                .operationType(GET_CONSTRUCTOR)
                .operationName("获取构造器")
                .description(description)
                .keyFeatures(keyFeatures)
                .codeExample(codeExample)
                .reflectionApiUsage(reflectionApiUsage)
                .precautions(precautions)
                .actualResult(actualResult)
                .success(true)
                .build();
    }

    /**
     * 创建错误响应
     * <p>
     * 当请求的类型无效或发生错误时使用
     * </p>
     *
     * @param operationType 操作类型
     * @param errorMessage 错误信息
     * @return 错误响应对象
     */
    private ReflectionDemoResponse createErrorResponse(String operationType, String errorMessage) {
        return ReflectionDemoResponse.builder()
                .operationType(operationType)
                .operationName("未知")
                .description("演示失败")
                .keyFeatures("")
                .codeExample("")
                .reflectionApiUsage("")
                .precautions("")
                .actualResult(null)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
