package org.doubao.interview.agent.server.example.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 反射工具类 - 实际项目中的真实应用场景
 * <p>
 * 演示反射在真实项目中的各种应用
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
public class ReflectionExamples {

    // ==================== 场景 1: 对象转 Map（类似 Jackson 序列化） ====================
    
    /**
     * 将对象转换为 Map（模拟 JSON 序列化）
     * <p>
     * 实际应用场景：Jackson、Gson 等序列化库的核心逻辑
     * </p>
     *
     * @param obj 要转换的对象
     * @return Map 对象
     * @throws Exception 反射异常
     */
    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        Map<String, Object> map = new HashMap<>();
        
        if (obj == null) {
            return map;
        }
        
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            // 跳过静态字段
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            
            // 设置可访问（包括 private 字段）
            field.setAccessible(true);
            
            // 获取字段值
            Object value = field.get(obj);
            
            // 放入 Map
            map.put(field.getName(), value);
        }
        
        return map;
    }
    
    /**
     * 测试对象转 Map
     */
    public static void testObjectToMap() throws Exception {
        System.out.println("===== 场景 1: 对象转 Map =====");
        
        User user = new User(1L, "张三", 25, "zhangsan@example.com");
        Map<String, Object> map = objectToMap(user);
        
        System.out.println("转换后的 Map:");
        map.forEach((key, value) -> 
            System.out.println(key + " = " + value)
        );
        
        System.out.println();
    }

    // ==================== 场景 2: Map 转对象（类似 Jackson 反序列化） ====================
    
    /**
     * 将 Map 转换为对象（模拟 JSON 反序列化）
     * <p>
     * 实际应用场景：Jackson、Gson 等反序列化的核心逻辑
     * </p>
     *
     * @param map 数据 Map
     * @param clazz 目标类型
     * @return 转换后的对象
     * @throws Exception 反射异常
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) throws Exception {
        // 创建实例
        T obj = clazz.getDeclaredConstructor().newInstance();
        
        // 获取所有字段
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            // 跳过静态字段
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            
            // 设置可访问
            field.setAccessible(true);
            
            // 从 Map 中获取值
            Object value = map.get(field.getName());
            
            if (value != null) {
                // 设置字段值
                field.set(obj, value);
            }
        }
        
        return obj;
    }
    
    /**
     * 测试 Map 转对象
     */
    public static void testMapToObject() throws Exception {
        System.out.println("===== 场景 2: Map 转对象 =====");
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", 2L);
        map.put("username", "李四");
        map.put("age", 30);
        map.put("email", "lisi@example.com");
        
        User user = mapToObject(map, User.class);
        
        System.out.println("转换后的对象:");
        System.out.println("ID: " + user.getId());
        System.out.println("用户名：" + user.getUsername());
        System.out.println("年龄：" + user.getAge());
        System.out.println("邮箱：" + user.getEmail());
        
        System.out.println();
    }

    // ==================== 场景 3: 对象属性拷贝（类似 Spring BeanUtils） ====================
    
    /**
     * 同名同类型属性拷贝（模拟 Spring BeanUtils.copyProperties）
     * <p>
     * 实际应用场景：DTO 与 Entity 之间的转换
     * </p>
     *
     * @param source 源对象
     * @param target 目标对象
     * @throws Exception 反射异常
     */
    public static void copyProperties(Object source, Object target) throws Exception {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        
        Field[] sourceFields = sourceClass.getDeclaredFields();
        
        for (Field sourceField : sourceFields) {
            // 跳过静态字段
            if (Modifier.isStatic(sourceField.getModifiers())) {
                continue;
            }
            
            try {
                // 在目标类中查找同名字段
                Field targetField = targetClass.getDeclaredField(sourceField.getName());
                
                // 检查类型是否相同
                if (sourceField.getType() == targetField.getType()) {
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    
                    // 获取源字段值并设置到目标字段
                    Object value = sourceField.get(source);
                    if (value != null) {
                        targetField.set(target, value);
                    }
                }
            } catch (NoSuchFieldException e) {
                // 目标类没有这个字段，跳过
            }
        }
    }
    
    /**
     * 测试属性拷贝
     */
    public static void testCopyProperties() throws Exception {
        System.out.println("===== 场景 3: 对象属性拷贝 =====");
        
        User sourceUser = new User(1L, "王五", 28, "wangwu@example.com");
        User targetUser = new User();
        
        System.out.println("拷贝前:");
        System.out.println("Target User: " + targetUser);
        
        copyProperties(sourceUser, targetUser);
        
        System.out.println("拷贝后:");
        System.out.println("Target User: " + targetUser);
        
        System.out.println();
    }

    // ==================== 场景 4: 注解验证器（类似 Hibernate Validator） ====================
    
    /**
     * 基于注解的字段验证（模拟 Hibernate Validator）
     * <p>
     * 实际应用场景：参数校验、数据验证
     * </p>
     *
     * @param obj 要验证的对象
     * @return 验证结果（错误信息列表）
     * @throws Exception 反射异常
     */
    public static java.util.List<String> validateWithAnnotations(Object obj) throws Exception {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            // 检查是否有 FieldMeta 注解
            if (field.isAnnotationPresent(FieldMeta.class)) {
                FieldMeta meta = field.getAnnotation(FieldMeta.class);
                
                field.setAccessible(true);
                Object value = field.get(obj);
                
                // 必填验证
                if (meta.required() && (value == null || "".equals(value.toString()))) {
                    errors.add(meta.chineseName() + "不能为空");
                }
                
                // 字符串长度验证
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue != null && !strValue.isEmpty()) {
                        if (strValue.length() < meta.minLength()) {
                            errors.add(meta.chineseName() + "长度不能小于" + meta.minLength());
                        }
                        if (strValue.length() > meta.maxLength()) {
                            errors.add(meta.chineseName() + "长度不能超过" + meta.maxLength());
                        }
                    }
                }
            }
        }
        
        return errors;
    }
    
    /**
     * 测试注解验证
     */
    public static void testValidateWithAnnotations() throws Exception {
        System.out.println("===== 场景 4: 注解验证器 =====");
        
        // 测试无效产品
        Product invalidProduct = new Product();
        invalidProduct.setId(null);  // ID 为 null（必填）
        invalidProduct.setName("A");  // 名称太短（最小 2 字符）
        invalidProduct.setPrice(null);  // 价格为 null（必填）
        
        java.util.List<String> errors = validateWithAnnotations(invalidProduct);
        System.out.println("无效产品的验证错误:");
        for (String error : errors) {
            System.out.println("❌ " + error);
        }
        
        // 测试有效产品
        System.out.println("\n有效产品的验证:");
        Product validProduct = new Product(1L, "iPhone 15", 7999.0, 100, "苹果手机");
        errors = validateWithAnnotations(validProduct);
        if (errors.isEmpty()) {
            System.out.println("✅ 验证通过");
        } else {
            for (String error : errors) {
                System.out.println("❌ " + error);
            }
        }
        
        System.out.println();
    }

    // ==================== 场景 5: 动态调用方法（类似 AOP） ====================
    
    /**
     * 动态调用对象的所有 public 方法（模拟 AOP）
     * <p>
     * 实际应用场景：AOP 切面、方法拦截、日志记录
     * </p>
     *
     * @param obj 对象实例
     * @param methodName 方法名
     * @param args 参数
     * @return 方法返回值
     * @throws Exception 反射异常
     */
    public static Object invokeMethod(Object obj, String methodName, Object... args) throws Exception {
        Class<?> clazz = obj.getClass();
        
        // 根据参数类型获取方法
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        
        Method method = clazz.getMethod(methodName, paramTypes);
        
        // 调用方法
        return method.invoke(obj, args);
    }
    
    /**
     * 测试动态调用方法
     */
    public static void testInvokeMethod() throws Exception {
        System.out.println("===== 场景 5: 动态调用方法 =====");
        
        User user = new User(1L, "赵六", 35, "zhaoliu@example.com");
        
        // 调用无参方法
        System.out.println("调用 showInfo():");
        invokeMethod(user, "showInfo");
        
        // 调用带参方法
        System.out.println("调用 greet(\"你好\"):");
        Object result1 = invokeMethod(user, "greet", "你好");
        System.out.println("返回值：" + result1);
        
        // 调用多参数方法
        System.out.println("调用 createProfile(\"编程\", \"北京\"):");
        Object result2 = invokeMethod(user, "createProfile", "编程", "北京");
        System.out.println("返回值：" + result2);
        
        System.out.println();
    }

    // ==================== 场景 6: 调用私有方法（测试框架常用） ====================
    
    /**
     * 调用私有方法（测试框架常用技巧）
     * <p>
     * 实际应用场景：单元测试中测试私有方法
     * </p>
     *
     * @param obj 对象实例
     * @param methodName 方法名
     * @param args 参数
     * @return 方法返回值
     * @throws Exception 反射异常
     */
    public static Object invokePrivateMethod(Object obj, String methodName, Object... args) throws Exception {
        Class<?> clazz = obj.getClass();
        
        // 根据参数类型获取方法
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        
        Method method = clazz.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);  // 关键：绕过访问检查
        
        return method.invoke(obj, args);
    }
    
    /**
     * 测试调用私有方法
     */
    public static void testInvokePrivateMethod() throws Exception {
        System.out.println("===== 场景 6: 调用私有方法 =====");
        
        User user = new User(1L, "孙七", 40, "sunqi@example.com");
        
        // 调用私有方法
        System.out.println("调用私有方法 validateAge():");
        try {
            invokePrivateMethod(user, "validateAge");
            System.out.println("✅ 年龄验证通过");
        } catch (Exception e) {
            System.out.println("❌ 年龄验证失败：" + e.getCause().getMessage());
        }
        
        // 调用私有带返回值方法
        System.out.println("\n调用私有方法 formatEmail():");
        Object result = invokePrivateMethod(user, "formatEmail");
        System.out.println("返回值：" + result);
        
        System.out.println();
    }

    // ==================== 场景 7: 获取类的完整信息（IDE 功能实现） ====================
    
    /**
     * 获取类的完整信息（类似 IDE 的功能）
     * <p>
     * 实际应用场景：IDE 代码提示、文档生成工具
     * </p>
     *
     * @param clazz Class 对象
     */
    public static void getClassInfo(Class<?> clazz) {
        System.out.println("===== 场景 7: 获取类的完整信息 =====");
        System.out.println("类名：" + clazz.getName());
        System.out.println("简单类名：" + clazz.getSimpleName());
        System.out.println("包名：" + clazz.getPackage().getName());
        
        // 父类
        Class<?> superclass = clazz.getSuperclass();
        System.out.println("父类：" + (superclass != null ? superclass.getSimpleName() : "无"));
        
        // 接口
        Class<?>[] interfaces = clazz.getInterfaces();
        System.out.print("实现的接口：");
        if (interfaces.length > 0) {
            for (Class<?> iface : interfaces) {
                System.out.print(iface.getSimpleName() + " ");
            }
        } else {
            System.out.print("无");
        }
        System.out.println();
        
        // 修饰符
        int modifiers = clazz.getModifiers();
        System.out.print("修饰符：");
        if (Modifier.isPublic(modifiers)) System.out.print("public ");
        if (Modifier.isFinal(modifiers)) System.out.print("final ");
        if (Modifier.isAbstract(modifiers)) System.out.print("abstract ");
        System.out.println();
        
        // 字段
        System.out.println("\n字段列表:");
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            System.out.printf("  %s %s\n", 
                Modifier.toString(field.getModifiers()),
                field.getType().getSimpleName() + " " + field.getName()
            );
        }
        
        // 方法
        System.out.println("\n方法列表:");
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            System.out.printf("  %s %s %s(...)\n",
                Modifier.toString(method.getModifiers()),
                method.getReturnType().getSimpleName(),
                method.getName()
            );
        }
        
        // 构造器
        System.out.println("\n构造器列表:");
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            System.out.print("  " + Modifier.toString(constructor.getModifiers()) + " " + constructor.getName() + "(");
            Class<?>[] paramTypes = constructor.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                System.out.print(paramTypes[i].getSimpleName());
                if (i < paramTypes.length - 1) System.out.print(", ");
            }
            System.out.println(")");
        }
        
        System.out.println();
    }
    
    /**
     * 测试获取类信息
     */
    public static void testGetClassInfo() {
        getClassInfo(User.class);
    }

    // ==================== 主函数 - 运行所有测试 ====================
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Java 反射机制 - 真实应用场景演示");
        System.out.println("========================================\n");
        
        try {
            // 运行所有测试
            testObjectToMap();
            testMapToObject();
            testCopyProperties();
            testValidateWithAnnotations();
            testInvokeMethod();
            testInvokePrivateMethod();
            testGetClassInfo();
            
            System.out.println("========================================");
            System.out.println("所有演示完成！");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("演示过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
