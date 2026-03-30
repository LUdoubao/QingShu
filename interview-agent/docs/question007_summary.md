# 问题 007: final、finally、finalize 的区别 - 总结文档

## 一、核心知识点回顾

### 1.1 三个关键字的本质区别

| 特性 | final | finally | finalize |
|------|-------|---------|----------|
| **类型** | 修饰符 | 异常处理关键字 | Object 方法 |
| **作用对象** | 类、方法、变量 | try-catch 块 | 垃圾回收机制 |
| **核心功能** | 不可变性 | 必执行代码块 | GC 前清理 |
| **JDK 状态** | 正常使用 | 正常使用 | JDK9+ @Deprecated |
| **使用频率** | 高 | 中 | 极低（废弃） |

### 1.2 形象比喻

- **final** 像"封印"：一旦贴上就不能改变（类不能继承、方法不能重写、变量不能修改）
- **finally** 像"保险箱"：无论发生什么都会执行（资源清理的保险）
- **finalize** 像"临终遗言"：对象死前（被 GC 回收前）的最后机会（但不可靠）

## 二、详细作用说明

### 2.1 final - 不可变的修饰符

#### 核心特性

```java
// 1. final 修饰类（不可继承）
public final class MathUtil {
    // 工具类，不允许被继承
    // 典型应用：String、Integer、Math 等
    public static int add(int a, int b) {
        return a + b;
    }
}

// ❌ 错误：不能继承 final 类
// public class AdvancedMath extends MathUtil { }

// 2. final 修饰方法（不可重写）
public class Parent {
    public final void show() {
        System.out.println("父类方法");
    }
}

public class Child extends Parent {
    // ❌ 错误：不能重写 final 方法
    // @Override
    // public void show() { }
}

// 3. final 修饰变量（常量）
public class Constants {
    // 编译时常量（必须在声明时或静态代码块赋值）
    public static final double PI = 3.14159;
    
    // 实例常量（可以在构造器中赋值）
    private final String id;
    
    public Constants(String id) {
        this.id = id;  // 只能赋值一次
    }
    
    // final 修饰数组
    private final int[] numbers = {1, 2, 3};
    
    public void test() {
        // numbers = new int[]{4,5}; // ❌ 错误：不能修改引用
        numbers[0] = 100;  // ✓ 正确：可以修改数组元素
    }
    
    // final 修饰集合
    private final List<String> list = new ArrayList<>();
    
    public void modifyList() {
        // list = new LinkedList<>(); // ❌ 错误：不能修改引用
        list.add("item");  // ✓ 正确：可以修改集合内容
    }
}

// 4. final 修饰参数（方法内部不能修改）
public class Calculator {
    public void calculate(final int base) {
        // base = 100;  // ❌ 错误：不能修改 final 参数
        System.out.println("基数：" + base);
    }
}

// 5. final 修饰匿名内部类参数（JDK 8+ effective final）
public class Outer {
    public void method() {
        int count = 0;  // effective final（虽无 final 修饰符，但不能修改）
        
        Runnable runnable = () -> {
            // count++;  // ❌ 错误：不能修改 effective final 变量
            System.out.println("count=" + count);
        };
    }
}
```

#### final 的底层原理

**1. final 字段在内存中的表现**：
```java
public class FinalTest {
    private final int value1 = 100;  // 编译期常量
    private final int value2;         // 实例常量
    
    public FinalTest(int val) {
        this.value2 = val;  // 构造器中赋值
    }
}
```

字节码层面：
- `value1` 会在编译期放入常量池
- `value2` 在运行时通过构造函数初始化
- final 字段一旦初始化后不能再赋值

**2. final 引用类型的本质**：
```java
public class ReferenceTest {
    private final StringBuilder sb = new StringBuilder("hello");
    
    public void test() {
        // sb = new StringBuilder("world");  // ❌ 错误：引用不能改
        sb.append(" world");  // ✓ 正确：内容可以改
        System.out.println(sb.toString());  // "hello world"
    }
}
```

**关键点**：final 保证的是引用地址不变，不是对象内容不变

#### 为什么使用 final？

1. **定义常量**
   ```java
   public interface Constants {
       String APP_NAME = "MyApp";
       int MAX_RETRY = 3;
   }
   ```

2. **保证安全性**
   ```java
   // String 类为什么是 final？
   public final class String {
       // 防止被继承，保证不可变性
       // 确保字符串常量池的安全
   }
   ```

3. **性能优化**（早期版本）
   ```java
   // 编译器可能将 final 方法内联（现代 JVM 自动优化）
   public final int getValue() {
       return 100;
   }
   ```

4. **设计约束**
   ```java
   // 工具类不需要继承
   public final class Collections {
       private Collections() {}  // 私有构造器
   }
   ```

#### 典型应用场景

```java
// 场景 1：配置常量类
@Configuration
public class AppConfig {
    public static final String DATABASE_URL = "jdbc:mysql://localhost:3306/db";
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int MAX_POOL_SIZE = 20;
}

// 场景 2：不可变类
@Immutable
public final class Person {
    private final String name;
    private final int age;
    private final List<String> hobbies;
    
    public Person(String name, int age, List<String> hobbies) {
        this.name = name;
        this.age = age;
        // 防御性拷贝，防止外部修改
        this.hobbies = new ArrayList<>(hobbies);
    }
    
    // 返回防御性拷贝
    public List<String> getHobbies() {
        return new ArrayList<>(hobbies);
    }
}

// 场景 3：Builder 模式
public class User {
    private final Long id;
    private final String username;
    private final String email;
    
    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String username;
        private String email;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public User build() {
            return new User(this);
        }
    }
}
```

### 2.2 finally - 必执行的代码块

#### 核心特性

```java
// 1. 基本的 try-catch-finally
public void readFile(String path) {
    BufferedReader reader = null;
    try {
        reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        System.out.println(line);
    } catch (IOException e) {
        log.error("读取文件失败", e);
    } finally {
        // 无论如何都会执行，用于关闭资源
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("关闭流失败", e);
            }
        }
    }
}

// 2. try-finally（不捕获异常）
public void closeResource(AutoCloseable resource) throws Exception {
    try {
        // 使用资源
        resource.close();
    } finally {
        // 即使 close() 抛异常，也会执行这里
        System.out.println("清理完成");
    }
}

// 3. finally 中有 return（不推荐！）
public int testReturn() {
    try {
        int result = 10 / 0;  // 抛异常
        return 1;  // 不会执行
    } catch (ArithmeticException e) {
        return 2;  // 准备返回 2
    } finally {
        return 3;  // ❌ 覆盖 catch 中的 return，最终返回 3
    }
}

// 4. finally 中抛异常（危险！）
public void testFinallyException() {
    try {
        throw new RuntimeException("原始异常");
    } finally {
        throw new RuntimeException("finally 中的异常");  // ❌ 覆盖原始异常
    }
}
// 结果：只看到"finally 中的异常"，丢失了原始异常信息

// 5. try 中有 return，finally 先执行
public int testReturnOrder() {
    int result = 1;
    try {
        result = 2;
        return result;  // 准备返回 2
    } finally {
        result = 3;  // 修改 result，但不影响返回值（已缓存）
        System.out.println("finally 执行");
    }
    // 实际返回 2，不是 3
}
```

#### finally 的执行时机

```java
public class FinallyExecutionTest {
    
    // 情况 1：正常执行
    public void normalCase() {
        try {
            System.out.println("try 执行");
        } finally {
            System.out.println("finally 执行");
        }
    }
    // 输出：try 执行 → finally 执行
    
    // 情况 2：try 中抛异常
    public void exceptionCase() {
        try {
            System.out.println("try 执行");
            throw new RuntimeException("异常");
        } finally {
            System.out.println("finally 执行");
        }
    }
    // 输出：try 执行 → finally 执行 → 异常抛出
    
    // 情况 3：try 中有 return
    public int returnCase() {
        try {
            System.out.println("try 执行");
            return 1;
        } finally {
            System.out.println("finally 执行");
        }
    }
    // 输出：try 执行 → finally 执行 → 返回 1
    
    // 情况 4：System.exit()
    public void exitCase() {
        try {
            System.out.println("try 执行");
            System.exit(0);  // ❌ 直接退出 JVM，finally 不执行
        } finally {
            System.out.println("finally 执行");  // 不会输出
        }
    }
}
```

#### finally 不执行的情况

1. **System.exit(0)** - 直接退出 JVM
2. **线程死亡** - Thread.stop()（已废弃）
3. **CPU 强制关闭** - kill -9 PID
4. **无限循环** - while(true) { }
5. **JVM 崩溃** - 段错误等

#### JDK7+ 的现代替代方案

```java
// 传统方式（啰嗦、易错）
public void readFileTraditional(String path) {
    BufferedReader reader = null;
    try {
        reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        System.out.println(line);
    } catch (IOException e) {
        log.error("读取文件失败", e);
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("关闭流失败", e);
            }
        }
    }
}

// JDK7+ try-with-resources（简洁、安全）
public void readFileModern(String path) {
    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
        String line = reader.readLine();
        System.out.println(line);
    } catch (IOException e) {
        log.error("读取文件失败", e);
    }
    // 自动关闭资源，无需 finally
}

// 支持多个资源
public void copyFile(String src, String dst) {
    try (InputStream is = new FileInputStream(src);
         OutputStream os = new FileOutputStream(dst)) {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
    } catch (IOException e) {
        log.error("复制文件失败", e);
    }
}
```

#### 典型应用场景

```java
// 场景 1：数据库连接管理
public void executeQuery(Connection conn, String sql) {
    Statement stmt = null;
    ResultSet rs = null;
    try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
    } catch (SQLException e) {
        log.error("查询失败", e);
    } finally {
        // 关闭资源（从里到外）
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { }
        }
    }
}

// 场景 2：锁的释放
public void synchronizedOperation(ReentrantLock lock) {
    lock.lock();
    try {
        // 临界区操作
        doSomething();
    } finally {
        lock.unlock();  // 必须释放锁
    }
}

// 场景 3：切换 ClassLoader
public void loadPlugin(String pluginPath) {
    ClassLoader original = Thread.currentThread().getContextClassLoader();
    try {
        ClassLoader pluginLoader = new URLClassLoader(new URL[]{new File(pluginPath).toURI().toURL()});
        Thread.currentThread().setContextClassLoader(pluginLoader);
        // 加载插件
        Class<?> clazz = pluginLoader.loadClass("com.example.Plugin");
        Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
        plugin.init();
    } finally {
        // 恢复原始 ClassLoader
        Thread.currentThread().setContextClassLoader(original);
    }
}
```

### 2.3 finalize - 已废弃的 GC 回调

#### 核心特性

```java
// 1. finalize() 的传统用法（已过时）
public class LegacyResource {
    private String resourceName;
    
    public LegacyResource(String name) {
        this.resourceName = name;
        System.out.println("创建资源：" + name);
    }
    
    @Override
    @Deprecated  // JDK9+ 标记为废弃
    protected void finalize() throws Throwable {
        try {
            // 清理资源（不推荐）
            System.out.println("清理资源：" + resourceName);
            resourceName = null;
        } finally {
            super.finalize();  // 必须调用父类方法
        }
    }
}

// 2. finalize() 的问题演示
public class FinalizeProblem {
    
    // 问题 1：调用时机不确定
    public void testTiming() {
        LegacyResource resource = new LegacyResource("test");
        resource = null;  // 等待 GC
        
        // ❌ 不知道什么时候执行 finalize()
        // 可能几毫秒后，也可能几分钟后，甚至永远不执行
        System.gc();  // 建议 GC，但不保证立即执行
    }
    
    // 问题 2：可能被调用多次
    @Override
    @Deprecated
    protected void finalize() throws Throwable {
        System.out.println("finalize 第一次调用");
        // 如果对象"复活"，下次 GC 时还会调用
        super.finalize();
    }
    
    // 问题 3：对象"复活"（不推荐）
    public class Resurrection {
        private static Resurrection instance;
        
        @Override
        @Deprecated
        protected void finalize() throws Throwable {
            // 让对象复活（危险操作）
            instance = this;
            super.finalize();
        }
    }
}

// 3. 现代替代方案：AutoCloseable
public class ModernResource implements AutoCloseable {
    private String resourceName;
    
    public ModernResource(String name) {
        this.resourceName = name;
        System.out.println("创建资源：" + name);
    }
    
    @Override
    public void close() {
        // 确定性清理（推荐）
        System.out.println("清理资源：" + resourceName);
        resourceName = null;
    }
}

// 使用 try-with-resources
public void useResource() {
    try (ModernResource resource = new ModernResource("test")) {
        // 使用资源
    }  // 自动调用 close()，确定性强
}
```

#### finalize() 的性能问题

```java
public class FinalizePerformance {
    
    // 有 finalize() 方法的对象性能差
    public static class WithFinalize {
        @Override
        @Deprecated
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }
    
    // 无 finalize() 方法的对象性能好
    public static class WithoutFinalize {
        // 普通对象
    }
    
    public static void main(String[] args) {
        // 性能测试
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < 100000; i++) {
            new WithFinalize();  // 慢（需要特殊处理）
        }
        
        System.out.println("WithFinalize: " + (System.currentTimeMillis() - start) + "ms");
        
        start = System.currentTimeMillis();
        
        for (int i = 0; i < 100000; i++) {
            new WithoutFinalize();  // 快
        }
        
        System.out.println("WithoutFinalize: " + (System.currentTimeMillis() - start) + "ms");
        
        // 结果：WithFinalize 慢很多（可能 10 倍以上）
    }
}
```

**性能差的根本原因**：
1. 带 finalize() 的对象需要特殊处理
2. GC 时需要额外步骤（调用 finalize）
3. 对象生命周期延长（至少多活一个 GC 周期）

#### JDK9+ 的替代方案

```java
// 方案 1：java.lang.ref.Cleaner（JDK9+）
public class CleanerResource implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private String resourceName;
    
    public CleanerResource(String name) {
        this.resourceName = name;
        // 注册清理动作
        this.cleanable = cleaner.register(this, () -> {
            System.out.println("清理资源：" + resourceName);
            resourceName = null;
        });
    }
    
    @Override
    public void close() {
        // 显式清理
        cleanable.clean();
    }
}

// 方案 2：PhantomReference（虚引用）
public class PhantomResource {
    private final PhantomReference<PhantomResource> phantomRef;
    private final ReferenceQueue<PhantomResource> queue;
    
    public PhantomResource() {
        this.queue = new ReferenceQueue<>();
        this.phantomRef = new PhantomReference<>(this, queue);
        
        // 启动清理线程
        new Thread(() -> {
            while (true) {
                try {
                    // 等待对象被 GC
                    Reference<? extends PhantomResource> ref = queue.remove();
                    // 执行清理逻辑
                    cleanup();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    private void cleanup() {
        System.out.println("清理资源");
    }
}
```

#### 为什么废弃 finalize()？

1. **不确定性**：不知道何时执行
2. **性能差**：延长对象生命周期
3. **不可靠**：可能永远不执行
4. **复杂性**：容易导致死锁等问题
5. **有更好的替代方案**：AutoCloseable、Cleaner

## 三、对比表格

### 3.1 完整对比表

| 对比维度 | final | finally | finalize |
|----------|-------|---------|----------|
| **类型** | 修饰符 | 异常处理关键字 | Object 方法 |
| **所属** | 语言特性 | try-catch 结构 | java.lang.Object |
| **作用** | 不可变性 | 必执行代码 | GC 前清理 |
| **使用时机** | 编译期 | 运行时 | GC 时（不确定） |
| **是否可靠** | ✓ 完全可靠 | ✓ 几乎总是执行 | ✗ 不可靠 |
| **性能影响** | 无/微小 | 微小 | 大 |
| **JDK 状态** | 正常使用 | 正常使用 | JDK9+ @Deprecated |
| **推荐使用** | ✓ 推荐 | ✓ 按需使用 | ✗ 不推荐 |

### 3.2 记忆口诀

```
final 修饰不可变，类法变量都能管
finally 配 try 用，资源清理它最稳
finalize 已废弃，垃圾回收前清理
三者读音虽相似，用途差别要牢记
```

### 3.3 使用建议

| 场景 | 选择 | 理由 |
|------|------|------|
| 定义常量 | final | 标准做法，语义清晰 |
| 工具类 | final 类 | 防止继承 |
| 不希望重写的方法 | final 方法 | 明确设计意图 |
| 资源清理 | finally 或 try-with-resources | 确保执行 |
| 关闭 IO 流 | try-with-resources | 简洁安全 |
| 对象销毁前清理 | Cleaner/AutoCloseable | 不要用 finalize |

## 四、面试高频考点

### 4.1 经典问题与答案

#### Q1: final、finally、finalize 的核心区别？

**答**：

1. **final**（修饰符）：
   - 修饰类：不能被继承
   - 修饰方法：不能被重写
   - 修饰变量：值/引用不能修改

2. **finally**（异常处理）：
   - 配合 try-catch 使用
   - 块中代码几乎总是执行
   - 用于资源清理

3. **finalize**（GC 回调）：
   - Object 类的方法
   - GC 回收前调用
   - JDK9+ 已废弃

#### Q2: final 修饰的变量真的不能修改吗？

**答**：分情况。

**基本类型**：值不能修改
```java
final int x = 100;
// x = 200;  // ❌ 错误
```

**引用类型**：引用地址不能修改，对象内容可改
```java
final List<String> list = new ArrayList<>();
// list = new LinkedList<>();  // ❌ 错误：引用不能改
list.add("item");  // ✓ 正确：内容可改
```

**数组**：数组引用不能改，元素可改
```java
final int[] arr = {1, 2, 3};
// arr = new int[]{4, 5};  // ❌ 错误
arr[0] = 100;  // ✓ 正确
```

#### Q3: finally 一定会执行吗？

**答**：几乎总是，但有例外。

**会执行的情况**：
- try-catch 正常结束
- try 中抛异常
- try 中有 return
- catch 中有 return

**不执行的情况**：
1. `System.exit(0)` - 退出 JVM
2. 线程死亡（Thread.stop()）
3. CPU 强制关闭（kill -9）
4. 无限循环
5. JVM 崩溃

#### Q4: 为什么 finalize() 被废弃？

**答**：

1. **调用时机不确定**
   - 依赖 GC，可能很久才执行
   
2. **性能差**
   - 延长对象生命周期
   - 增加 GC 负担

3. **不可靠**
   - 可能永远不执行
   - 可能被调用多次

4. **有更好的替代方案**
   - AutoCloseable + try-with-resources
   - java.lang.ref.Cleaner（JDK9+）
   - PhantomReference（虚引用）

#### Q5: finally 中有 return 会怎样？

**答**：会覆盖 try/catch 中的 return。

```java
public int test() {
    try {
        return 1;
    } finally {
        return 2;  // 最终返回 2
    }
}
```

**强烈不推荐**在 finally 中使用 return，因为：
1. 覆盖原始返回值
2. 可能导致异常丢失
3. 代码难以理解

#### Q6: 如何正确清理资源？

**答**：

**JDK7+ 推荐**：try-with-resources
```java
try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
    String line = reader.readLine();
} catch (IOException e) {
    log.error("读取失败", e);
}
// 自动关闭
```

**传统方式**：try-catch-finally
```java
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader(path));
    // 使用资源
} finally {
    if (reader != null) {
        reader.close();
    }
}
```

**绝对不要**：依赖 finalize()
```java
@Deprecated
protected void finalize() {
    // ❌ 不推荐
}
```

### 4.2 实战应用场景

#### 场景 1: 如何设计不可变类？

**答**：使用 final。

```java
@Immutable
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    // 返回新对象，不修改自身
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

#### 场景 2: 如何处理外部资源？

**答**：使用 try-with-resources。

```java
@Service
public class FileService {
    
    public String readFile(String path) {
        try (InputStream is = new FileInputStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
            
        } catch (IOException e) {
            throw new BusinessException("读取文件失败", e);
        }
    }
    
    public void processConnection(Socket socket) {
        try (socket;
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            String request = reader.readLine();
            String response = handle(request);
            writer.println(response);
            
        } catch (IOException e) {
            log.error("处理连接失败", e);
        }
    }
}
```

## 五、实验演示说明

### 5.1 启动服务

```bash
# 在项目根目录执行
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 5.2 final 演示

```bash
curl -X POST "http://localhost:9510/interview-agent/finalkeyword/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"keywordType":"FINAL"}'
```

### 5.3 finally 演示

```bash
curl -X POST "http://localhost:9510/interview-agent/finalkeyword/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"keywordType":"FINALLY"}'
```

### 5.4 finalize 演示

```bash
curl -X POST "http://localhost:9510/interview-agent/finalkeyword/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"keywordType":"FINALIZE"}'
```

## 六、常见误区

### 6.1 关于 final

❌ **误区**：final 修饰的 List 不能添加元素

✅ **正解**：final 保证引用不变，List 内容可变
```java
final List<String> list = new ArrayList<>();
list.add("item");  // ✓ 正确
// list = new LinkedList<>();  // ❌ 错误
```

❌ **误区**：final 方法性能更好

✅ **正解**：现代 JVM 自动优化，final 影响不大

### 6.2 关于 finally

❌ **误区**：finally 一定执行

✅ **正解**：System.exit(0) 等极端情况不执行

❌ **误区**：finally 中可以 return

✅ **正解**：会覆盖 try/catch 的返回值，不推荐

### 6.3 关于 finalize

❌ **误区**：finalize() 可以清理资源

✅ **正解**：不可靠，应该用 AutoCloseable

❌ **误区**：finalize() 只会被调用一次

✅ **正解**：可能多次（如果对象"复活"）

## 七、最佳实践建议

### 7.1 final 使用规范

```java
// ✓ 推荐：定义常量
public static final int MAX_SIZE = 100;

// ✓ 推荐：工具类用 final
public final class StringUtils { }

// ✓ 推荐：不希望重写的方法用 final
public final void templateMethod() { }

// ⚠️ 谨慎：final 引用类型（可能误导）
private final List<String> list = new ArrayList<>();
// 最好用不可变集合
private final List<String> list = Collections.unmodifiableList(new ArrayList<>());
```

### 7.2 finally 使用规范

```java
// ✓ 推荐：JDK7+ 优先用 try-with-resources
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // 使用资源
}

// ✓ 允许：传统 try-catch-finally
Connection conn = null;
try {
    conn = dataSource.getConnection();
    // 使用资源
} finally {
    if (conn != null) conn.close();
}

// ❌ 避免：finally 中 return
finally {
    return defaultValue;  // 不推荐
}

// ❌ 避免：finally 中复杂逻辑
finally {
    // 大量业务逻辑（应该保持简单）
}
```

### 7.3 finalize 替代方案

```java
// ✓ 推荐：实现 AutoCloseable
public class DatabaseConnection implements AutoCloseable {
    @Override
    public void close() {
        // 清理逻辑
    }
}

// ✓ 推荐：JDK9+ 使用 Cleaner
public class Resource implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    private final Cleanable cleanable;
    
    public Resource() {
        this.cleanable = cleaner.register(this, this::cleanup);
    }
    
    private void cleanup() {
        // 清理逻辑
    }
    
    @Override
    public void close() {
        cleanable.clean();
    }
}

// ❌ 避免：使用 finalize
@Deprecated
@Override
protected void finalize() { }
```

## 八、总结

### 8.1 一句话记忆

- **final**：修饰符，表不可变
- **finally**：异常处理，必执行
- **finalize**：GC 回调，已废弃

### 8.2 核心要点

1. final 用于定义常量、不可变类、不可重写方法
2. finally 用于资源清理，几乎总是执行
3. finalize 不可靠且性能差，JDK9+ 已废弃
4. JDK7+ 优先使用 try-with-resources
5. final 不保证线程安全（只是引用不变）

### 8.3 延伸学习

- Java 内存模型（JMM）
- 不可变类设计
- 异常处理最佳实践
- 垃圾回收机制
- Java 9+ 新特性

---

**对应面试题**：问题 007 - final、finally、finalize 的区别

**关键词**：final,finally,finalize，常量，异常，垃圾回收

**难度等级**：⭐⭐⭐⭐（高频考点，必须掌握）
