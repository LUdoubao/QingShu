# 问题 006: 接口和抽象类的区别 - 总结文档

## 一、核心知识点回顾

### 1.1 接口和抽象类的本质区别

| 特性 | 抽象类 (Abstract Class) | 接口 (Interface) |
|------|------------------------|------------------|
| **继承方式** | 单继承 | 多实现 |
| **方法类型** | 抽象方法 + 普通方法 | 抽象方法 + 默认方法 + 静态方法（JDK8+） |
| **构造器** | ✓ 可以有 | ✗ 不能有 |
| **变量修饰符** | 任意（private/protected/public） | 默认 public static final |
| **设计理念** | is-a（本质相同） | like-a（能力/行为） |
| **代码复用** | 强（提供公共实现） | 弱（通过默认方法） |
| **JDK 版本要求** | 无特殊要求 | JDK 8+ 支持默认方法 |
| **访问修饰符** | 任意 | 默认 public |

### 1.2 形象比喻

- **抽象类** 像"家族遗传"：子女继承父母的特征（is-a 关系），如 Dog is-a Animal
- **接口** 像"技能证书"：一个人可以拥有多个技能（like-a 关系），如 Bird can-fly Flyable

## 二、详细作用说明

### 2.1 抽象类 - 代码复用和类层次

#### 核心特性

```java
// 1. 定义抽象类
public abstract class Animal {
    // 受保护的变量
    protected String name;
    
    // 私有变量
    private Integer age;
    
    // 构造器（供子类调用）
    public Animal(String name) {
        this.name = name;
    }
    
    // 抽象方法（无实现）
    public abstract void makeSound();
    
    // 普通方法（有实现，子类可继承）
    public void sleep() {
        System.out.println(name + "正在睡觉");
    }
    
    // final 方法（不可重写）
    public final void breathe() {
        System.out.println(name + "正在呼吸");
    }
}

// 2. 子类继承抽象类
public class Dog extends Animal {
    public Dog(String name) {
        super(name);  // 调用父类构造器
    }
    
    @Override
    public void makeSound() {
        System.out.println(name + "汪汪汪");
    }
    
    // 可以重写普通方法
    @Override
    public void sleep() {
        System.out.println(name + "趴着睡觉");
    }
}
```

#### 为什么使用抽象类？

1. **代码复用**
   - 提供公共实现给子类
   - 减少重复代码

2. **建立类层次**
   - 体现 is-a 关系
   - 符合现实世界认知

3. **部分抽象**
   - 有些方法已实现
   - 有些方法由子类实现

4. **控制访问**
   - 可以使用 protected/private
   - 封装内部细节

#### 典型应用场景

```java
// 模板方法模式
public abstract class DataProcessor {
    // 模板方法（final，子类不能重写）
    public final void process() {
        loadData();
        processData();  // 抽象方法，子类实现
        saveResult();
    }
    
    // 具体方法
    protected void loadData() {
        System.out.println("加载数据");
    }
    
    // 抽象方法
    protected abstract void processData();
    
    // 钩子方法（可选重写）
    protected void afterProcess() {
        // 默认空实现
    }
}

// 具体实现
public class CSVProcessor extends DataProcessor {
    @Override
    protected void processData() {
        System.out.println("处理 CSV 数据");
    }
}
```

### 2.2 接口 - 行为规范和多继承

#### 核心特性

```java
// 1. 定义接口（JDK 8+）
public interface Flyable {
    // 常量（public static final 可省略）
    int MAX_HEIGHT = 1000;
    double GRAVITY = 9.8;
    
    // 抽象方法（public abstract 可省略）
    void fly();
    
    // 默认方法（JDK 8+，提供默认实现）
    default void land() {
        System.out.println("正在降落");
    }
    
    // 静态方法（JDK 8+）
    static void info() {
        System.out.println("这是飞行接口");
    }
    
    // 私有方法（JDK 9+，辅助默认方法）
    private void log(String msg) {
        System.out.println("日志：" + msg);
    }
}

// 2. 实现接口
public class Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("鸟儿在飞翔");
    }
    
    // 可以重写默认方法
    @Override
    public void land() {
        System.out.println("鸟儿降落在树枝上");
    }
}

// 3. 多实现
public class FlyingFish implements Flyable, Swimmable {
    @Override
    public void fly() {
        System.out.println("飞鱼在滑翔");
    }
    
    @Override
    public void swim() {
        System.out.println("飞鱼在游泳");
    }
}
```

#### 接口的演进

**JDK 7 及之前**：
```java
// 完全抽象
public interface Runnable {
    void run();  // 只有抽象方法
}
```

**JDK 8**：
```java
// 增加默认方法和静态方法
public interface Collection<E> {
    int size();
    
    // 默认方法
    default boolean isEmpty() {
        return size() == 0;
    }
    
    // 静态方法
    static <T> List<T> of(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }
}
```

**JDK 9**：
```java
// 增加私有方法
public interface Stream<T> {
    // 默认方法
    default Stream<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return doFilter(predicate);  // 调用私有方法
    }
    
    // 私有方法
    private Stream<T> doFilter(Predicate<? super T> predicate) {
        // 实现细节
    }
}
```

#### 典型应用场景

```java
// 1. 策略模式
public interface PaymentStrategy {
    void pay(BigDecimal amount);
}

@Service("alipay")
public class AlipayService implements PaymentStrategy {
    @Override
    public void pay(BigDecimal amount) {
        // 支付宝支付
    }
}

@Service("wechatpay")
public class WechatPayService implements PaymentStrategy {
    @Override
    public void pay(BigDecimal amount) {
        // 微信支付
    }
}

// 2. 回调函数
public interface Callback {
    void onSuccess(String result);
    void onError(Exception e);
}

public void asyncTask(Callback callback) {
    new Thread(() -> {
        try {
            // 执行任务
            callback.onSuccess("成功");
        } catch (Exception e) {
            callback.onError(e);
        }
    }).start();
}

// 3. 多重继承效果
public class SmartWatch implements TimeDisplay, NotificationReceiver, HealthTracker {
    // 实现多个接口的能力
}
```

## 三、对比表格

### 3.1 完整对比表

| 对比维度 | 抽象类 | 接口 |
|----------|--------|------|
| **本质** | 不完全的类 | 完全抽象的类型 |
| **继承关键字** | extends | implements |
| **继承数量** | 单继承 | 多实现 |
| **构造器** | ✓ 可以有 | ✗ 不能有 |
| **静态方法** | ✓ 可以有 | ✓ 可以有（JDK8+） |
| **实例变量** | ✓ 任意修饰符 | ✗ 只能常量 |
| **访问修饰符** | 任意 | 默认 public |
| **设计目的** | 代码复用 | 行为规范 |
| **关系类型** | is-a | like-a/can-do |
| **速度** | 较快 | 较慢（查找开销） |
| **添加新方法** | 可提供默认实现 | 必须修改所有实现类（除非 default） |

### 3.2 如何选择

| 场景 | 选择 | 原因 |
|------|------|------|
| 需要代码复用 | 抽象类 | 提供公共实现 |
| 需要多继承 | 接口 | 支持多实现 |
| 表示 is-a 关系 | 抽象类 | 本质相同 |
| 表示能力/行为 | 接口 | 附加功能 |
| 需要非 public 成员 | 抽象类 | 接口只能 public |
| 需要构造器 | 抽象类 | 接口不能有 |
| 解耦组件 | 接口 | 面向接口编程 |
| 定义契约 | 接口 | 纯抽象规范 |

### 3.3 记忆口诀

```
抽象类，单继承，代码复用它最强
is-a 关系表本质，构造方法里面藏

接口们，多实现，行为规范它来定
like-a 关系表能力，默认方法来帮忙

若要代码能复用，抽象类里找答案
若要解耦和扩展，接口堆里寻良方
```

## 四、面试高频考点

### 4.1 经典问题与答案

#### Q1: 接口和抽象类的核心区别？

**答**：

1. **继承方式**：
   - 抽象类：单继承（extends）
   - 接口：多实现（implements）

2. **方法类型**：
   - 抽象类：抽象方法 + 普通方法
   - 接口：抽象方法 + 默认方法 + 静态方法（JDK8+）

3. **变量限制**：
   - 抽象类：任意访问修饰符的变量
   - 接口：默认 public static final 常量

4. **构造器**：
   - 抽象类：可以有构造器
   - 接口：不能有构造器

5. **设计理念**：
   - 抽象类：is-a 关系（本质相同）
   - 接口：like-a 关系（能力/行为）

#### Q2: 什么时候用抽象类，什么时候用接口？

**答**：

**使用抽象类的场景**：
1. 需要代码复用，提供公共实现
2. 多个相关类有共同行为和属性
3. 体现 is-a 关系
4. 需要非 public 成员
5. 需要构造器

**使用接口的场景**：
1. 定义行为规范或契约
2. 需要多实现
3. 表示能力或行为
4. 解耦组件依赖
5. 回调函数和事件监听

**简单判断**：
- 问自己：是"is-a"还是"can-do"？
- is-a → 抽象类
- can-do → 接口

#### Q3: JDK 8 对接口的改进有什么意义？

**答**：

**改进内容**：
1. **默认方法**：接口可以提供默认实现
2. **静态方法**：接口可以有静态工具方法

**意义**：
1. **向后兼容**：添加新方法不破坏已有实现
   ```java
   // JDK 8 之前的困境
   public interface List<E> {
       int size();
       // 想添加新方法？所有实现类都要修改！
   }
   
   // JDK 8 的解决方案
   public interface List<E> {
       int size();
       
       // 默认方法，不影响已有实现
       default void sort(Comparator<? super E> c) {
           Collections.sort(this, c);
       }
   }
   ```

2. **增强功能**：接口可以提供实用方法
   ```java
   public interface Stream<T> {
       // 丰富的默认方法
       default Optional<T> max(Comparator<? super T> comp) { ... }
       default long count() { ... }
   }
   ```

3. **简化代码**：减少工具类的使用
   ```java
   // 之前
   Collections.sort(list);
   
   // 现在
   list.sort(Comparator.naturalOrder());
   ```

#### Q4: 一个类可以同时继承抽象类和实现接口吗？

**答**：可以。

```java
public abstract class Animal {
    protected String name;
    public abstract void makeSound();
}

public interface Flyable {
    void fly();
}

public interface Swimmable {
    void swim();
}

// 合法：单继承 + 多实现
public class Duck extends Animal implements Flyable, Swimmable {
    @Override
    public void makeSound() {
        System.out.println("嘎嘎嘎");
    }
    
    @Override
    public void fly() {
        System.out.println("鸭子在飞");
    }
    
    @Override
    public void swim() {
        System.out.println("鸭子在游泳");
    }
}
```

#### Q5: 接口中的默认方法可以被重写吗？

**答**：可以。

```java
public interface Flyable {
    default void land() {
        System.out.println("正在降落");
    }
}

public class Bird implements Flyable {
    // 重写默认方法
    @Override
    public void land() {
        System.out.println("鸟儿降落在树枝上");
    }
}
```

**冲突解决**（多实现时）：
```java
public interface A {
    default void show() {
        System.out.println("A");
    }
}

public interface B {
    default void show() {
        System.out.println("B");
    }
}

// 编译错误：冲突的默认方法
public class C implements A, B {
    // 必须重写解决冲突
    @Override
    public void show() {
        // 选择 A 的实现
        A.super.show();
        // 或选择 B 的实现
        // B.super.show();
        // 或全新实现
        System.out.println("C");
    }
}
```

#### Q6: 抽象类可以有构造器吗？有什么作用？

**答**：可以有。

**作用**：
1. **供子类调用**
   ```java
   public abstract class Animal {
       protected String name;
       
       // 构造器
       public Animal(String name) {
           this.name = name;
       }
   }
   
   public class Dog extends Animal {
       public Dog(String name) {
           super(name);  // 调用父类构造器
       }
   }
   ```

2. **初始化公共属性**
   ```java
   public abstract class DataAccessObject {
       protected Connection conn;
       
       // 构造器中初始化连接
       public DataAccessObject() {
           this.conn = DriverManager.getConnection(...);
       }
   }
   ```

3. **执行通用逻辑**
   ```java
   public abstract class Template {
       public Template() {
           System.out.println("构造器中执行通用逻辑");
           register();  // 可以调用抽象方法
       }
       
       protected abstract void register();
   }
   ```

### 4.2 实战应用场景

#### 场景 1: 如何设计一个灵活的支付系统？

**答**：结合使用抽象类和接口。

```java
// 接口定义行为规范
public interface PaymentService {
    void pay(BigDecimal amount);
    RefundResult refund(String orderId);
}

// 抽象类提供公共实现
public abstract class AbstractPaymentService implements PaymentService {
    
    // 公共属性
    protected String merchantId;
    protected String apiKey;
    
    // 构造器初始化
    public AbstractPaymentService(String merchantId, String apiKey) {
        this.merchantId = merchantId;
        this.apiKey = apiKey;
    }
    
    // 公共方法：验证参数
    protected void validate(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额无效");
        }
    }
    
    // 公共方法：记录日志
    protected void logPayment(String orderId, BigDecimal amount) {
        System.out.println("支付：" + orderId + ", 金额：" + amount);
    }
    
    // 模板方法
    @Override
    public void pay(BigDecimal amount) {
        validate(amount);
        doPay(amount);  // 抽象方法，子类实现
    }
    
    // 抽象方法
    protected abstract void doPay(BigDecimal amount);
}

// 具体实现
@Service("alipay")
public class AlipayService extends AbstractPaymentService {
    public AlipayService() {
        super("ALI_MERCHANT", "ALI_KEY");
    }
    
    @Override
    protected void doPay(BigDecimal amount) {
        System.out.println("支付宝支付：" + amount);
    }
}
```

#### 场景 2: 如何利用接口实现解耦？

**答**：面向接口编程。

```java
// 接口定义（API）
public interface UserService {
    User findById(Long id);
    void save(User user);
}

// 实现类（Impl）
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User findById(Long id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public void save(User user) {
        userMapper.insert(user);
    }
}

// Controller 依赖接口而非实现
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;  // 依赖接口
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

**好处**：
1. 易于替换实现
2. 便于单元测试（Mock）
3. 降低模块间耦合

## 五、实验演示说明

### 5.1 启动服务

```bash
# 在项目根目录执行
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 5.2 抽象类演示

```bash
curl -X POST "http://localhost:9510/interview-agent/abstractinterface/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"targetType":"ABSTRACT_CLASS"}'
```

**观察点**：
- 抽象类的特性说明
- 代码示例展示

### 5.3 接口演示

```bash
curl -X POST "http://localhost:9510/interview-agent/abstractinterface/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"targetType":"INTERFACE"}'
```

**观察点**：
- 接口的特性说明
- JDK8+ 的默认方法和静态方法

## 六、常见误区

### 6.1 关于抽象类

❌ **误区**：抽象类必须有抽象方法

✅ **正解**：可以没有抽象方法（如模板方法模式的基类）

❌ **误区**：抽象类不能被实例化所以没用

✅ **正解**：通过子类间接使用，提供代码复用

### 6.2 关于接口

❌ **误区**：接口只能是完全抽象的

✅ **正解**：JDK8+ 可以有默认方法和静态方法

❌ **误区**：接口中的方法必须是 public

✅ **正解**：JDK9+ 可以有私有方法

### 6.3 关于选择

❌ **误区**：接口比抽象类更抽象

✅ **正解**：看场景，没有绝对的优劣

❌ **误区**：优先使用接口

✅ **正解**：根据设计需求选择

## 七、最佳实践建议

### 7.1 设计原则

1. **面向接口编程**
   - 依赖倒置原则
   - 降低耦合度

2. **合理使用抽象类**
   - 代码复用
   - 模板方法模式

3. **接口隔离原则**
   - 接口要小而精
   - 不要大而全

### 7.2 代码规范

```java
// 推荐：接口命名用形容词或动词
public interface Comparable { ... }
public interface Runnable { ... }

// 推荐：抽象类命名用名词
public abstract class Animal { ... }
public abstract class Shape { ... }

// 推荐：接口避免暴露实现细节
public interface Repository {
    User findById(Long id);
    // 不要暴露 SQL 细节
}
```

## 八、总结

### 8.1 一句话记忆

- **抽象类**：单继承，代码复用，is-a
- **接口**：多实现，行为规范，like-a

### 8.2 核心要点

1. 抽象类是单继承，接口是多实现
2. 抽象类提供代码复用，接口定义行为规范
3. 抽象类体现 is-a 关系，接口体现 like-a 关系
4. JDK8+ 接口可以有默认方法和静态方法
5. 根据设计需求选择合适的类型

### 8.3 延伸学习

- SOLID 设计原则
- 设计模式（策略、模板方法、观察者等）
- 面向接口编程
- Java 8+ Stream API 中的接口设计

---

**对应面试题**：问题 006 - 接口和抽象类的区别

**关键词**：接口，抽象类，继承，实现，常量，默认方法

**难度等级**：⭐⭐⭐⭐（高频考点，必须掌握）
