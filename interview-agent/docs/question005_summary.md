# 问题 005: String、StringBuffer、StringBuilder 的区别 - 总结文档

## 一、核心知识点回顾

### 1.1 三种字符串类的本质区别

| 特性 | String | StringBuffer | StringBuilder |
|------|--------|--------------|---------------|
| **可变性** | 不可变 | 可变 | 可变 |
| **线程安全** | ✓ 安全（final） | ✓ 安全（synchronized） | ✗ 不安全 |
| **同步锁** | ✗ 无 | ✓ 有 | ✗ 无 |
| **性能** | 低（频繁创建对象） | 中等（有锁开销） | 高（无锁开销） |
| **内存占用** | 高 | 低 | 低 |
| **JDK 版本** | JDK 1.0 | JDK 1.0 | JDK 5.0 |
| **继承关系** | final class | 继承自 AbstractStringBuilder | 继承自 AbstractStringBuilder |

### 1.2 形象比喻

- **String** 像"刻在石头上的文字"：一旦刻好就不能修改，要改只能重新刻一块
- **StringBuffer** 像"带锁的白板"：可以擦写修改，但有锁保护（多线程安全），效率稍低
- **StringBuilder** 像"普通的白板"：可以擦写修改，没有锁（单线程），效率最高

## 二、详细作用说明

### 2.1 String - 不可变字符序列

#### 核心特性

```java
// 1. String 的不可变性
String str = "hello";
str.concat(" world");  // str 仍然是 "hello"
System.out.println(str);  // 输出：hello

// concat() 返回了新字符串，原字符串未改变
String newStr = str.concat(" world");
System.out.println(newStr);  // 输出：hello world
```

#### 为什么 String 是不可变的？

1. **设计原因**
   - 保证字符串常量池的安全性
   - 作为 HashMap/HashSet 的 key 更可靠
   - 天然线程安全，无需同步

2. **实现原理**
   ```java
   // JDK 8 源码（简化版）
   public final class String implements Serializable, Comparable<String>, CharSequence {
       private final char value[];  // final 修饰，不可变
       
       public String concat(String str) {
           // 创建新数组，复制旧内容，添加新内容
           char buf[] = new char[newLen];
           // ... 复制操作
           return new String(0, newLen, buf);  // 返回新对象
       }
   }
   ```

3. **字符串常量池**
   ```java
   // 字面量创建（在常量池中）
   String s1 = "hello";
   String s2 = "hello";
   System.out.println(s1 == s2);  // true（同一对象）
   
   // new 创建（在堆内存中）
   String s3 = new String("hello");
   System.out.println(s1 == s3);  // false（不同对象）
   ```

#### 性能问题

```java
// 低效示例：循环中大量拼接
String result = "";
for (int i = 0; i < 10000; i++) {
    result += String.valueOf(i);  // 每次创建新对象
}
// 创建了 10000 个 String 对象，效率极低

// 高效示例：使用 StringBuilder
StringBuilder builder = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    builder.append(i);  // 只创建一个对象
}
String result = builder.toString();
```

### 2.2 StringBuffer - 可变、线程安全

#### 核心特性

```java
// 1. 可变性
StringBuffer buffer = new StringBuffer("hello");
buffer.append(" world");  // 在原对象上修改
System.out.println(buffer);  // 输出：hello world

// 2. 线程安全（方法有 synchronized 修饰）
// 多个线程同时 append 不会出现并发问题
StringBuffer buffer = new StringBuffer();
// thread1: buffer.append("A");
// thread2: buffer.append("B");
// 安全，但有效率开销

// 3. 链式调用
buffer.append("A").append("B").append("C");
System.out.println(buffer);  // 输出：ABC
```

#### 源码分析

```java
// JDK 8 源码（简化版）
public final class StringBuffer extends AbstractStringBuilder 
        implements Serializable, Comparable<StringBuffer>, CharSequence {
    
    // synchronized 保证线程安全
    @Override
    public synchronized StringBuffer append(String str) {
        super.append(str);
        return this;
    }
    
    @Override
    public synchronized String toString() {
        return new String(value, 0, count);
    }
}
```

#### 性能测试

```java
// 单线程环境下的性能对比
int loopCount = 10000;

// String
long start1 = System.currentTimeMillis();
String result1 = "";
for (int i = 0; i < loopCount; i++) {
    result1 += String.valueOf(i);
}
long time1 = System.currentTimeMillis() - start1;
// 耗时：约 500-1000ms

// StringBuffer
long start2 = System.currentTimeMillis();
StringBuffer buffer = new StringBuffer();
for (int i = 0; i < loopCount; i++) {
    buffer.append(i);
}
long time2 = System.currentTimeMillis() - start2;
// 耗时：约 10-20ms

// StringBuilder
long start3 = System.currentTimeMillis();
StringBuilder builder = new StringBuilder();
for (int i = 0; i < loopCount; i++) {
    builder.append(i);
}
long time3 = System.currentTimeMillis() - start3;
// 耗时：约 5-10ms
```

### 2.3 StringBuilder - 可变、线程不安全

#### 核心特性

```java
// 1. 基本使用
StringBuilder builder = new StringBuilder("hello");
builder.append(" world");
System.out.println(builder);  // 输出：hello world

// 2. 高效拼接（单线程）
StringBuilder builder = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    builder.append(i);  // 高效
}
String result = builder.toString();

// 3. 其他常用方法
builder.insert(0, "Start: ");  // 插入
builder.delete(0, 6);          // 删除
builder.reverse();             // 反转
builder.replace(0, 5, "New");  // 替换
```

#### 与 StringBuffer 的区别

```java
// StringBuilder 源码（简化版）
public final class StringBuilder extends AbstractStringBuilder 
        implements Serializable, Comparable<StringBuilder>, CharSequence {
    
    // 没有 synchronized 修饰
    @Override
    public StringBuilder append(String str) {
        super.append(str);
        return this;
    }
}

// 对比：
// StringBuffer: public synchronized StringBuffer append(String str)
// StringBuilder: public StringBuilder append(String str)
// 差别就在 synchronized 关键字
```

#### 多线程环境下的问题

```java
// 危险示例：StringBuilder 在多线程下不安全
StringBuilder builder = new StringBuilder();

// thread1: builder.append("A");
// thread2: builder.append("B");
// 可能出现数据丢失或错乱

// 解决方案：
// 1. 使用 StringBuffer（推荐）
// 2. 手动加锁
synchronized(builder) {
    builder.append("A");
}
```

## 三、对比表格

### 3.1 完整对比表

| 对比维度 | String | StringBuffer | StringBuilder |
|----------|--------|--------------|---------------|
| **底层结构** | final char[] | char[] | char[] |
| **可变性** | 不可变 | 可变 | 可变 |
| **线程安全** | ✓ 安全 | ✓ 安全（synchronized） | ✗ 不安全 |
| **同步锁** | ✗ 无 | ✓ 有 | ✗ 无 |
| **性能** | 低 | 中等 | 高 |
| **内存占用** | 高（频繁创建） | 低 | 低 |
| **适用场景** | 少量操作、常量 | 多线程、频繁修改 | 单线程、频繁修改 |
| **字符串拼接** | 不推荐 | 推荐 | 强烈推荐 |
| **作为 Key** | ✓ 推荐 | ✗ 不推荐 | ✗ 不推荐 |

### 3.2 性能对比（循环 10000 次拼接）

| 类型 | 耗时 | 相对性能 |
|------|------|----------|
| String | ~800ms | 1x |
| StringBuffer | ~15ms | ~53x |
| StringBuilder | ~8ms | ~100x |

### 3.3 记忆口诀

```
String 不变最安全，频繁修改性能差
StringBuffer 线程安，同步锁来保平安
StringBuilder 最快，单线程里它是王
三者选择看场景，匹配使用效率高
```

## 四、面试高频考点

### 4.1 经典问题与答案

#### Q1: String、StringBuffer、StringBuilder 的核心区别？

**答**：

1. **可变性**：
   - String 不可变
   - StringBuffer 和 StringBuilder 可变

2. **线程安全性**：
   - String：final 修饰，不可变所以安全
   - StringBuffer：有 synchronized，线程安全
   - StringBuilder：无同步锁，线程不安全

3. **性能**：
   - String 最低（频繁创建对象）
   - StringBuffer 中等（有锁开销）
   - StringBuilder 最高（无锁开销）

#### Q2: 为什么 String 是不可变的？

**答**：

1. **字符串常量池的需要**
   - 保证相同字面量的字符串共享同一对象
   - 节省内存空间

2. **作为集合 key 的安全性**
   - HashMap/HashSet 的 key 不会改变
   - hashCode() 只在创建时计算一次

3. **线程安全**
   - 不可变对象天然线程安全
   - 可以在多线程间共享

4. **安全性**
   - 用作文件路径、网络 URL、数据库连接等参数时更安全

#### Q3: String str = new String("abc") 创建了几个对象？

**答**：可能 1 个或 2 个。

分析：
1. **"abc"字面量**：如果常量池中没有"abc"，则创建 1 个
2. **new String()**：在堆内存中创建 1 个

所以：
- 如果常量池中已有"abc"：只创建 1 个对象（new 的那个）
- 如果常量池中没有"abc"：创建 2 个对象（常量池 1 个 + new 1 个）

验证：
```java
String s1 = "abc";      // 常量池创建 1 个
String s2 = new String("abc");  // 堆内存创建 1 个
System.out.println(s1 == s2);  // false
```

#### Q4: 什么时候用 String，什么时候用 StringBuffer/StringBuilder？

**答**：

**使用 String 的场景**：
1. 字符串不需要频繁修改
2. 作为常量使用
3. 需要作为 HashMap/HashSet 的 key
4. 多线程环境下的共享数据

**使用 StringBuffer 的场景**：
1. 多线程环境下的字符串拼接
2. 字符串需要频繁修改
3. 对线程安全有要求

**使用 StringBuilder 的场景**：
1. 单线程环境下的字符串拼接
2. 字符串需要频繁修改
3. 对性能要求较高的场景

#### Q5: StringBuffer 和 StringBuilder 如何选择？

**答**：

1. **看是否多线程**
   - 多线程 → StringBuffer
   - 单线程 → StringBuilder

2. **看性能要求**
   - 高性能要求 → StringBuilder
   - 一般要求 → 都可以

3. **实际开发建议**
   - 99% 的场景用 StringBuilder
   - 只有明确需要线程安全时才用 StringBuffer
   - 或者使用 ThreadLocal + StringBuilder

#### Q6: String 的 intern() 方法有什么作用？

**答**：

intern() 方法可以将字符串放入常量池。

```java
// 示例 1
String s1 = new String("hello");
String s2 = s1.intern();
String s3 = "hello";
System.out.println(s1 == s2);  // false
System.out.println(s2 == s3);  // true

// 示例 2（JDK 7+）
String s4 = new String("1") + new String("2");
String s5 = s4.intern();
String s6 = "12";
System.out.println(s4 == s5);  // false
System.out.println(s5 == s6);  // true
```

应用场景：
1. 减少内存占用（重复字符串）
2. 加快比较速度（== 比较）
3. 确保唯一性

### 4.2 实战应用场景

#### 场景 1: 如何高效地进行字符串拼接？

**答**：

```java
// 1. 单线程循环拼接（推荐）
StringBuilder builder = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    builder.append(i);
}

// 2. 已知大小的拼接（更高效）
StringBuilder builder = new StringBuilder(10000);
for (int i = 0; i < 10000; i++) {
    builder.append(i);
}

// 3. 多线程拼接（安全）
StringBuffer buffer = new StringBuffer();
// 多个线程同时 append

// 4. Java 8+ Stream API
String result = IntStream.range(0, 10000)
    .mapToObj(String::valueOf)
    .collect(Collectors.joining());
```

#### 场景 2: 如何优化字符串性能？

**答**：

1. **避免循环中的+=**
   ```java
   // 错误
   String result = "";
   for (...) { result += x; }
   
   // 正确
   StringBuilder builder = new StringBuilder();
   for (...) { builder.append(x); }
   ```

2. **预分配容量**
   ```java
   // 估计大小，减少扩容
   StringBuilder builder = new StringBuilder(expectedSize);
   ```

3. **合理使用常量池**
   ```java
   // 使用字面量
   String str = "hello";
   
   // 必要时使用 intern()
   String interned = str.intern();
   ```

4. **避免不必要的 toString()**
   ```java
   // 直接传递 StringBuilder
   log.info("Result: {}", builder);
   
   // 而不是
   log.info("Result: {}", builder.toString());
   ```

## 五、实验演示说明

### 5.1 启动服务

```bash
# 在项目根目录执行
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 5.2 String 类演示

```bash
curl -X POST "http://localhost:9510/interview-agent/stringbuilder/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"stringType":"STRING"}'
```

**观察点**：
- String 的不可变性
- 性能测试结果（较慢）

### 5.3 StringBuffer 类演示

```bash
curl -X POST "http://localhost:9510/interview-agent/stringbuilder/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"stringType":"STRINGBUFFER"}'
```

**观察点**：
- StringBuffer 的可变性
- 线程安全特性
- 性能测试结果（中等）

### 5.4 StringBuilder 类演示

```bash
curl -X POST "http://localhost:9510/interview-agent/stringbuilder/demonstrate" \
  -H "Content-Type: application/json" \
  -d '{"stringType":"STRINGBUILDER"}'
```

**观察点**：
- StringBuilder 的可变性
- 高性能表现
- 无同步锁开销

## 六、常见误区

### 6.1 关于 String

❌ **误区**：String 可以随便修改

✅ **正解**：String 一旦创建就不可变，修改会创建新对象

❌ **误区**：String 拼接用+=没问题

✅ **正解**：循环中大量拼接要用 StringBuilder

### 6.2 关于 StringBuffer

❌ **误区**：StringBuffer 总是比 StringBuilder 好

✅ **正解**：单线程下 StringBuilder 更快

❌ **误区**：StringBuffer 绝对安全

✅ **正解**：复合操作仍需额外同步

### 6.3 关于 StringBuilder

❌ **误区**：StringBuilder 可以在多线程中使用

✅ **正解**：线程不安全，需手动同步

❌ **误区**：StringBuilder 是 JDK 1.0 就有的

✅ **正解**：JDK 5.0 才引入

## 七、最佳实践建议

### 7.1 选择原则

1. **优先 StringBuilder**
   - 除非明确需要线程安全
   - 性能最优

2. **谨慎使用 String 拼接**
   - 避免在循环中使用+=
   - 少量拼接可以用

3. **StringBuffer 的特殊场景**
   - 多线程环境
   - 或使用 ThreadLocal 包装 StringBuilder

### 7.2 代码规范

```java
// 推荐：预分配容量
StringBuilder builder = new StringBuilder(256);

// 推荐：链式调用
builder.append("A").append("B").append("C");

// 推荐：及时转 String
String result = builder.toString();

// 不推荐：容量不足频繁扩容
StringBuilder builder = new StringBuilder();  // 默认 16
while (...) {
    builder.append(largeString);  // 频繁扩容
}
```

## 八、总结

### 8.1 一句话记忆

- **String**：不可变，安全但慢
- **StringBuffer**：可变，安全中等快
- **StringBuilder**：可变，不安全最快

### 8.2 核心要点

1. String 不可变，适合常量和少量操作
2. StringBuffer 线程安全，适合多线程
3. StringBuilder 性能最优，适合单线程大量拼接
4. 循环中避免使用 String 的+=拼接
5. 根据场景选择合适的字符串类

### 8.3 延伸学习

- JVM 字符串常量池的实现
- AbstractStringBuilder 源码分析
- 字符串编码与解码
- 正则表达式与字符串处理

---

**对应面试题**：问题 005 - String、StringBuffer、StringBuilder 的核心区别

**关键词**：String, StringBuffer, StringBuilder, 不可变，线程安全，性能

**难度等级**：⭐⭐⭐（基础必考题，必须掌握）
