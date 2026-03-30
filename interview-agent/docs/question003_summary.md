# 问题 003: 方法重载和方法重写的区别 - 总结文档

## 一、核心知识点回顾

### 1.1 方法重载和方法重写的本质区别

| 特性 | 方法重载 (Overload) | 方法重写 (Override) |
|------|---------------------|---------------------|
| **发生范围** | 同一个类中 | 父子类之间 |
| **方法名** | 必须相同 | 必须相同 |
| **参数列表** | 必须不同（个数、类型、顺序） | 必须相同 |
| **返回类型** | 可以不同，但不能仅通过返回值区分 | 必须相同或是其子类型 |
| **访问修饰符** | 可以不同 | 不能比父类更严格 |
| **异常处理** | 可以抛出任何异常 | 不能抛出比父类更多的检查异常 |
| **绑定方式** | 编译时绑定（静态多态） | 运行时绑定（动态多态） |
| **注解** | 不需要 @Override | 建议使用 @Override |
| **典型应用** | 构造函数重载、工具类方法 | 实现多态、定制子类行为 |

### 1.2 形象比喻

- **方法重载** 像"多功能刀具"：同一把刀（方法名），通过不同的使用方式（参数），实现切菜、剪绳、开瓶等不同功能
- **方法重写** 像"方言发音"：同一个词（方法名），不同地区的人（子类）按照自己的方言（实现）来发音

## 二、详细作用说明

### 2.1 方法重载（Overload）

#### 核心思想

在同一个类中，定义多个方法名相同但参数列表不同的方法，编译器根据调用时传入的参数自动选择合适的方法。

#### 三要素

1. **同一个类中**
   - 重载只能发生在同一个类内部
   - 包括父类和子类不能构成重载关系

2. **方法名相同**
   - 所有重载方法必须使用相同的名称

3. **参数列表不同**
   - 参数个数不同
   - 参数类型不同
   - 参数顺序不同（当类型不同时）

#### 重要规则

```java
public class Calculator {
    // ✓ 正确：参数个数不同
    public int add(int a, int b) { return a + b; }
    public int add(int a, int b, int c) { return a + b + c; }
    
    // ✓ 正确：参数类型不同
    public double add(double a, double b) { return a + b; }
    
    // ✓ 正确：参数顺序不同（类型不同时有效）
    public void process(String name, int value) { }
    public void process(int value, String name) { }
    
    // ✗ 错误：仅返回类型不同，无法区分
    // public String add(int a, int b) { return String.valueOf(a + b); }
}
```

#### 示例场景

```java
// 1. 两个整数相加
Calculator calc = new Calculator();
int result1 = calc.add(10, 20);  // 输出：30

// 2. 三个整数相加
int result2 = calc.add(10, 20, 30);  // 输出：60

// 3. 两个浮点数相加
double result3 = calc.add(10.5, 20.8);  // 输出：31.3

// 4. 字符串拼接
String result4 = calc.add("Hello, ", "World!");  // 输出："Hello, World!"

// 5. 可变参数相加
int result5 = calc.add(1, 2, 3, 4, 5);  // 输出：15
```

#### 注意事项

1. **与返回值无关**
   - 不能仅通过返回类型的不同来区分重载方法
   - 编译器无法判断应该调用哪个方法

2. **构造方法也可以重载**
   ```java
   public class Person {
       private String name;
       private Integer age;
       
       // 无参构造
       public Person() {}
       
       // 带一个参数的构造
       public Person(String name) {
           this.name = name;
       }
       
       // 带两个参数的构造
       public Person(String name, Integer age) {
           this.name = name;
           this.age = age;
       }
   }
   ```

3. **可变参数的重载优先级最低**
   ```java
   // 当调用 add(1, 2) 时，优先匹配 add(int, int)
   // 只有没有精确匹配时，才会使用可变参数版本
   public int add(int... numbers) { ... }
   ```

### 2.2 方法重写（Override）

#### 核心思想

在父子类继承关系中，子类重新定义父类已有的方法，提供适合自身特性的具体实现。

#### 三要素

1. **父子类关系**
   - 必须存在继承关系（extends 或 implements）
   - 同一个类中不存在重写

2. **方法签名完全一致**
   - 方法名相同
   - 参数列表相同
   - 返回类型相同或是其子类型（协变返回类型）

3. **遵循重写规则**
   - 访问权限不能比父类更严格
   - 不能抛出比父类更多的检查异常
   - 使用 @Override 注解确保正确性

#### 重写规则详解

```java
public class Parent {
    public Object getObject() { return new Object(); }
    public void doSomething() throws IOException { }
}

public class Child extends Parent {
    // ✓ 正确：返回类型可以是父类返回类型的子类型（协变返回类型）
    @Override
    public String getObject() { return "Hello"; }
    
    // ✓ 正确：可以抛出更少的异常或不抛异常
    @Override
    public void doSomething() { }
    
    // ✓ 正确：访问权限可以相同或更宽松
    @Override
    public void doSomethingElse() { }
    
    // ✗ 错误：访问权限不能更严格
    // @Override
    // protected void doSomething() { }
    
    // ✗ 错误：不能抛出更多的检查异常
    // @Override
    // public void doSomething() throws SQLException { }
}
```

#### 示例场景

```java
// 1. 创建子类对象
Dog dog = new Dog("旺财", "金毛");
Cat cat = new Cat("咪咪", "白色");

// 2. 调用重写的方法
System.out.println(dog.makeSound());  // 输出：旺财汪汪汪！
System.out.println(cat.makeSound());  // 输出：咪咪喵喵喵~

// 3. 调用重写的移动方法
System.out.println(dog.move());  // 输出：旺财用四条腿奔跑
System.out.println(cat.move());  // 输出：咪咪优雅地踱步

// 4. 多态演示：父类引用指向子类对象
Animal animal = new Dog("小黑", "哈士奇");
System.out.println(animal.makeSound());  // 输出：小黑汪汪汪！
```

#### 注意事项

1. **@Override 注解的作用**
   - 编译器会检查是否真正重写了父类方法
   - 如果方法签名不匹配，编译会报错
   - 提高代码可读性和可维护性

2. **私有方法不能被重写**
   ```java
   public class Parent {
       private void secret() { }
   }
   
   public class Child extends Parent {
       // 这不是重写，这是定义新的方法
       private void secret() { 
           System.out.println("Child's secret");
       }
   }
   ```

3. **静态方法不能被重写**
   ```java
   public class Parent {
       public static void show() { 
           System.out.println("Parent show");
       }
   }
   
   public class Child extends Parent {
       // 这不是重写，这是隐藏（Hiding）
       public static void show() { 
           System.out.println("Child show");
       }
   }
   
   // 调用
   Parent.show();  // 输出：Parent show（编译时绑定）
   Child.show();   // 输出：Child show（编译时绑定）
   ```

4. **final 方法不能被重写**
   ```java
   public class Parent {
       public final void finalMethod() { }
   }
   
   public class Child extends Parent {
       // ✗ 编译错误：不能重写 final 方法
       // @Override
       // public void finalMethod() { }
   }
   ```

## 三、对比表格

### 3.1 完整对比表

| 对比维度 | 方法重载 (Overload) | 方法重写 (Override) |
|----------|---------------------|---------------------|
| **发生位置** | 同一个类中 | 父子类之间 |
| **方法名要求** | 必须相同 | 必须相同 |
| **参数列表要求** | 必须不同（个数、类型、顺序） | 必须相同 |
| **返回类型要求** | 可以不同，但不能仅通过返回值区分 | 必须相同或是其子类型 |
| **访问修饰符** | 可以任意修改 | 不能比父类更严格 |
| **异常声明** | 可以抛出任何异常 | 不能抛出比父类更多的检查异常 |
| **private 方法** | 可以在同类中重载 | 不能被重写 |
| **static 方法** | 可以在同类中重载 | 不能被重写（是隐藏） |
| **final 方法** | 可以在同类中重载 | 不能被重写 |
| **构造方法** | 可以重载 | 不适用（构造方法不能被继承） |
| **绑定时间** | 编译时绑定（早期绑定） | 运行时绑定（晚期绑定） |
| **多态类型** | 静态多态（编译时多态） | 动态多态（运行时多态） |
| **注解要求** | 不需要特殊注解 | 建议使用 @Override |
| **主要目的** | 提供多种参数组合的调用方式 | 改变或扩展父类的行为 |
| **性能差异** | 无额外开销 | 有轻微的动态绑定开销 |

### 3.2 记忆口诀

```
重载：同类同名不同参，编译时候就定案
重写：父子同名同参数，运行时候才确定
```

## 四、面试高频考点

### 4.1 经典问题与答案

#### Q1: 方法重载和重写的区别？

**答**：

1. **发生位置不同**：重载在同一个类中，重写在父子类之间
2. **参数要求不同**：重载要求参数列表不同，重写要求参数列表相同
3. **返回类型不同**：重载可以不同（但不能仅靠返回值区分），重写必须相同或是子类型
4. **绑定时间不同**：重载是编译时绑定，重写是运行时绑定
5. **访问修饰符不同**：重载可以任意，重写不能更严格

#### Q2: 能否根据返回类型区分重载方法？

**答**：不能。

原因：
- Java 编译器无法通过返回值类型来判断应该调用哪个方法
- 调用方法时可以忽略返回值（不赋值给变量）
- 会导致二义性，编译器无法确定

示例：
```java
// 错误示例
public int add(int a, int b) { return a + b; }
public String add(int a, int b) { return String.valueOf(a + b); }

// 调用时
add(1, 2);  // 编译器不知道该调用哪个方法
```

#### Q3: 什么是协变返回类型？

**答**：协变返回类型是指重写方法时，子类方法的返回类型可以是父类方法返回类型的子类型。

示例：
```java
public class Parent {
    public Number getNumber() { return null; }
}

public class Child extends Parent {
    @Override
    public Integer getNumber() { return 1; }  // Integer 是 Number 的子类型
}
```

意义：
- 提供更具体的返回类型
- 减少类型转换的需要
- Java 5 引入的特性

#### Q4: 静态方法能被重写吗？

**答**：不能。

解释：
- 静态方法属于类，不属于实例
- 子类可以定义同名的静态方法，这叫"隐藏"（Hiding）而非"重写"
- 调用时根据引用类型决定，而不是实际对象类型（编译时绑定）

示例：
```java
public class Parent {
    public static void show() {
        System.out.println("Parent show");
    }
}

public class Child extends Parent {
    public static void show() {
        System.out.println("Child show");
    }
}

// 调用
Parent p = new Child();
p.show();  // 输出：Parent show（根据引用类型 Parent 决定）
```

#### Q5: 私有方法能被重写吗？

**答**：不能。

原因：
- 私有方法只在定义它的类内部可见
- 子类无法访问父类的私有方法
- 子类定义同名的私有方法是全新的方法，不是重写

示例：
```java
public class Parent {
    private void display() {
        System.out.println("Parent display");
    }
}

public class Child extends Parent {
    private void display() {
        System.out.println("Child display");
    }
}

// 调用
Parent p = new Child();
p.display();  // 编译错误：display() 在 Parent 中不可见
```

#### Q6: 重载和重写在实际开发中的应用场景？

**答**：

**重载的应用场景**：

1. **构造方法重载**
   ```java
   public User() {}
   public User(String name) { this.name = name; }
   public User(String name, Integer age) { 
       this.name = name; 
       this.age = age; 
   }
   ```

2. **工具类方法**
   ```java
   public class StringUtils {
       public static String join(String... elements) { ... }
       public static String join(String separator, String... elements) { ... }
   }
   ```

3. **Builder 模式**
   ```java
   public Builder append(String value) { ... }
   public Builder append(int value) { ... }
   public Builder append(Object value) { ... }
   ```

**重写的应用场景**：

1. **实现多态**
   ```java
   public interface PaymentService {
       void pay(BigDecimal amount);
   }
   
   @Service("alipay")
   public class AlipayServiceImpl implements PaymentService {
       @Override
       public void pay(BigDecimal amount) {
           // 支付宝支付逻辑
       }
   }
   ```

2. **模板方法模式**
   ```java
   public abstract class DataProcessor {
       public final void process() {
           loadData();
           processData();  // 由子类实现
           saveResult();
       }
       protected abstract void processData();
   }
   ```

3. **覆盖 Object 类的方法**
   ```java
   @Override
   public String toString() {
       return "User{name='" + name + "'}";
   }
   
   @Override
   public boolean equals(Object obj) {
       // 自定义相等逻辑
   }
   ```

### 4.2 实战应用场景

#### 场景 1: 设计一个支持多种数据类型的计算器

**答**：使用方法重载提供多种参数组合。

```java
public class Calculator {
    // 整数加法
    public int add(int a, int b) {
        return a + b;
    }
    
    // 浮点数加法
    public double add(double a, double b) {
        return a + b;
    }
    
    // 大数加法
    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }
    
    // 字符串拼接
    public String add(String a, String b) {
        return a + b;
    }
}
```

#### 场景 2: 如何设计可扩展的动物系统？

**答**：使用抽象类和重写实现多态。

```java
// 抽象基类
public abstract class Animal {
    protected String name;
    
    public abstract String makeSound();
    
    public String move() {
        return name + "正在移动";
    }
}

// 具体实现
public class Dog extends Animal {
    @Override
    public String makeSound() {
        return name + "汪汪汪！";
    }
    
    @Override
    public String move() {
        return name + "用四条腿奔跑";
    }
}

public class Zoo {
    public void letAllAnimalsSpeak(List<Animal> animals) {
        for (Animal animal : animals) {
            // 多态：同样的方法调用，不同的实现
            System.out.println(animal.makeSound());
        }
    }
}
```

#### 场景 3: @Override 注解有什么作用？忘记写会怎样？

**答**：

@Override 注解的作用：

1. **编译检查**
   - 确保方法确实重写了父类方法
   - 如果方法签名有误，编译时报错

2. **代码可读性**
   - 明确标识这是重写的方法
   - 便于其他开发者理解

3. **防止拼写错误**
   ```java
   // 忘记写 @Override
   public String tostring() {  // 应该是 toString()
       return "...";
   }
   // 编译器不会报错，但这不是重写，是新方法
   
   // 写上 @Override
   @Override
   public String tostring() {  // 编译错误！
       return "...";
   }
   ```

建议：**始终使用 @Override 注解**

## 五、实验演示说明

### 5.1 启动服务

```bash
# 在项目根目录执行
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 5.2 演示方法重载

```bash
curl -X POST "http://localhost:9510/interview-agent/method/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"featureType":"OVERLOAD"}'
```

**观察点**：
- 响应中包含 Calculator 类的 5 个重载方法
- 展示了不同参数类型的处理方式
- 体现了编译时绑定的特性

### 5.3 演示方法重写

```bash
curl -X POST "http://localhost:9510/interview-agent/method/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"featureType":"OVERRIDE"}'
```

**观察点**：
- Dog 和 Cat 类重写了 Animal 类的方法
- 使用 @Override 注解确保正确性
- 体现了运行时绑定的多态特性

### 5.4 测试兜底逻辑

```bash
curl -X POST "http://localhost:9510/interview-agent/method/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"featureType":"INVALID_TYPE"}'
```

**观察点**：
- 返回错误响应
- 体现了系统的健壮性

## 六、常见误区

### 6.1 关于重载

❌ **误区**：可以通过返回类型区分重载方法

✅ **正解**：不能仅通过返回类型区分，编译器无法判断

❌ **误区**：重载方法可以有相同的参数列表

✅ **正解**：参数列表必须不同（个数、类型、顺序）

❌ **误区**：可变参数和普通参数不能重载

✅ **正解**：可以重载，但要注意优先级问题

### 6.2 关于重写

❌ **误区**：静态方法可以被重写

✅ **正解**：静态方法是隐藏（Hiding），不是重写

❌ **误区**：重写方法可以抛出更多异常

✅ **正解**：不能抛出比父类更多的检查异常

❌ **误区**：@Override 注解可有可无

✅ **正解**：强烈建议使用，可以提供编译期检查

## 七、最佳实践建议

### 7.1 重载的最佳实践

1. **保持方法语义一致**
   ```java
   // 推荐：所有 add 方法都执行加法操作
   public int add(int a, int b) { ... }
   public double add(double a, double b) { ... }
   
   // 不推荐：重载方法执行完全不同的操作
   public int calculate(int a, int b) { ... }
   public String calculate(String a, String b) { ... }  // 容易混淆
   ```

2. **参数个数递增原则**
   ```java
   // 推荐：参数从少到多
   public void connect(String host) { ... }
   public void connect(String host, int port) { ... }
   public void connect(String host, int port, int timeout) { ... }
   ```

3. **避免过多的重载**
   - 一般不超过 3-4 个重载版本
   - 太多会让使用者困惑

### 7.2 重写的最佳实践

1. **始终使用 @Override 注解**
   ```java
   @Override
   public String toString() {
       return "...";
   }
   ```

2. **遵循里氏替换原则**
   - 子类应该能够替换父类
   - 不要破坏父类的契约

3. **谨慎重写 Object 类的方法**
   ```java
   // equals() 和 hashCode() 要一起重写
   @Override
   public boolean equals(Object obj) { ... }
   
   @Override
   public int hashCode() { ... }
   ```

## 八、总结

### 8.1 一句话记忆

- **重载**："换参数不换名字" - 同类同名不同参
- **重写**："换实现不换签名" - 父子同名同参数

### 8.2 核心要点

1. 重载发生在同一个类中，重写发生在父子类之间
2. 重载看参数列表（必须不同），重写看方法签名（必须相同）
3. 重载是编译时绑定（静态多态），重写是运行时绑定（动态多态）
4. @Override 注解能提供编译期检查，建议始终使用
5. 理解两者的区别是掌握面向对象编程的基础

### 8.3 延伸学习

- 多态的深入理解（静态多态 vs 动态多态）
- 设计模式中的重载和重写应用
- 里氏替换原则（LSP）
- 接口默认方法的重写规则

---

**对应面试题**：问题 003 - 方法重载和方法重写的区别

**关键词**：重载，重写，Overload，Override，父子类，方法签名，编译时绑定，运行时绑定

**难度等级**：⭐⭐⭐（基础必考题，必须掌握）
