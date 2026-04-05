# HashMap、LinkedHashMap、TreeMap 的区别 面试深度解析

## 问题概述

这是 **Java 集合框架高频核心对比题**，三者均实现 [Map](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Map.java#L128-L1267) 接口（键值对存储），核心差异源于**底层数据结构**，直接决定了**有序性、键的排序规则、性能表现**和适用场景。

核心结论：

| 实现类 | 底层结构 | 核心特性 | 核心价值 |
|--------|----------|----------|----------|
| [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) | 数组 + 链表 + 红黑树（JDK1.8+） | **无序**、键唯一依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149)+[equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221) | 最快的键值查找/插入 |
| [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) | 哈希表 + 双向链表 | **插入/访问有序**、继承 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 核心逻辑 | 键值映射 + 保证顺序 |
| [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) | 红黑树（自平衡二叉查找树） | **键排序有序**、键唯一依赖 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119)/[Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212) | 键值映射 + 排序 + 范围查找 |

---

## 一、核心区别对比表（面试必背）
| 对比维度 | [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) | [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) | [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) |
|----------|-----------|-----------------|-----------|
| **底层数据结构** | 数组 + 链表 + 红黑树（JDK1.8+） | 哈希表（继承 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)） + **双向链表** | 红黑树（实现 [NavigableMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/NavigableMap.java#L66-L312)） |
| **有序性** | **无序**（键的顺序由哈希值决定，与插入顺序无关） | **插入有序**（默认）/ **访问有序**（通过 `accessOrder` 控制） | **键排序有序**（自然排序/自定义排序） |
| **键唯一性原理** | 依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 计算哈希值，[equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221) 判断是否相同 | **完全继承 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)**，双向链表仅维护顺序 | 依赖 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 的 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 或 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212) 的 [compare()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620)，返回 0 视为键重复 |
| **查找时间复杂度** | **O(1)**（无哈希冲突时）；冲突时链表 O(n)、红黑树 O(logn) | **O(1)**（比 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 略高，需维护链表指针） | **O(logn)**（红黑树二分查找） |
| **插入/删除性能** | 高（无冲突时 O(1)） | 中（比 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 多维护链表节点） | 中（红黑树旋转调整平衡） |
| **null 支持** | 允许 **1 个 null 键**、多个 null 值 | 允许 **1 个 null 键**、多个 null 值（继承 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)） | **不允许 null 键**（排序时抛 [NullPointerException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/NullPointerException.java#L51-L89)）；允许多个 null 值 |
| **继承关系** | 继承 [AbstractMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/AbstractMap.java#L131-L553)，实现 [Map](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Map.java#L128-L1267) 接口 | **继承 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)**，实现 [Map](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Map.java#L128-L1267) 接口 | 继承 [AbstractMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/AbstractMap.java#L131-L553)，实现 [NavigableMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/NavigableMap.java#L66-L312) 接口 |
| **线程安全性** | 非线程安全 | 非线程安全 | 非线程安全 |
| **独有特性** | 哈希扰动、红黑树转换、扩容机制 | 维护键的顺序；`accessOrder=true` 时支持 LRU 缓存 | 支持范围查找（`ceilingKey()`/`floorKey()`）、键排序调整 |
| **适用场景** | 高并发读多写少、追求最快查询效率 | 键值映射 + 需要保证插入/访问顺序 | 键值映射 + 需要对键排序 + 范围查询 |

---

## 二、深度解析（源码级+原理）
### 1. [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)：键值映射的性能标杆
#### （1）底层核心结构（JDK1.8+）
[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 是 **哈希表** 的经典实现，结构为 **数组（桶）+ 链表 + 红黑树**，核心设计是为了平衡查找效率和空间利用率：
- **数组（桶）**：默认初始容量 `16`，每个桶存储链表/红黑树的头节点；
- **链表**：解决哈希冲突，当多个键的哈希值对应同一个桶时，用链表存储；
- **红黑树**：当链表长度 ≥ `8` 且数组长度 ≥ `64` 时，链表转为红黑树，将查找复杂度从 `O(n)` 降为 `O(logn)`。

#### （2）核心特性：无序性 + 键唯一性
- **无序性**：键的存储位置由 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 计算的哈希值决定，与插入顺序无关。
  ```java
  HashMap<String, Integer> map = new HashMap<>();
  map.put("B", 2);
  map.put("A", 1);
  System.out.println(map.keySet()); // 输出可能为 [A, B] 或 [B, A]，顺序不固定
  ```
- **键唯一性**：插入重复键时，新值覆盖旧值。底层依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221) 校验：
  1. 计算键的哈希值，定位到数组桶位置；
  2. 桶内无元素 → 直接存入；
  3. 桶内有元素 → 调用 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221) 比较：相等则覆盖，不等则加入链表/红黑树。

#### （3）关键源码：扩容机制（面试重点）
默认负载因子 `0.75`，当 `size > 容量 × 负载因子` 时，触发扩容：
- 扩容规则：新容量 = 原容量 × `2`；
- 扩容操作：重新计算所有键的哈希值，迁移到新数组（**耗时操作**，初始化时指定容量可优化）。

#### （4）null 支持：1 个 null 键
[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 允许存储 `null` 键，因为 `null` 的哈希值被固定为 `0`，对应数组的第 `0` 个桶，且只能存储 1 个（保证键唯一）。

### 2. [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)：有序的键值映射
#### （1）底层核心结构：哈希表 + 双向链表
[LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) **直接继承 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)**，完全复用父类的哈希表逻辑，额外通过 **双向链表** 维护键的顺序，源码核心如下：
```java
public class LinkedHashMap<K,V> extends HashMap<K,V> {
    // 双向链表的头节点和尾节点
    transient LinkedHashMap.Entry<K,V> head;
    transient LinkedHashMap.Entry<K,V> tail;

    // 顺序控制：true=访问有序，false=插入有序（默认）
    final boolean accessOrder;

    // 重写 HashMap 的节点类，增加前驱/后继指针
    static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
}
```

#### （2）核心特性：两种有序模式
- **插入有序（默认）**：双向链表按键的插入顺序排列，遍历顺序 = 插入顺序。
  ```java
  LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
  map.put("B", 2);
  map.put("A", 1);
  System.out.println(map.keySet()); // 固定输出 [B, A]
  ```
- **访问有序**：创建时指定 `accessOrder=true`，调用 [get()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L612-L614)/[put()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L654-L656) 访问键时，该键会被移到双向链表尾部，支持 **LRU（最近最少使用）缓存** 实现。
  ```java
  // 访问有序模式
  LinkedHashMap<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
  map.put("A", 1);
  map.put("B", 2);
  map.get("A"); // 访问 A，A 移到尾部
  System.out.println(map.keySet()); // 输出 [B, A]
  ```

#### （3）性能特点
- 查找/插入性能略低于 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)（需维护双向链表的指针）；
- 遍历性能高于 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)（双向链表直接按顺序遍历，无需计算哈希）。

### 3. [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)：支持排序的键值映射
#### （1）底层核心结构：红黑树
[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 底层是 **红黑树**（一种自平衡的二叉查找树），无需哈希表，直接通过键的大小关系组织节点，保证树的高度平衡，从而实现 `O(logn)` 的查找/插入效率。

#### （2）核心特性：键排序有序
[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 的有序性是 **键的排序顺序**，而非插入顺序，排序方式分两种：
1. **自然排序**：键必须实现 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 接口，重写 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 方法（如 [String](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/String.java#L114-L117)、[Integer](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Integer.java#L64-L67) 等默认支持）。
   ```java
   TreeMap<String, Integer> map = new TreeMap<>();
   map.put("B", 2);
   map.put("A", 1);
   System.out.println(map.keySet()); // 输出 [A, B]（按字典序排序）
   ```
2. **自定义排序**：创建 [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 时传入 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212) 接口，自定义排序规则。
   ```java
   // 自定义降序排序
   TreeMap<String, Integer> map = new TreeMap<>((a, b) -> b.compareTo(a));
   map.put("B", 2);
   map.put("A", 1);
   System.out.println(map.keySet()); // 输出 [B, A]
   ```

#### （3）键唯一性原理：依赖比较方法
[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) **不依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)**，判断键是否重复的唯一标准是：
- 自然排序：`key1.compareTo(key2) == 0` → 重复；
- 自定义排序：`comparator.compare(key1, key2) == 0` → 重复。

**面试坑点**：自定义对象作为 [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 的键时，若未实现 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 且未传入 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212)，会抛出 [ClassCastException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/ClassCastException.java#L48-L64)。

#### （4）独有功能：范围查找（面试加分项）
实现 [NavigableMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/NavigableMap.java#L66-L312) 接口，支持键的范围查询，高频方法如下：
| 方法 | 作用 |
|------|------|
| `ceilingKey(K key)` | 返回大于等于 `key` 的最小键 |
| `floorKey(K key)` | 返回小于等于 `key` 的最大键 |
| `subMap(K from, K to)` | 返回 `[from, to)` 范围内的键值对 |

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(1, "one");
map.put(3, "three");
map.put(5, "five");
System.out.println(map.ceilingKey(2)); // 输出 3
System.out.println(map.subMap(1, 5)); // 输出 {1=one, 3=three}
```

---

## 三、面试高频考点与坑点
### 1. 三者的有序性有什么区别？（必问）
答：
- [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)：无序，键的顺序由哈希值决定，与插入顺序无关；
- [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)：插入有序或访问有序，双向链表维护键的顺序；
- [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)：键排序有序，红黑树维护键的排序顺序。

### 2. TreeMap 为什么不依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)？
答：
[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 底层是红黑树，核心逻辑是**比较排序**：通过 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 或 [compare()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620) 方法判断键的大小关系，返回 0 即视为重复键。哈希表依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 定位元素，而红黑树依赖比较结果定位元素，因此不需要这两个方法。

### 3. 为什么 TreeMap 不能存储 null 键？
答：
[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 排序时会调用键的 [compareTo()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/io/File.java#L1087-L1097) 方法（或 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212) 的 [compare()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620) 方法），若键为 `null`，会触发 [NullPointerException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/NullPointerException.java#L51-L89)。而 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 和 [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) 基于哈希表，`null` 的哈希值为 0，可正常存储。

### 4. 自定义对象作为三者键时，分别需要注意什么？
答：
- [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)/[LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)：必须重写 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149) 和 [equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)，保证内容相同的对象视为重复键；
- [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)：必须实现 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119) 接口或传入 [Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212)，否则抛出 [ClassCastException](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/ClassCastException.java#L48-L64)。

### 5. 三者的性能如何选择？
答：
- 追求最快查找/插入：选 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)（O(1) 效率）；
- 键值映射+需要键顺序：选 [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)（略逊于 [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)，但遍历更快）；
- 键值映射+需要键排序：选 [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)（O(logn) 效率，支持范围查找）。

---

## 四、面试高频真题（满分回答）
### 1. 请简述 HashMap、LinkedHashMap、TreeMap 的核心区别？
答：
三者均实现 [Map](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Map.java#L128-L1267) 接口，提供键值对存储功能，核心区别源于底层数据结构：
1. **底层结构**：[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 基于哈希表，[LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) 基于哈希表+双向链表，[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 基于红黑树；
2. **有序性**：[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 无序，[LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) 插入/访问有序，[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 键排序有序；
3. **键唯一性**：[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)/[LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) 依赖 [hashCode()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149)+[equals()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)，[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 依赖 [Comparable](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Comparable.java#L84-L119)/[Comparator](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/Comparator.java#L123-L212)；
4. **性能**：[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 查找最快（O(1)），[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 查找 O(logn)；
5. **null 支持**：[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)/[LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) 允许一个 `null` 键，[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 不允许。

### 2. 如何用 LinkedHashMap 实现 LRU 缓存？
答：
[LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426) 支持访问顺序模式，当 `accessOrder=true` 时，每次访问键（[get()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L612-L614) 或 [put()](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L654-L656)）会将该键移到双向链表尾部。可以通过重写 `removeEldestEntry()` 方法实现自动移除最老的条目：
```java
LinkedHashMap<Integer, String> lruCache = new LinkedHashMap<>(16, 0.75f, true) {
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > MAX_CACHE_SIZE; // 当超过最大容量时移除最老的条目
    }
};
```

---

## 五、总结（面试速记）
1. **无序键值对** → [HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445)（性能最优）；
2. **有序键值对** → [LinkedHashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/LinkedHashMap.java#L152-L1426)（遍历快，支持LRU）；
3. **排序键值对** → [TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605)（支持范围查找）；
4. **键唯一性**：[HashMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/HashMap.java#L182-L1445) 看 [hashCode](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L147-L149)+[equals](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/lang/Object.java#L214-L221)，[TreeMap](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/util/TreeMap.java#L134-L1605) 看 [compare](file:///tmp/javasharedspaces/java/jdk8u392-openjdk/rt/java/nio/charset/Charset.java#L1613-L1620)；
5. **线程安全**：三者都非线程安全，需用 `Collections.synchronizedMap()` 包装。

## 六、实际应用场景

1. **HashMap**：适用于需要快速键值查找的场景，如缓存实现、临时数据存储等
2. **LinkedHashMap**：适用于需要键值对去重但又要保持插入或访问顺序的场景，如LRU缓存实现、历史记录等
3. **TreeMap**：适用于需要对键进行排序，或者需要范围查询的场景，如按时间排序的日志处理、区间统计等

通过我们的演示代码和API接口，您可以直观地看到三种Map实现类的区别和特性，这有助于在实际开发中选择合适的集合类型。