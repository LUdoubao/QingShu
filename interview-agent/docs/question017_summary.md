# 问题 017: Java 反射机制 - 总结文档

## 一、核心知识点回顾

### 1.1 什么是反射机制

**反射（Reflection）** 是 Java 语言的核心特性之一，它允许程序在**运行时**获取类的完整结构信息（属性、方法、构造器），并能够动态地创建对象、调用方法、访问和修改属性。

**一句话概括**：反射让 Java 代码可以" introspect"（内省）自身，并在运行时操作任意类。

### 1.2 反射的核心能力

| 能力 | 说明 | 核心 API |
|------|------|----------|
| **获取 Class 对象** | 获取类的类型信息 | Class.forName()、getClass()、ClassName.class |
| **创建实例** | 动态创建对象 | Class.newInstance()、Constructor.newInstance() |
| **调用方法** | 动态执行方法 | Method.invoke() |
| **访问属性** | 读取/修改属性值 | Field.get()、Field.set() |
| **获取构造器** | 获取构造器信息 | Constructor、getConstructor() |

### 1.3 反射的重要性

- ✅ **Spring 框架的基石**：依赖注入（DI）、AOP、Bean 管理
- ✅ **动态代理的基础**：JDK 动态代理、CGLIB
- ✅ **ORM 框架的核心**：MyBatis、Hibernate 的对象关系映射
- ✅ **IDE 的智能提示**：Eclipse、IntelliJ IDEA 的代码补全
- ✅ **序列化/反序列化**：Jackson、Gson、FastJSON
- ✅ **注解处理**：编译时和运行时注解解析

## 二、详细作用说明

### 2.1 获取 Class 对象 - 反射的入口

#### 三种主要方式

```java
// 方式 1：Class.forName() - 最常用
Class<?> clazz1 = Class.forName("java.lang.String");

// 方式 2：obj.getClass() - 通过实例
String str = "hello";
Class<?> clazz2 = str.getClass();

// 方式 3：ClassName.class - 性能最好
Class<?> clazz3 = String.class;

// 验证：三个 Class 对象是同一个
System.out.println(clazz1 == clazz2);  // true
System.out.println(clazz2 == clazz3);  // true
```

#### 第四种方式：ClassLoader

```java
// 通过类加载器获取（不会初始化类）
ClassLoader classLoader = MyClass.class.getClassLoader();
Class<?> clazz4 = classLoader.loadClass("java.lang.String");
```

#### Class 对象包含的信息

```java
Class<?> clazz = String.class;

// 基本信息
System.out.println("类名：" + clazz.getName());           // java.lang.String
System.out.println("简单类名：" + clazz.getSimpleName());   // String
System.out.println("父类：" + clazz.getSuperclass());      // java.lang.Object
System.out.println("包名：" + clazz.getPackage().getName()); // java.lang

// 接口信息
Class<?>[] interfaces = clazz.getInterfaces();
System.out.println("实现的接口：" + Arrays.toString(interfaces));

// 修饰符
int modifiers = clazz.getModifiers();
System.out.println("public: " + Modifier.isPublic(modifiers));
System.out.println("final: " + Modifier.isFinal(modifiers));
System.out.println("abstract: " + Modifier.isAbstract(modifiers));

// 判断类型
System.out.println("是否是接口：" + clazz.isInterface());
System.out.println("是否是枚举：" + clazz.isEnum());
System.out.println("是否是数组：" + clazz.isArray());
System.out.println("是否是基本类型：" + clazz.isPrimitive());
```

#### 实际运行示例

```java
try {
    Class<?> clazz = Class.forName("java.lang.String");
    
    System.out.println("类名：" + clazz.getName());
    System.out.println("父类：" + clazz.getSuperclass().getName());
    System.out.println("接口：" + Arrays.toString(clazz.getInterfaces()));
    System.out.println("是否 final: " + Modifier.isFinal(clazz.getModifiers()));
    
} catch (ClassNotFoundException e) {
    e.printStackTrace();
}
```

### 2.2 创建实例 - 动态对象创建

#### 方式对比

```java
// 方式 1：Class.newInstance() - JDK9+ 不推荐
Class<?> clazz = Class.forName("java.util.ArrayList");
Object obj1 = clazz.newInstance();  // 只能调用无参构造

// 方式 2：Constructor.newInstance() - 推荐
Constructor<?> constructor = clazz.getDeclaredConstructor();
Object obj2 = constructor.newInstance();  // 更灵活

// 方式 3：调用带参构造
Constructor<String> stringConstructor = 
    String.class.getDeclaredConstructor(String.class);
String str = stringConstructor.newInstance("hello");
```

#### 调用私有构造器

```java
public class Singleton {
    private static Singleton instance;
    
    // 私有构造器
    private Singleton() {}
    
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}

// 通过反射创建单例对象
Class<?> clazz = Singleton.class;
Constructor<?> constructor = clazz.getDeclaredConstructor();
constructor.setAccessible(true);  // 绕过访问检查
Singleton instance = (Singleton) constructor.newInstance();
```

#### Spring 中的应用

```java
// Spring Bean 的创建就是基于反射
@Service
public class UserService {
    // ...
}

// Spring 内部大致这样创建 Bean
Class<?> clazz = Class.forName("com.example.UserService");
Constructor<?> constructor = clazz.getDeclaredConstructor();
Object bean = constructor.newInstance();
applicationContext.registerBean("userService", bean);
```

### 2.3 调用方法 - 动态方法执行

#### 基本用法

```java
// 获取方法
Method method = String.class.getMethod("length");

// 调用方法
String str = "hello";
Object result = method.invoke(str);
System.out.println(result);  // 5

// 调用带参方法
Method concatMethod = String.class.getMethod("concat", String.class);
Object concatResult = concatMethod.invoke(str, " world");
System.out.println(concatResult);  // hello world
```

#### 调用不同返回值的方法

```java
// 返回基本类型
Method lengthMethod = String.class.getMethod("length");
int length = (int) lengthMethod.invoke("hello");

// 返回对象
Method upperMethod = String.class.getMethod("toUpperCase");
String upper = (String) upperMethod.invoke("hello");

// 返回 void（返回 null）
Method printlnMethod = System.out.getClass().getMethod("println", String.class);
Object voidResult = printlnMethod.invoke(System.out, "Hello");
System.out.println("void 方法返回：" + voidResult);  // null
```

#### 调用静态方法

```java
// Math.random() 是静态方法
Method randomMethod = Math.class.getMethod("random");
Object result = randomMethod.invoke(null);  // 静态方法第一个参数传 null
System.out.println("随机数：" + result);

// Math.abs(int) 也是静态方法
Method absMethod = Math.class.getMethod("abs", int.class);
Object absResult = absMethod.invoke(null, -100);
System.out.println("绝对值：" + absResult);  // 100
```

#### 调用私有方法

```java
public class MyClass {
    private void privateMethod(String param) {
        System.out.println("私有方法被调用：" + param);
    }
}

// 调用私有方法
Class<?> clazz = MyClass.class;
Method privateMethod = clazz.getDeclaredMethod("privateMethod", String.class);
privateMethod.setAccessible(true);  // 关键：绕过访问检查
MyClass obj = new MyClass();
privateMethod.invoke(obj, "测试参数");
```

#### 方法重载的处理

```java
public class Calculator {
    public int add(int a, int b) { return a + b; }
    public double add(double a, double b) { return a + b; }
    public int add(int a, int b, int c) { return a + b + c; }
}

// 获取特定签名的方法
Method addMethod1 = Calculator.class.getMethod("add", int.class, int.class);
Method addMethod2 = Calculator.class.getMethod("add", double.class, double.class);
Method addMethod3 = Calculator.class.getMethod("add", int.class, int.class, int.class);

Calculator calc = new Calculator();
System.out.println(addMethod1.invoke(calc, 10, 20));  // 30
System.out.println(addMethod2.invoke(calc, 10.5, 20.3));  // 30.8
System.out.println(addMethod3.invoke(calc, 10, 20, 30));  // 60
```

#### 异常处理

```java
try {
    Method method = String.class.getMethod("charAt", int.class);
    String str = "hello";
    
    // 正常调用
    Object result = method.invoke(str, 1);  // 'e'
    
    // 可能抛 InvocationTargetException
    method.invoke(str, 100);  // 索引越界
    
} catch (NoSuchMethodException e) {
    // 方法不存在
    e.printStackTrace();
} catch (IllegalAccessException e) {
    // 无法访问方法
    e.printStackTrace();
} catch (InvocationTargetException e) {
    // 目标方法抛出的异常
    System.out.println("原始异常：" + e.getCause());
    e.printStackTrace();
}
```

### 2.4 访问属性 - 动态读写字段

#### 访问 public 属性

```java
public class Person {
    public String name = "张三";
    public int age = 25;
}

Person person = new Person();
Class<?> clazz = Person.class;

// 获取 field
Field nameField = clazz.getField("name");
Field ageField = clazz.getField("age");

// 读取属性值
String name = (String) nameField.get(person);
int age = ageField.getInt(person);  // getInt 自动解包

System.out.println(name);  // 张三
System.out.println(age);   // 25

// 修改属性值
nameField.set(person, "李四");
ageField.setInt(person, 30);

System.out.println(person.name);  // 李四
System.out.println(person.age);   // 30
```

#### 访问 private 属性

```java
public class Person {
    private String name = "张三";
    private int age = 25;
}

Person person = new Person();
Class<?> clazz = Person.class;

// 获取私有 field（用 getDeclaredField）
Field nameField = clazz.getDeclaredField("name");
Field ageField = clazz.getDeclaredField("age");

// 关键：设置可访问
nameField.setAccessible(true);
ageField.setAccessible(true);

// 读取属性值
String name = (String) nameField.get(person);
int age = ageField.getInt(person);

System.out.println(name);  // 张三
System.out.println(age);   // 25

// 修改属性值
nameField.set(person, "李四");
System.out.println(person);  // 可通过 getter 验证
```

#### 访问静态属性

```java
public class Constants {
    public static int COUNT = 100;
    private static String NAME = "Test";
}

// 访问 public 静态属性
Field countField = Constants.class.getField("COUNT");
int count = (int) countField.get(null);  // 静态属性传 null
System.out.println(count);  // 100

countField.set(null, 200);  // 修改静态属性
System.out.println(Constants.COUNT);  // 200

// 访问 private 静态属性
Field nameField = Constants.class.getDeclaredField("NAME");
nameField.setAccessible(true);
String name = (String) nameField.get(null);  // 传 null
nameField.set(null, "NewName");
```

#### 修改 final 属性

```java
public class FinalTest {
    public final int value = 100;
}

FinalTest obj = new FinalTest();
Class<?> clazz = FinalTest.class;

Field valueField = clazz.getField("value");

// 移除 final 修饰符
valueField.setAccessible(true);
Field modifiersField = Field.class.getDeclaredField("modifiers");
modifiersField.setAccessible(true);
modifiersField.setInt(valueField, valueField.getModifiers() & ~Modifier.FINAL);

// 现在可以修改 final 属性
valueField.setInt(obj, 200);
System.out.println(obj.value);  // 200
```

⚠️ **警告**：修改 final 属性是不安全的行为，仅用于学习和特殊场景

### 2.5 获取构造器 - 实例化的基础

#### 获取特定构造器

```java
// 获取 public 构造器
Constructor<String> constructor = 
    String.class.getConstructor(String.class);

// 获取私有构造器
Constructor<?> privateConstructor = 
    MyClass.class.getDeclaredConstructor();
privateConstructor.setAccessible(true);
```

#### 获取所有构造器

```java
// 获取所有 public 构造器
Constructor<?>[] constructors = ArrayList.class.getConstructors();
System.out.println("ArrayList 有 " + constructors.length + " 个 public 构造器");

// 获取所有构造器（包括私有）
Constructor<?>[] allConstructors = MyClass.class.getDeclaredConstructors();
System.out.println("MyClass 共有 " + allConstructors.length + " 个构造器");

// 遍历构造器
for (Constructor<?> c : allConstructors) {
    System.out.println("\n构造器：" + c.getName());
    System.out.println("参数个数：" + c.getParameterCount());
    System.out.println("参数类型：" + Arrays.toString(c.getParameterTypes()));
    System.out.println("异常类型：" + Arrays.toString(c.getExceptionTypes()));
}
```

#### 查看 String 类的构造器

```java
Constructor<?>[] constructors = String.class.getDeclaredConstructors();

for (Constructor<?> c : constructors) {
    System.out.println("构造器：" + c);
    System.out.println("参数类型：" + Arrays.toString(c.getParameterTypes()));
    System.out.println("---");
}

// 输出示例：
// 构造器：public java.lang.String()
// 参数类型：[]
// ---
// 构造器：public java.lang.String(java.lang.String)
// 参数类型：[class java.lang.String]
// ---
// 构造器：public java.lang.String(char[])
// 参数类型：[char[]]
// ---
// ...
```

## 三、反射 API 速查表

### 3.1 Class 类常用方法

| 方法 | 说明 | 示例 |
|------|------|------|
| `forName(String className)` | 根据类名获取 Class 对象 | Class.forName("java.lang.String") |
| `getName()` | 获取全限定类名 | clazz.getName() |
| `getSimpleName()` | 获取简单类名 | clazz.getSimpleName() |
| `getSuperclass()` | 获取父类 | clazz.getSuperclass() |
| `getInterfaces()` | 获取实现的接口 | clazz.getInterfaces() |
| `getFields()` | 获取所有 public 属性 | clazz.getFields() |
| `getDeclaredFields()` | 获取所有属性 | clazz.getDeclaredFields() |
| `getMethods()` | 获取所有 public 方法 | clazz.getMethods() |
| `getDeclaredMethods()` | 获取所有方法 | clazz.getDeclaredMethods() |
| `getConstructors()` | 获取所有 public 构造器 | clazz.getConstructors() |
| `getDeclaredConstructors()` | 获取所有构造器 | clazz.getDeclaredConstructors() |

### 3.2 Field 类常用方法

| 方法 | 说明 | 示例 |
|------|------|------|
| `getName()` | 获取属性名 | field.getName() |
| `getType()` | 获取属性类型 | field.getType() |
| `getModifiers()` | 获取修饰符 | field.getModifiers() |
| `get(Object obj)` | 获取属性值 | field.get(obj) |
| `set(Object obj, Object value)` | 设置属性值 | field.set(obj, value) |
| `getInt(Object obj)` | 获取 int 值 | field.getInt(obj) |
| `setInt(Object obj, int value)` | 设置 int 值 | field.setInt(obj, 100) |
| `setAccessible(boolean flag)` | 设置可访问性 | field.setAccessible(true) |

### 3.3 Method 类常用方法

| 方法 | 说明 | 示例 |
|------|------|------|
| `getName()` | 获取方法名 | method.getName() |
| `getReturnType()` | 获取返回类型 | method.getReturnType() |
| `getParameterTypes()` | 获取参数类型 | method.getParameterTypes() |
| `getModifiers()` | 获取修饰符 | method.getModifiers() |
| `invoke(Object obj, Object... args)` | 调用方法 | method.invoke(obj, arg1, arg2) |
| `setAccessible(boolean flag)` | 设置可访问性 | method.setAccessible(true) |

### 3.4 Constructor 类常用方法

| 方法 | 说明 | 示例 |
|------|------|------|
| `getName()` | 获取构造器名 | constructor.getName() |
| `getParameterTypes()` | 获取参数类型 | constructor.getParameterTypes() |
| `getModifiers()` | 获取修饰符 | constructor.getModifiers() |
| `newInstance(Object... args)` | 创建实例 | constructor.newInstance(arg1, arg2) |
| `setAccessible(boolean flag)` | 设置可访问性 | constructor.setAccessible(true) |

## 四、反射的实际应用场景

### 4.1 Spring 依赖注入

```java
// Spring 容器内部大致这样工作
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}

// Spring 通过反射注入依赖
Class<?> clazz = UserService.class;
Object userService = clazz.getDeclaredConstructor().newInstance();

Field userRepoField = clazz.getDeclaredField("userRepository");
userRepoField.setAccessible(true);
UserRepository repo = applicationContext.getBean(UserRepository.class);
userRepoField.set(userService, repo);
```

### 4.2 MyBatis 结果映射

```java
// MyBatis 将 ResultSet 映射到对象
public class User {
    private Long id;
    private String username;
    private String email;
}

// 反射实现自动映射
ResultSet rs = statement.executeQuery(sql);
while (rs.next()) {
    User user = (User) clazz.getDeclaredConstructor().newInstance();
    
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
        field.setAccessible(true);
        String columnName = field.getName();
        Object value = rs.getObject(columnName);
        field.set(user, value);
    }
    
    userList.add(user);
}
```

### 4.3 Jackson 序列化/反序列化

```java
// JSON 转对象
String json = "{\"id\":1,\"username\":\"张三\"}";
User user = objectMapper.readValue(json, User.class);

// 对象转 JSON
String json = objectMapper.writeValueAsString(user);

// 底层使用反射获取属性和值
Field[] fields = User.class.getDeclaredFields();
for (Field field : fields) {
    field.setAccessible(true);
    String fieldName = field.getName();
    Object fieldValue = field.get(user);
    // 写入 JSON
}
```

### 4.4 JDBC 驱动加载

```java
// 传统方式（JDBC 4.0 之前）
Class.forName("com.mysql.jdbc.Driver");

// 原理：Driver 类的静态代码块会注册自己
static {
    DriverManager.registerDriver(new Driver());
}

// 现代方式（JDBC 4.0+）
// ServiceLoader 机制，无需手动加载
Connection conn = DriverManager.getConnection(url, user, password);
```

### 4.5 动态代理

```java
// JDK 动态代理
UserService target = new UserServiceImpl();

UserService proxy = (UserService) Proxy.newProxyInstance(
    target.getClass().getClassLoader(),
    target.getClass().getInterfaces(),
    (proxyObj, method, args) -> {
        System.out.println("前置增强");
        Object result = method.invoke(target, args);
        System.out.println("后置增强");
        return result;
    }
);

// 调用代理方法
proxy.getUserById(1L);
```

### 4.6 注解处理

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
    String value() default "";
}

public class MyTest {
    @Test("测试方法 1")
    public void testMethod1() { }
    
    @Test("测试方法 2")
    public void testMethod2() { }
}

// 运行时解析注解
Class<?> clazz = MyTest.class;
Method[] methods = clazz.getDeclaredMethods();

for (Method method : methods) {
    if (method.isAnnotationPresent(Test.class)) {
        Test annotation = method.getAnnotation(Test.class);
        System.out.println("方法：" + method.getName());
        System.out.println("注解值：" + annotation.value());
        
        // 执行测试方法
        method.setAccessible(true);
        method.invoke(clazz.getDeclaredConstructor().newInstance());
    }
}
```

## 五、面试高频考点

### 5.1 经典问题与答案

#### Q1: 什么是反射？有什么优缺点？

**答**：

**定义**：
反射是 Java 在运行时获取类的完整结构信息，并动态操作对象的能力。

**优点**：
1. **灵活性高**：运行时决定创建哪个对象、调用哪个方法
2. **通用性强**：可以编写与具体类无关的通用代码
3. **扩展性好**：支持插件化架构，无需修改代码即可扩展
4. **框架基础**：是 Spring、MyBatis 等框架的基石

**缺点**：
1. **性能开销**：反射调用比普通调用慢（JVM 优化后差距缩小）
2. **安全性问题**：可以访问私有成员，破坏封装性
3. **代码可读性差**：反射代码难以理解和维护
4. **编译期检查失效**：错误只能在运行时发现

#### Q2: 反射的性能真的差吗？差多少？

**答**：是的，但现代 JVM 已经大幅优化。

**性能对比**（近似值）：

```java
// 普通调用：~1ms（100 万次）
for (int i = 0; i < 1_000_000; i++) {
    obj.method();
}

// 反射调用（不缓存）：~100ms（100 万次）
for (int i = 0; i < 1_000_000; i++) {
    Method method = clazz.getMethod("method");
    method.invoke(obj);
}

// 反射调用（缓存 Method）：~10ms（100 万次）
Method method = clazz.getMethod("method");
for (int i = 0; i < 1_000_000; i++) {
    method.invoke(obj);
}

// setAccessible(true)：~5ms（100 万次）
method.setAccessible(true);
for (int i = 0; i < 1_000_000; i++) {
    method.invoke(obj);
}
```

**结论**：
- 反射调用确实慢（5-100 倍）
- 缓存 Method 对象可大幅提升性能
- 对于非热点代码，性能差异可忽略
- Spring 等框架大量使用反射，但整体性能依然优秀

#### Q3: 如何突破泛型擦除的限制？

**答**：通过反射。

```java
List<String> list1 = new ArrayList<>();
List<Integer> list2 = new ArrayList<>();

// 运行时泛型擦除
System.out.println(list1.getClass() == list2.getClass());  // true

// 通过反射添加不同类型的元素
Method addMethod = list1.getClass().getMethod("add", Object.class);
addMethod.invoke(list1, "字符串");
addMethod.invoke(list1, 123);

System.out.println(list1);  // [字符串，123]
```

#### Q4: 反射能调用私有方法吗？如何实现？

**答**：可以，通过 setAccessible(true)。

```java
public class MyClass {
    private void privateMethod(String param) {
        System.out.println("私有方法：" + param);
    }
}

// 调用私有方法
Class<?> clazz = MyClass.class;
Method method = clazz.getDeclaredMethod("privateMethod", String.class);
method.setAccessible(true);  // 关键步骤
MyClass obj = new MyClass();
method.invoke(obj, "测试");
```

#### Q5: 反射在实际项目中有哪些应用？

**答**：

1. **Spring 框架**
   - 依赖注入（DI）
   - AOP 动态代理
   - Bean 生命周期管理

2. **ORM 框架**
   - MyBatis 结果映射
   - Hibernate 对象持久化

3. **序列化工具**
   - Jackson、Gson 处理 JSON
   - Protobuf 序列化

4. **测试框架**
   - JUnit 运行测试方法
   - Mockito Mock 对象

5. **开发工具**
   - IDE 代码补全
   - Lombok 自动生成代码

6. **数据库驱动**
   - JDBC 驱动加载
   - 连接池管理

### 5.2 实战应用场景

#### 场景 1: 实现简单的 IOC 容器

```java
public class SimpleIOC {
    private Map<String, Object> beanMap = new HashMap<>();
    
    public void register(Class<?> clazz) throws Exception {
        // 扫描带有@Component 注解的类
        if (clazz.isAnnotationPresent(Component.class)) {
            String beanName = clazz.getSimpleName();
            
            // 创建实例
            Object bean = clazz.getDeclaredConstructor().newInstance();
            beanMap.put(beanName, bean);
            
            // 注入依赖
            injectDependencies(bean);
        }
    }
    
    private void injectDependencies(Object bean) throws Exception {
        Class<?> clazz = bean.getClass();
        
        // 扫描带有@Autowired 的字段
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                String dependencyName = field.getType().getSimpleName();
                Object dependency = beanMap.get(dependencyName);
                field.set(bean, dependency);
            }
        }
    }
    
    public Object getBean(String name) {
        return beanMap.get(name);
    }
}
```

#### 场景 2: 实现通用的对象拷贝工具

```java
public class BeanUtils {
    
    /**
     * 同名同类型属性拷贝
     */
    public static void copyProperties(Object source, Object target) {
        try {
            Class<?> sourceClass = source.getClass();
            Class<?> targetClass = target.getClass();
            
            Field[] sourceFields = sourceClass.getDeclaredFields();
            
            for (Field sourceField : sourceFields) {
                try {
                    // 在目标类中查找同名字段
                    Field targetField = targetClass.getDeclaredField(sourceField.getName());
                    
                    // 检查类型是否相同
                    if (sourceField.getType() == targetField.getType()) {
                        sourceField.setAccessible(true);
                        targetField.setAccessible(true);
                        
                        Object value = sourceField.get(source);
                        if (value != null) {
                            targetField.set(target, value);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    // 目标类没有这个字段，跳过
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("属性拷贝失败", e);
        }
    }
}

// 使用示例
User user = new User("张三", 25);
UserDTO dto = new UserDTO();
BeanUtils.copyProperties(user, dto);
```

#### 场景 3: 实现简单的 ORM 映射

```java
@TableName("users")
public class User {
    @Id
    @Column("id")
    private Long id;
    
    @Column("username")
    private String username;
    
    @Column("email")
    private String email;
}

public class SimpleORM {
    
    public <T> T findById(Class<T> clazz, Long id) throws Exception {
        // 获取表名
        TableName tableName = clazz.getAnnotation(TableName.class);
        String sql = "SELECT * FROM " + tableName.value() + " WHERE id = ?";
        
        // 执行查询
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            // 创建对象
            T obj = clazz.getDeclaredConstructor().newInstance();
            
            // 映射字段
            for (Field field : clazz.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    field.setAccessible(true);
                    String columnName = column.value();
                    Object value = rs.getObject(columnName);
                    field.set(obj, value);
                }
            }
            
            return obj;
        }
        
        return null;
    }
}
```

## 六、注意事项和最佳实践

### 6.1 性能优化

```java
// ❌ 不推荐：每次都获取 Method
for (int i = 0; i < 1000; i++) {
    Method method = clazz.getMethod("getValue");
    method.invoke(obj);
}

// ✅ 推荐：缓存 Method 对象
Method method = clazz.getMethod("getValue");
for (int i = 0; i < 1000; i++) {
    method.invoke(obj);
}

// ✅ 更佳：使用静态缓存
private static final Map<Class<?>, Map<String, Method>> METHOD_CACHE = 
    new ConcurrentHashMap<>();

public Method getCachedMethod(Class<?> clazz, String methodName) {
    return METHOD_CACHE
        .computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
        .computeIfAbsent(methodName, k -> {
            try {
                return clazz.getMethod(k);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
}
```

### 6.2 安全检查

```java
// 访问私有成员前进行安全检查
public void accessPrivateField(Object obj, String fieldName) {
    SecurityManager securityManager = System.getSecurityManager();
    if (securityManager != null) {
        securityManager.checkPermission(
            new ReflectPermission("suppressAccessChecks")
        );
    }
    
    // 继续访问...
}
```

### 6.3 异常处理

```java
// ✅ 推荐：明确处理各种异常
try {
    Class<?> clazz = Class.forName("com.example.MyClass");
    Method method = clazz.getMethod("doSomething", String.class);
    Object instance = clazz.getDeclaredConstructor().newInstance();
    method.invoke(instance, "param");
    
} catch (ClassNotFoundException e) {
    // 类不存在
    log.error("类未找到", e);
} catch (NoSuchMethodException e) {
    // 方法不存在
    log.error("方法未找到", e);
} catch (IllegalAccessException e) {
    // 无法访问
    log.error("无法访问方法", e);
} catch (InvocationTargetException e) {
    // 目标方法抛异常
    log.error("方法执行失败", e.getCause());
} catch (InstantiationException e) {
    // 实例化失败
    log.error("无法创建实例", e);
}
```

### 6.4 代码规范

```java
// ✅ 推荐：封装反射逻辑
public class ReflectionUtil {
    
    public static Object createInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建实例失败", e);
        }
    }
    
    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        try {
            Method method = findMethod(obj.getClass(), methodName, args);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException("调用方法失败", e);
        }
    }
    
    private static Method findMethod(Class<?> clazz, String methodName, Object[] args) 
            throws NoSuchMethodException {
        // 实现方法查找逻辑
    }
}
```

## 七、总结

### 7.1 核心要点

1. **反射是什么**：运行时获取类的信息并动态操作对象
2. **核心 API**：Class、Field、Method、Constructor
3. **三种获取 Class 方式**：forName()、getClass()、ClassName.class
4. **五大操作**：获取 Class、创建实例、调用方法、访问属性、获取构造器
5. **性能考虑**：反射调用较慢，需缓存 Method 对象
6. **应用场景**：Spring、MyBatis、序列化、动态代理等

### 7.2 记忆口诀

```
反射机制真强大，运行时来把类查
三种方式获 Class，forName getClass 类字面
创建实例 newInstance，调用方法 invoke
访问属性 get 和 set，私有也能强访问
Spring 框架它做基，ORM 中把活干
性能虽有小额耗，缓存之后影响小
```

### 7.3 延伸学习

- Java 泛型与类型擦除
- 动态代理（JDK Proxy、CGLIB）
- ASM、ByteBuddy字节码操作
- Java Module System（JDK9+）对反射的限制
- VarHandle（JDK9+）替代反射的新方案

---

**对应面试题**：问题 017 - Java 反射机制

**关键词**：反射，动态获取，Class 对象，运行时

**难度等级**：⭐⭐⭐⭐⭐（核心考点，必须深入理解）
