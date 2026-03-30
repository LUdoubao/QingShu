# 问题 004: ==和 equals() 的区别 - 总结文档

## 一、核心知识点回顾

### 1.1 ==和 equals() 的本质区别

| 比较维度 | ==操作符 | equals() 方法 |
|----------|----------|---------------|
| **本质** | 运算符（Operator） | Object 类的方法 |
| **基本类型** | ✓ 比较值 | ✗ 不支持 |
| **引用类型** | ✓ 比较内存地址 | ✓ 默认比较地址，可重写 |
| **String 类** | 比较地址（不推荐） | 比较内容（推荐） |
| **包装类** | 比较地址（不可靠） | 比较值（可靠） |
| **自定义对象** | 比较地址 | 可重写比较内容 |
| **能否重写** | ✗ 不能 | ✓ 可以且经常重写 |

### 1.2 形象比喻

- **==** 像"身份证比对"：只认地址（身份证号），同一个地址就是同一个人
- **equals()** 像"指纹比对"：可以自定义规则（如比较内容），内容相同就认为是同一个对象

## 二、详细作用说明

### 2.1 基本类型比较

#### 核心要点

基本类型（byte、short、int、long、float、double、char、boolean）只能使用==比较。

```java
// int 类型比较
int a = 10;
int b = 10;
System.out.println(a == b);  // true

// double 类型比较（注意精度）
double x = 0.1;
double y = 0.1;
System.out.println(x == y);  // true
System.out.println(0.1 + 0.2 == 0.3);  // false（精度问题）

// char 类型比较
char c1 = 'A';
char c2 = 'A';
System.out.println(c1 == c2);  // true
System.out.println('A' == 65);  // true（Unicode 值）
```

#### 注意事项

1. **float/double 比较精度问题**
   ```java
   // 错误做法
   System.out.println(0.1 + 0.2 == 0.3);  // false
   
   // 正确做法：使用误差范围
   double result = 0.1 + 0.2;
   System.out.println(Math.abs(result - 0.3) < 0.00001);  // true
   ```

2. **基本类型没有 equals() 方法**
   ```java
   int a = 10;
   // a.equals(10);  // 编译错误！
   ```

### 2.2 字符串比较

#### 核心要点

String 是引用类型，==比较地址，equals() 比较内容。

```java
// 字符串字面量（常量池）
String s1 = "hello";
String s2 = "hello";
System.out.println(s1 == s2);        // true（同一对象）
System.out.println(s1.equals(s2));   // true（内容相同）

// new 创建的字符串（堆内存）
String s3 = new String("hello");
String s4 = new String("hello");
System.out.println(s3 == s4);        // false（不同对象）
System.out.println(s3.equals(s4));   // true（内容相同）
```

#### 字符串常量池

```java
// 字面量在常量池中
String s1 = "hello";  // 常量池中查找，没有则创建
String s2 = "hello";  // 直接返回常量池中的引用

// new 创建在堆内存中
String s3 = new String("hello");  // 总是新对象
String s4 = new String("hello");  // 总是新对象

// intern() 方法
String s5 = new String("world").intern();
String s6 = "world";
System.out.println(s5 == s6);  // true（都在常量池）
```

#### 最佳实践

✅ **始终使用 equals() 比较字符串内容**

```java
// 推荐
if (str.equals("hello")) { ... }

// 不推荐（可能出错）
if (str == "hello") { ... }

// 防止 null 的写法
if ("hello".equals(str)) { ... }  // 推荐！
```

### 2.3 包装类比较

#### 核心要点

包装类（Integer、Long 等）是引用类型，==比较地址，equals() 比较值。

```java
// Integer 缓存池（-128~127）
Integer i1 = 100;
Integer i2 = 100;
System.out.println(i1 == i2);      // true（缓存池）
System.out.println(i1.equals(i2)); // true

// 超出缓存范围
Integer i3 = 200;
Integer i4 = 200;
System.out.println(i3 == i4);      // false（新对象）
System.out.println(i3.equals(i4)); // true

// new 创建
Integer i5 = new Integer(100);
Integer i6 = new Integer(100);
System.out.println(i5 == i6);      // false
System.out.println(i5.equals(i6)); // true
```

#### 整数缓存池详解

```java
// 自动装箱（使用缓存池）
Integer i1 = 100;    // 等价于 Integer.valueOf(100)
Integer i2 = 100;

// valueOf 源码（简化版）
public static Integer valueOf(int i) {
    if (i >= -128 && i <= 127) {
        return IntegerCache.cache[i + 128];  // 返回缓存
    }
    return new Integer(i);  // 创建新对象
}

// 手动 new（不使用缓存）
Integer i3 = new Integer(100);  // 总是新对象
```

#### 最佳实践

✅ **始终使用 equals() 比较包装类的值**

```java
// 推荐
if (count.equals(100)) { ... }

// 危险！结果不可预测
if (count == 100) { ... }

// 拆箱后比较（基本类型）
if (count.intValue() == 100) { ... }  // 注意 NPE 风险
```

### 2.4 自定义对象比较

#### 核心要点

自定义对象默认 equals() 比较地址（Object 类实现），需要重写才能比较内容。

```java
public class User {
    private Long id;
    private String name;
    
    // 未重写 equals() 时，使用 Object 的实现
    // public boolean equals(Object obj) {
    //     return this == obj;  // 比较地址
    // }
}

User user1 = new User(1L, "张三");
User user2 = new User(1L, "张三");
System.out.println(user1 == user2);         // false
System.out.println(user1.equals(user2));    // false（默认比较地址）
```

#### 重写 equals() 的正确方式

```java
@Override
public boolean equals(Object obj) {
    // 1. 检查是否为 null
    if (obj == null) {
        return false;
    }
    
    // 2. 检查是否是同一个对象（地址相同）
    if (this == obj) {
        return true;
    }
    
    // 3. 检查是否是同一类型
    if (!(obj instanceof User)) {
        return false;
    }
    
    // 4. 类型转换
    User other = (User) obj;
    
    // 5. 比较关键字段（id）
    if (this.id == null) {
        return other.id == null;
    } else {
        return this.id.equals(other.id);
    }
}
```

#### equals() 和 hashCode() 的关系

```java
@override
public int hashCode() {
    // 与 equals() 保持一致：equals() 相等的对象，hashCode() 必须相等
    return id != null ? id.hashCode() : 0;
}
```

**重要原则**：
1. 如果两个对象 equals() 相等，则它们的 hashCode() 必须相等
2. hashCode() 相等的对象，equals() 不一定相等（哈希碰撞）
3. 集合类（HashMap、HashSet）依赖这两个方法

## 三、对比表格

### 3.1 完整对比表

| 数据类型 | ==比较 | equals() 比较 | 推荐使用 |
|----------|--------|--------------|----------|
| **基本类型** | ✓ 比较值 | ✗ 不支持 | == |
| **String** | 比较地址 | 比较内容 | equals() |
| **Integer(-128~127)** | 可能 true（缓存） | 比较值 | equals() |
| **Integer(超出范围)** | false（新对象） | 比较值 | equals() |
| **其他包装类** | 比较地址 | 比较值 | equals() |
| **自定义对象（未重写）** | 比较地址 | 比较地址 | 看需求 |
| **自定义对象（已重写）** | 比较地址 | 比较内容 | 看需求 |

### 3.2 记忆口诀

```
基本类型用==，引用类型 equals()
String 包装自定义，内容比较最安全
==比较的是地址，equals 可以自定义
要想内容比地址，重写 equals 才可以
```

## 四、面试高频考点

### 4.1 经典问题与答案

#### Q1: ==和 equals() 的根本区别？

**答**：

1. **本质不同**：==是运算符，equals() 是 Object 类的方法
2. **比较对象不同**：
   - 基本类型：==比较值
   - 引用类型：==比较地址，equals() 默认比较地址，可重写为比较内容
3. **能否重写**：==不能重写，equals() 可以且经常重写

#### Q2: String 类的==和 equals() 有什么区别？

**答**：

```java
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");

// ==比较地址
s1 == s2;  // true（常量池同一对象）
s1 == s3;  // false（不同对象）

// equals() 比较内容
s1.equals(s2);  // true
s1.equals(s3);  // true
```

结论：**比较字符串内容要用 equals()，不要用==**

#### Q3: Integer i = 100; Integer j = 100; i==j 的结果是什么？

**答**：true。

原因：
- Java 有整数缓存池（-128~127）
- 自动装箱使用 `Integer.valueOf()` 方法
- 范围内的值会从缓存池返回，是同一个对象
- 所以==比较结果为 true

追问：那 Integer i = 200; Integer j = 200; 呢？

**答**：false。超出缓存范围，每次都是新对象。

#### Q4: 为什么重写 equals() 时必须重写 hashCode()？

**答**：

1. **Java 规范要求**：equals() 相等的对象，hashCode() 必须相等
2. **集合类依赖**：HashMap、HashSet 等依赖 hashCode() 定位
3. **避免 bug**：只重写 equals() 会导致集合行为异常

示例：
```java
User user1 = new User(1L, "张三");
User user2 = new User(1L, "张三");

// 只重写 equals()，没重写 hashCode()
System.out.println(user1.equals(user2));  // true
System.out.println(user1.hashCode());     // 不同！

// 放入 HashSet
Set<User> set = new HashSet<>();
set.add(user1);
set.add(user2);  // 会被添加进去（hashCode 不同）
System.out.println(set.size());  // 2（期望是 1）
```

#### Q5: 如何正确重写 equals() 方法？

**答**：遵循以下步骤：

1. **检查 null**：`if (obj == null) return false;`
2. **检查地址**：`if (this == obj) return true;`
3. **检查类型**：`if (!(obj instanceof User)) return false;`
4. **类型转换**：`User other = (User) obj;`
5. **比较字段**：选择关键字段比较（如 id）

完整示例：
```java
@Override
public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    if (!(obj instanceof User)) return false;
    User other = (User) obj;
    if (this.id == null) {
        return other.id == null;
    } else {
        return this.id.equals(other.id);
    }
}
```

#### Q6: 什么时候用==，什么时候用 equals()？

**答**：

**使用==的场景**：
1. 基本类型比较（int、double、char 等）
2. 判断是否为同一个对象实例
3. 与 null 比较（`if (str == null)`）

**使用 equals() 的场景**：
1. 字符串内容比较
2. 包装类值比较
3. 自定义对象内容比较（需要重写）
4. 业务逻辑判断（如用户 ID 相同即为同一用户）

### 4.2 实战应用场景

#### 场景 1: 如何安全地比较字符串？

**答**：

```java
// 1. 常量在前，防止 NPE
if ("admin".equals(username)) { ... }

// 2. 工具类方法
if (StringUtils.equals(str1, str2)) { ... }  // Apache Commons

// 3. Java 7+ Objects 类
if (Objects.equals(str1, str2)) { ... }

// 4. 避免==
if (str == "admin") { ... }  // 危险！
```

#### 场景 2: HashMap 中如何使用 equals() 和 hashCode()？

**答**：

```java
// 1. put 操作
map.put(key, value);
// - 计算 key.hashCode() 找到桶位置
// - 使用 key.equals() 比较是否已存在

// 2. get 操作
map.get(key);
// - 计算 key.hashCode() 找到桶
// - 使用 key.equals() 找到具体 entry

// 3. 自定义 key 必须重写两个方法
class MyKey {
    private String id;
    
    @Override
    public boolean equals(Object obj) {
        // 比较 id
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
```

#### 场景 3: 为什么不建议在循环中使用==比较字符串？

**答**：

```java
// 错误示例
for (String str : list) {
    if (str == "target") {  // 可能永远不匹配
        // ...
    }
}

// 正确示例
for (String str : list) {
    if ("target".equals(str)) {  // 安全
        // ...
    }
}
```

原因：循环中的字符串可能来自不同来源（文件、网络、数据库），不一定是常量池中的对象。

## 五、实验演示说明

### 5.1 启动服务

```bash
# 在项目根目录执行
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 5.2 基本类型比较演示

```bash
curl -X POST "http://localhost:9510/interview-agent/equals/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"comparisonType":"BASIC"}'
```

**观察点**：
- 基本类型只能使用==
- 不支持 equals() 方法

### 5.3 字符串比较演示

```bash
curl -X POST "http://localhost:9510/interview-agent/equals/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"comparisonType":"STRING"}'
```

**观察点**：
- 常量池 vs 堆内存
- ==比较地址，equals() 比较内容

### 5.4 包装类比较演示

```bash
curl -X POST "http://localhost:9510/interview-agent/equals/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"comparisonType":"WRAPPER"}'
```

**观察点**：
- 整数缓存池的影响（-128~127）
- ==比较的不可靠性

### 5.5 自定义对象比较演示

```bash
curl -X POST "http://localhost:9510/interview-agent/equals/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"comparisonType":"CUSTOM"}'
```

**观察点**：
- equals() 和 hashCode() 的重写
- 两者的一致性

## 六、常见误区

### 6.1 关于==

❌ **误区**：==可以比较所有类型

✅ **正解**：基本类型用==，引用类型谨慎使用

❌ **误区**：Integer 用==比较值没问题

✅ **正解**：缓存范围内可能对，但不保险

❌ **误区**：字符串用==比较更快

✅ **正解**：快但结果错误，应该用 equals()

### 6.2 关于 equals()

❌ **误区**：equals() 只能比较地址

✅ **正解**：默认比较地址，但可以重写

❌ **误区**：重写 equals() 可以不重写 hashCode()

✅ **正解**：必须一起重写，否则集合类会出问题

❌ **误区**：equals() 可以随便重写

✅ **正解**：要遵循自反性、对称性、传递性、一致性

## 七、最佳实践建议

### 7.1 比较操作的最佳实践

1. **基本类型：直接用==**
   ```java
   if (count == 0) { ... }
   ```

2. **字符串：用 equals()，常量在前**
   ```java
   if ("expected".equals(actual)) { ... }
   ```

3. **包装类：用 equals() 或拆箱**
   ```java
   if (count.equals(100)) { ... }
   // 或
   if (count != null && count == 100) { ... }
   ```

4. **自定义对象：根据业务重写**
   ```java
   @Override
   public boolean equals(Object obj) {
       // 比较业务关键字段
   }
   
   @Override
   public int hashCode() {
       // 与 equals() 保持一致
   }
   ```

### 7.2 使用工具类

1. **Apache Commons Lang**
   ```java
   // 安全比较
   StringUtils.equals(str1, str2);
   ObjectUtils.equals(obj1, obj2);
   
   // 空安全
   StringUtils.isEmpty(str);
   ```

2. **Java 7+ Objects 类**
   ```java
   Objects.equals(obj1, obj2);  // 空安全
   Objects.hash(field1, field2);  // 生成 hashCode
   ```

3. **Lombok**
   ```java
   @Data
   @EqualsAndHashCode(of = {"id"})  // 只比较 id 字段
   public class User {
       private Long id;
       private String name;
   }
   ```

## 八、总结

### 8.1 一句话记忆

- **基本类型**：只能用==
- **引用类型**：优先 equals()
- **String 包装**：必须 equals()
- **自定义对象**：重写要成对

### 8.2 核心要点

1. ==是运算符，equals() 是方法
2. 基本类型用==，引用类型用 equals()（通常）
3. String 和包装类必须用 equals() 比较内容
4. 自定义对象重写 equals() 时必须重写 hashCode()
5. 集合类依赖 equals() 和 hashCode() 工作
6. 使用工具类可以让比较更安全

### 8.3 延伸学习

- Object 类的其他方法（toString、hashCode 等）
- 集合框架的内部实现
- 哈希算法和哈希碰撞
- 不可变类的设计原则

---

**对应面试题**：问题 004 - ==和 equals() 方法的区别

**关键词**：==, equals, 地址比较，内容比较，整数缓存池，hashCode

**难度等级**：⭐⭐⭐（基础必考题，必须掌握）
