# HashSet、LinkedHashSet、TreeSet 的区别 面试深度解析

## 问题概述

这是 **Java 集合框架高频对比题**，三者均实现 [Set](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Set.java#L128-L252) 接口，核心特性是**元素不可重复**，但底层数据结构不同，导致**有序性、去重原理、性能表现、适用场景**存在本质差异。

核心结论：

| 实现类 | 底层结构 | 核心特性 | 核心价值 |
|--------|----------|----------|----------|
| [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) | [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)（哈希表） | **无序**、去重依赖 `hashCode()+equals()` | 快速去重、查找 |
| [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) | [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)（哈希表+双向链表） | **插入有序**、继承 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 去重逻辑 | 去重+保证插入顺序 |
| [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) | [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)（红黑树） | **排序有序**、去重依赖 `Comparable/Comparator` | 去重+排序 |

---

## 一、核心区别对比表（面试必背）
| 对比维度 | [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) | [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) | [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) |
|----------|-----------|-----------------|-----------|
| **底层数据结构** | [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)（数组+链表+红黑树，JDK1.8+） | [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)（哈希表+双向链表） | [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)（红黑树） |
| **有序性** | **无序**（元素顺序与插入顺序无关，由哈希值决定） | **插入有序**（双向链表维护插入顺序） | **排序有序**（自然排序/自定义排序） |
| **去重原理** | 依赖元素的 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 计算哈希值，[equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221) 判断是否相同 | **继承 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 逻辑**，哈希表保证唯一性，双向链表保证顺序 | 依赖 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 接口的 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 或 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212) 接口的 [compare()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620)，返回 0 视为重复元素 |
| **查找时间复杂度** | **O(1)**（哈希表直接定位，无哈希冲突时） | **O(1)**（哈希表基础+链表，略高于 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)） | **O(logn)**（红黑树二分查找） |
| **插入/删除性能** | 高（无冲突时 O(1)，冲突时 O(n)） | 中（比 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 多维护链表指针） | 中（红黑树旋转调整平衡） |
| **允许存储 `null` 元素** | 是（最多 1 个，依赖 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 键特性） | 是（最多 1 个，继承 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)） | **否**（排序时会抛出 [NullPointerException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/NullPointerException.java#L51-L89)） |
| **继承关系** | 继承 [AbstractSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/AbstractSet.java#L45-L106)，实现 [Set](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Set.java#L128-L252) 接口 | **继承 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)**，实现 [Set](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Set.java#L128-L252) 接口 | 继承 [AbstractSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/AbstractSet.java#L45-L106)，实现 [NavigableSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/NavigableSet.java#L66-L312) 接口 |
| **线程安全性** | 非线程安全 | 非线程安全 | 非线程安全 |
| **独有特性** | 无额外特性，基础去重 | 按插入顺序遍历 | 支持范围查找（如 `ceiling()`、`floor()`）、排序调整 |
| **适用场景** | 快速去重、查找，不要求顺序 | 去重+需要保证元素的插入顺序 | 去重+需要对元素进行排序 |

---

## 二、深度解析（源码级+原理）
### 1. [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)：基础去重，性能最优
#### （1）底层原理
[HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 是 [Set](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Set.java#L128-L252) 接口的**基础实现类**，底层完全依赖 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)：
- 元素存储在 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 的 `key` 位置，`value` 是固定常量 `PRESENT`（`new Object()`）；
- 利用 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 键的唯一性实现去重，利用哈希表结构实现高效查找。

#### （2）核心特性：无序性
无序性体现在 **元素存储位置由哈希值决定**，与插入顺序无关：
```java
HashSet<String> set = new HashSet<>();
set.add("B");
set.add("A");
set.add("C");
System.out.println(set); // 输出可能是 [A, C, B]，顺序不固定
```

#### （3）去重关键：必须重写 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)
自定义对象存入 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 时，若不重写这两个方法，会使用 [Object](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L43-L276) 类的默认实现（比较内存地址），导致即使内容相同的对象也会被视为不同元素。

**规范示例**
```java
class User {
    private String id;
    private String name;

    // 必须重写 hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // 必须重写 equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
}
```

### 2. [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202)：去重+插入有序
#### （1）底层原理
[LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) **继承自 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)**，底层通过 [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) 实现：
- 构造器调用父类 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 的构造器，但强制指定底层 `map` 为 [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)；
- 在哈希表基础上，额外维护一个 **双向链表**，用于记录元素的插入顺序。

**源码核心（JDK 1.8）**
```java
public class LinkedHashSet<E> extends HashSet<E> {
    public LinkedHashSet() {
        // 调用 HashSet 的构造器，指定底层为 LinkedHashMap
        super(16, 0.75f, true);
    }
}

// HashSet 中的对应构造器
HashSet(int initialCapacity, float loadFactor, boolean dummy) {
    map = new LinkedHashMap<>(initialCapacity, loadFactor);
}
```

#### （2）核心特性：插入有序
双向链表保证**遍历顺序与插入顺序完全一致**，但元素仍不可重复：
```java
LinkedHashSet<String> set = new LinkedHashSet<>();
set.add("B");
set.add("A");
set.add("C");
System.out.println(set); // 固定输出 [B, A, C]
```

#### （3）性能特点
- 查找效率略低于 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)（多维护链表指针）；
- 遍历效率高于 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)（双向链表直接按顺序遍历，无需计算哈希）。

### 3. [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338)：去重+排序有序
#### （1）底层原理
[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 底层依赖 [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)，核心结构是 **红黑树**（一种自平衡的二叉查找树），支持高效的排序和范围查找。

#### （2）核心特性：排序有序
[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 的有序性是**排序后的顺序**，而非插入顺序，排序方式分两种：
1. **自然排序**：元素实现 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 接口，重写 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 方法（默认方式）；
2. **自定义排序**：创建 [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 时传入 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212) 接口实现类，自定义排序规则。

**代码示例 1：自然排序**
```java
// String 类实现了 Comparable 接口，默认按字典序排序
TreeSet<String> set = new TreeSet<>();
set.add("B");
set.add("A");
set.add("C");
System.out.println(set); // 输出 [A, B, C]
```

**代码示例 2：自定义排序（降序）**
```java
TreeSet<String> set = new TreeSet<>((a, b) -> b.compareTo(a));
set.add("B");
set.add("A");
set.add("C");
System.out.println(set); // 输出 [C, B, A]
```

#### （3）去重关键：[compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097)/[compare()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620) 返回 0 视为重复
**[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 不依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)**，判断元素是否重复的唯一标准是：
- 自然排序：`o1.compareTo(o2) == 0` → 重复；
- 自定义排序：`comparator.compare(o1, o2) == 0` → 重复。

**面试坑点**：自定义对象存入 [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 时，若未实现 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 且未传入 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212)，会抛出 [ClassCastException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/ClassCastException.java#L48-L64)。

#### （4）独有功能：范围查找
实现 [NavigableSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/NavigableSet.java#L66-L312) 接口，支持范围查找方法（面试加分项）：
| 方法 | 作用 |
|------|------|
| `ceiling(E e)` | 返回大于等于 `e` 的最小元素 |
| `floor(E e)` | 返回小于等于 `e` 的最大元素 |
| `subSet(E from, E to)` | 返回 `[from, to)` 范围内的子集 |

```java
TreeSet<Integer> set = new TreeSet<>();
set.add(1);
set.add(3);
set.add(5);
System.out.println(set.ceiling(2)); // 输出 3
System.out.println(set.subSet(1, 5)); // 输出 [1, 3]
```

---

## 三、面试高频考点与坑点
### 1. 三者的有序性有什么区别？（必问）
答：
- [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)：无序，元素顺序由哈希值决定，与插入顺序无关；
- [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202)：插入有序，双向链表维护插入顺序，遍历顺序=插入顺序；
- [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338)：排序有序，红黑树维护排序顺序，遍历顺序=排序顺序（自然/自定义）。

### 2. TreeSet 为什么不依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)？
答：
[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 底层是红黑树，核心逻辑是**比较排序**：通过 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 或 [compare()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620) 方法判断元素大小关系，返回 0 即视为重复元素。哈希表依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 定位元素，而红黑树依赖比较结果定位元素，因此不需要这两个方法。

### 3. 为什么 TreeSet 不能存储 `null` 元素？
答：
[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 排序时会调用元素的 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 方法（或 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212) 的 [compare()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620) 方法），若元素为 `null`，会触发 [NullPointerException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/NullPointerException.java#L51-L89)。而 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 和 [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) 基于哈希表，`null` 的哈希值为 0，可正常存储。

### 4. 自定义对象存入三者时，分别需要注意什么？
答：
- [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)/[LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202)：必须重写 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)，保证内容相同的对象视为重复；
- [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338)：必须实现 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 接口或传入 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212)，否则抛出 [ClassCastException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/ClassCastException.java#L48-L64)。

### 5. 三者的性能如何选择？
答：
- 追求最快查找/插入：选 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)（O(1) 效率）；
- 去重+需要插入顺序：选 [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202)（略逊于 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)，但遍历更快）；
- 去重+需要排序：选 [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338)（O(logn) 效率，支持范围查找）。

---

## 四、面试高频真题（满分回答）
### 1. 请简述 HashSet、LinkedHashSet、TreeSet 的核心区别？
答：
三者均实现 [Set](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Set.java#L128-L252) 接口，保证元素不可重复，核心区别源于底层数据结构：
1. **底层结构**：[HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 基于 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)，[LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) 基于 [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)，[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 基于 [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)；
2. **有序性**：[HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 无序，[LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) 插入有序，[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 排序有序；
3. **去重原理**：[HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)/[LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) 依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149)+[equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)，[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 依赖 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119)/[Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212)；
4. **性能**：[HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 查找最快（O(1)），[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 查找 O(logn)；
5. **null 支持**：[HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)/[LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) 允许一个 `null`，[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 不允许。

### 2. 当需要一个去重且按插入顺序遍历的集合，你会选哪个？为什么不选 TreeSet？
答：
选 [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202)。原因：
- [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 是排序有序，而非插入有序，无法保证遍历顺序与插入顺序一致；
- [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202) 继承 [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 的去重逻辑，同时通过双向链表保证插入顺序，兼顾去重和顺序需求。

---

## 五、总结（面试速记）
1. **无序去重** → [HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368)（性能最优）；
2. **插入有序去重** → [LinkedHashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashSet.java#L78-L202)（遍历快）；
3. **排序去重** → [TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338)（支持范围查找）；
4. **去重关键**：[HashSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashSet.java#L87-L368) 看 [hashCode](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149)+[equals](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)，[TreeSet](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeSet.java#L51-L338) 看 [compare](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620)；
5. **线程安全**：三者都非线程安全，需用 `Collections.synchronizedSet()` 包装。

## 六、实际应用场景

1. **HashSet**：适用于需要快速去重和查找的场景，例如过滤重复的用户ID或商品ID列表
2. **LinkedHashSet**：适用于需要去重但又要保持原始顺序的场景，如最近访问过的商品列表
3. **TreeSet**：适用于需要排序和范围查询的场景，如按时间排序的日志处理或成绩排名

通过我们的演示代码和API接口，您可以直观地看到三种Set实现类的区别和特性，这有助于在实际开发中选择合适的集合类型。