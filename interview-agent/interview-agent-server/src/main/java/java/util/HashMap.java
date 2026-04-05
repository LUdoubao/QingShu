/*
 * Copyright (c) 1997, 2021, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import sun.misc.SharedSecrets;

/**
 * 基于哈希表的 Map 接口实现。此实现提供所有可选的映射操作，并允许 null 值和 null 键。
 * （HashMap 类大致相当于 Hashtable，只是它是不同步的，并允许 null。）
 * 此类不保证映射的顺序；特别地，它不保证顺序会随着时间的推移保持不变。
 *
 * <p>此实现为基础操作（get 和 put）提供恒定时间性能，假设哈希函数将元素适当地分散到桶中。
 * 对集合视图进行迭代所需的时间与 HashMap 实例的“容量”（桶的数量）加上其大小（键值映射的数量）成正比。
 * 因此，如果迭代性能很重要，则不要将初始容量设置得太高（或将负载因子设置得太低）非常重要。
 *
 * <p>HashMap 的实例有两个影响其性能的参数：<i>初始容量</i>和<i>负载因子</i>。
 * <i>容量</i>是哈希表中桶的数量，初始容量只是创建哈希表时的容量。
 * <i>负载因子</i>是衡量哈希表在容量自动增加之前允许达到多满的度量。
 * 当哈希表中的条目数超过负载因子与当前容量的乘积时，哈希表将被<i>重新哈希</i>
 * （即重建内部数据结构），以便哈希表的桶数大约翻倍。
 *
 * <p>作为一般规则，默认负载因子（0.75）在时间和空间成本之间提供了良好的权衡。
 * 较高的值会减少空间开销，但会增加查找成本（反映在 HashMap 类的大多数操作中，包括 get 和 put）。
 * 在设置初始容量时，应考虑映射中的预期条目数及其负载因子，以最大限度地减少重新哈希操作的数量。
 * 如果初始容量大于最大条目数除以负载因子，则永远不会发生重新哈希操作。
 *
 * <p>如果要将许多映射存储在 HashMap 实例中，则使用足够大的容量创建它将允许更有效地存储映射，
 * 而不是让它根据需要执行自动重新哈希以增加表的大小。请注意，使用许多具有相同 {@code hashCode()} 的键
 * 肯定会降低任何哈希表的性能。为了减轻影响，当键是 {@link Comparable} 时，此类可以使用键之间的比较顺序来帮助打破平局。
 *
 * <p><strong>请注意，此实现不是同步的。</strong>
 * 如果多个线程同时访问一个哈希映射，并且至少有一个线程在结构上修改了映射，则必须在外部进行同步。
 * （结构修改是添加或删除一个或多个映射的任何操作；仅更改与实例已包含的键关联的值不是结构修改。）
 * 这通常通过同步一些自然封装映射的对象来完成。
 *
 * 如果不存在这样的对象，则应使用 {@link Collections#synchronizedMap Collections.synchronizedMap}
 * 方法“包装”映射。最好在创建时执行此操作，以防止意外地不同步访问映射：
 * <pre>
 *   Map m = Collections.synchronizedMap(new HashMap(...));</pre>
 *
 * <p>此类所有“集合视图方法”返回的迭代器都是 <i>快速失败</i> 的：
 * 如果在创建迭代器之后的任何时间对映射进行结构修改，除非通过迭代器自己的 <tt>remove</tt> 方法，
 * 否则迭代器将抛出 {@link ConcurrentModificationException}。
 * 因此，在面对并发修改时，迭代器会快速而干净地失败，而不是在未来不确定的时间冒任意、不确定行为的风险。
 *
 * <p>请注意，迭代器的快速失败行为无法得到保证，因为通常来说，在存在不同步的并发修改的情况下，
 * 不可能做出任何硬性保证。快速失败迭代器会尽最大努力抛出 {@code ConcurrentModificationException}。
 * 因此，编写依赖于此异常的程序是错误的：<i>迭代器的快速失败行为应该仅用于检测错误。</i>
 *
 * <p>此类是 <a href="{@docRoot}/../technotes/guides/collections/index.html">Java 集合框架</a> 的成员。
 *
 * @param <K> 此映射维护的键的类型
 * @param <V> 映射值的类型
 *
 * @author  Doug Lea
 * @author  Josh Bloch
 * @author  Arthur van Hoff
 * @author  Neal Gafter
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Map
 * @see     TreeMap
 * @see     Hashtable
 * @since   1.2
 */
public class HashMap<K,V> extends AbstractMap<K,V>
        implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * 实现说明。
     *
     * 此映射通常充当分箱（桶）哈希表，但当箱变得太大时，它们会转换为 TreeNode 的箱，
     * 每个 TreeNode 的结构类似于 java.util.TreeMap 中的结构。大多数方法尝试使用普通箱，
     * 但在适用时（仅通过检查节点的 instanceof）中继到 TreeNode 方法。
     * TreeNode 的箱可以像其他箱一样被遍历和使用，但额外支持在过饱和时更快的查找。
     * 然而，由于正常使用中的绝大多数箱不会过饱和，因此检查树箱的存在可以在表方法的过程中延迟。
     *
     * 树箱（即元素都是 TreeNode 的箱）主要按 hashCode 排序，但在平局的情况下，
     * 如果两个元素属于相同的“类 C 实现 Comparable<C>”类型，则使用它们的 compareTo 方法进行排序。
     * （我们通过反射保守地检查泛型类型以验证这一点——请参阅 comparableClassFor 方法。）
     * 当键要么具有不同的哈希值要么是可排序的时，树箱的额外复杂性在提供最坏情况 O(log n) 操作方面是值得的。
     * 因此，在 hashCode() 方法返回值分布不良以及许多键共享一个 hashCode 的情况下，
     * 只要它们也是 Comparable，性能就会优雅地降级。
     * （如果这两者都不适用，与不采取预防措施相比，我们可能会在时间和空间上浪费大约两倍。
     * 但唯一已知的情况来自糟糕的用户编程实践，这些实践已经非常慢，以至于这几乎没有什么区别。）
     *
     * 因为 TreeNode 的大小大约是普通节点的两倍，所以我们只在箱包含足够多的节点以保证使用时才使用它们
     * （请参阅 TREEIFY_THRESHOLD）。当它们变得太小（由于删除或调整大小）时，它们会转换回普通箱。
     * 在使用良好分布的用户 hashCode 的情况下，树箱很少使用。
     * 理想情况下，在随机 hashCode 下，箱中节点的频率遵循泊松分布
     * (http://en.wikipedia.org/wiki/Poisson_distribution)，对于默认调整大小阈值 0.75，
     * 平均参数约为 0.5，尽管由于调整大小的粒度而有很大的方差。
     * 忽略方差，列表大小 k 的预期出现次数为 (exp(-0.5) * pow(0.5, k) / factorial(k))。
     * 前几个值是：
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * 更多：小于千万分之一
     *
     * 树箱的根通常是其第一个节点。然而，有时（目前仅在 Iterator.remove 上），根可能在其他地方，
     * 但可以通过父链接恢复（方法 TreeNode.root()）。
     *
     * 所有适用的内部方法都接受哈希码作为参数（通常由公共方法提供），允许它们相互调用而无需重新计算用户哈希码。
     * 大多数内部方法还接受一个“tab”参数，该参数通常是当前表，但在调整大小或转换时可能是新表或旧表。
     *
     * 当箱列表被树化、拆分或去树化时，我们保持它们处于相同的相对访问/遍历顺序（即字段 Node.next），
     * 以更好地保持局部性，并稍微简化调用 iterator.remove 的拆分和遍历的处理。
     * 在插入时使用比较器时，为了在重新平衡时保持总顺序（或尽可能接近此处的要求），
     * 我们比较类和 identityHashCode 作为平局打破者。
     *
     * 普通模式与树模式之间的使用和转换由于子类 LinkedHashMap 的存在而变得复杂。
     * 请参阅下面定义的钩子方法，这些方法在插入、删除和访问时被调用，允许 LinkedHashMap 的内部结构以其他方式保持独立于这些机制。
     * （这也要求将映射实例传递给一些可能创建新节点的实用方法。）
     *
     * 类似并发编程的基于 SSA 的编码风格有助于避免在所有曲折的指针操作中出现别名错误。
     */

    /**
     * 默认初始容量 - 必须是 2 的幂。
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // 也就是 16

    /**
     * 最大容量，如果构造函数的参数中隐含指定了更高的值，则使用此值。
     * 必须是 2 的幂 <= 1<<30。
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 构造函数中未指定时使用的负载因子。
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 使用树而不是列表作为箱的箱计数阈值。
     * 当向具有至少这么多节点的箱添加元素时，箱会转换为树。
     * 该值必须大于 2，并且至少应为 8，以便与树删除中关于在收缩时转换回普通箱的假设相吻合。
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 在调整大小操作期间对（拆分）箱进行去树化的箱计数阈值。
     * 应小于 TREEIFY_THRESHOLD，并且最多为 6，以便与删除下的收缩检测相吻合。
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 箱可能被树化的最小表容量。
     * （否则，如果箱中的节点太多，表会被调整大小。）
     * 应至少为 4 * TREEIFY_THRESHOLD，以避免调整大小和树化阈值之间的冲突。
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 基本的哈希箱节点，用于大多数条目。（请参阅下面的 TreeNode 子类，
     * 以及 LinkedHashMap 中的 Entry 子类。）
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;      // 键的哈希值，不可变
        final K key;         // 键
        V value;             // 值
        Node<K,V> next;      // 指向下一个节点的指针（用于处理冲突）

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /* ---------------- 静态实用方法 -------------- */

    /**
     * 计算 key.hashCode() 并将哈希的高位扩散到低位。
     * 因为表使用 2 的幂掩码，仅在当前掩码以上的位中变化的哈希集将始终冲突。
     * （已知的例子包括在小表中持有连续整数的 Float 键集。）
     * 因此，我们应用一个转换，将高位的影响向下扩散。
     * 在速度、实用性和位扩散质量之间存在权衡。
     * 因为许多常见的哈希集已经合理分布（因此不会从扩散中受益），
     * 并且因为我们使用树来处理箱中的大量冲突，所以我们只是以最便宜的方式对某些移位位进行异或，
     * 以减少系统性的损失，并合并由于表边界而永远不会用于索引计算的高位的影响。
     */
    static final int hash(Object key) {
        int h;
        // 如果 key 为 null，哈希为 0；否则，对原始 hashCode 和高位右移 16 位进行异或
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 如果 x 是 "class C implements Comparable<C>" 形式，则返回 x 的 Class，否则返回 null。
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // 绕过检查
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                            ((p = (ParameterizedType)t).getRawType() ==
                                    Comparable.class) &&
                            (as = p.getActualTypeArguments()) != null &&
                            as.length == 1 && as[0] == c) // 类型参数是 c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * 如果 x 匹配 kc（k 的可比较类），则返回 k.compareTo(x)，否则返回 0。
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // 转换为 Comparable 的强制转换
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    /**
     * 返回给定目标容量的 2 的幂大小。
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /* ---------------- 字段 -------------- */

    /**
     * 表，在首次使用时初始化，并根据需要调整大小。
     * 分配时，长度始终是 2 的幂。（在某些操作中我们也允许长度为零，
     * 以允许当前不需要的引导机制。）
     */
    transient Node<K,V>[] table;

    /**
     * 持有缓存的 entrySet()。注意 AbstractMap 字段用于 keySet() 和 values()。
     */
    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * 此映射中包含的键值映射的数量。
     */
    transient int size;

    /**
     * 此 HashMap 已被结构修改的次数。
     * 结构修改是改变 HashMap 中映射数量或以其他方式修改其内部结构（例如，重新哈希）的那些操作。
     * 此字段用于使 HashMap 的集合视图上的迭代器快速失败。（请参阅 ConcurrentModificationException）。
     */
    transient int modCount;

    /**
     * 要调整大小的下一个大小值（容量 * 负载因子）。
     *
     * @serial
     */
    // （javadoc 描述在序列化时是真实的。此外，如果表数组尚未分配，
    // 此字段保存初始数组容量，或者零表示 DEFAULT_INITIAL_CAPACITY。）
    int threshold;

    /**
     * 哈希表的负载因子。
     *
     * @serial
     */
    final float loadFactor;

    /* ---------------- 公共操作 -------------- */

    /**
     * 使用指定的初始容量和负载因子构造一个空的 HashMap。
     *
     * @param  initialCapacity 初始容量
     * @param  loadFactor      负载因子
     * @throws IllegalArgumentException 如果初始容量为负数或负载因子为非正数
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        this.loadFactor = loadFactor;
        // 阈值设置为大于等于 initialCapacity 的最小 2 的幂
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * 使用指定的初始容量和默认负载因子（0.75）构造一个空的 HashMap。
     *
     * @param  initialCapacity 初始容量
     * @throws IllegalArgumentException 如果初始容量为负数
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 使用默认初始容量（16）和默认负载因子（0.75）构造一个空的 HashMap。
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // 所有其他字段均为默认值
    }

    /**
     * 使用与指定 Map 相同的映射构造一个新的 HashMap。
     * HashMap 使用默认负载因子（0.75）和足以容纳指定 Map 中映射的初始容量创建。
     *
     * @param   m 其映射将被放入此映射的映射
     * @throws  NullPointerException 如果指定的映射为 null
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /**
     * 实现 Map.putAll 和 Map 构造函数。
     *
     * @param m 映射
     * @param evict 当最初构造此映射时为 false，否则为 true（中继到方法 afterNodeInsertion）
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (table == null) { // 预大小
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                        (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)
                resize(); // 如果现有表容量不足，先扩容
            // 遍历 m 的所有条目，逐个 put
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * 返回此映射中的键值映射数。
     *
     * @return 此映射中的键值映射数
     */
    public int size() {
        return size;
    }

    /**
     * 如果此映射不包含任何键值映射，则返回 true。
     *
     * @return 如果此映射不包含任何键值映射，则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 返回指定键映射到的值，如果此映射不包含该键的映射，则返回 {@code null}。
     *
     * <p>更正式地说，如果此映射包含从键 {@code k} 到值 {@code v} 的映射，使得
     * {@code (key==null ? k==null : key.equals(k))}，则此方法返回 {@code v}；否则返回 {@code null}。
     * （最多只能有一个这样的映射。）
     *
     * <p>返回值 {@code null} 并不一定表示映射不包含该键的映射；也可能映射将键显式映射到 {@code null}。
     * {@link #containsKey containsKey} 操作可用于区分这两种情况。
     *
     * @see #put(Object, Object)
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 实现 Map.get 和相关方法。
     *
     * @param hash 键的哈希值
     * @param key 键
     * @return 节点，如果不存在则返回 null
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        // 如果表非空，且长度大于0，且哈希值对应的桶不为空
        if ((tab = table) != null && (n = tab.length) > 0 &&
                (first = tab[(n - 1) & hash]) != null) {
            // 检查第一个节点
            if (first.hash == hash && // 总是检查第一个节点
                    ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                // 如果是树节点，在树中查找
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // 否则在链表中查找
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    /**
     * 如果此映射包含指定键的映射，则返回 true。
     *
     * @param   key 要测试是否存在于映射中的键
     * @return 如果此映射包含指定键的映射，则返回 true
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * 将指定值与此映射中的指定键相关联。
     * 如果映射先前包含该键的映射，则替换旧值。
     *
     * @param key 与指定值相关联的键
     * @param value 与指定键相关联的值
     * @return 与 {@code key} 关联的先前值，如果没有 {@code key} 的映射，则返回 {@code null}。
     *         （{@code null} 返回也可能表示映射先前将 {@code null} 与 {@code key} 关联。）
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * 实现 Map.put 和相关方法。
     *
     * @param hash 键的哈希值
     * @param key 键
     * @param value 要放置的值
     * @param onlyIfAbsent 如果为 true，则不更改现有值
     * @param evict 如果为 false，则表处于创建模式
     * @return 先前的值，如果没有则返回 null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        // 如果表为空或长度为0，则进行扩容
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        // 计算桶索引，如果桶为空，则创建新节点
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            // 如果桶的第一个节点匹配，则记录 e
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
                // 如果是树节点，调用树的 put 方法
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                // 遍历链表
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        // 到达链表末尾，插入新节点
                        p.next = newNode(hash, key, value, null);
                        // 如果链表长度达到树化阈值，则尝试树化
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    // 找到匹配的节点
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            // 如果找到已存在的键，则更新值
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        // 如果大小超过阈值，则扩容
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }

    /**
     * 初始化或加倍表大小。如果为 null，则根据字段 threshold 中保存的初始容量目标进行分配。
     * 否则，因为我们使用 2 的幂扩展，每个箱中的元素必须要么保持在相同索引，要么在新表中以 2 的幂偏移量移动。
     *
     * @return 表
     */
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            // 如果旧容量已达最大，阈值设为 Integer.MAX_VALUE，不再扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 新容量翻倍，如果仍在最大容量内且旧容量>=默认初始容量，则阈值也翻倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // 初始容量放在 threshold 中
            newCap = oldThr;
        else {               // 零初始阈值表示使用默认值
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            // 遍历旧表，将元素迁移到新表
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null) // 只有一个节点，直接放入新表
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode) // 树节点，调用 split 方法处理
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // 链表，需要拆分为两个链表：低位和高位
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            // 通过 (e.hash & oldCap) 判断是留在低位还是移动到高位
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 低位链表放在原索引
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 高位链表放在原索引 + oldCap 处
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * 将给定哈希的索引处的箱中的所有链接节点替换为树，除非表太小，在这种情况下改为调整大小。
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize(); // 表太小，优先扩容
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            // 将链表节点转换为树节点，并形成双向链表
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab); // 真正构建红黑树
        }
    }

    /**
     * 将指定映射中的所有映射复制到此映射。
     * 这些映射将替换此映射对指定映射中当前任何键的所有映射。
     *
     * @param m 要存储在此映射中的映射
     * @throws NullPointerException 如果指定的映射为 null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    /**
     * 如果存在，则从此映射中移除指定键的映射。
     *
     * @param  key 要从映射中移除其映射的键
     * @return 与 {@code key} 关联的先前值，如果没有 {@code key} 的映射，则返回 {@code null}。
     *         （{@code null} 返回也可能表示映射先前将 {@code null} 与 {@code key} 关联。）
     */
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
                null : e.value;
    }

    /**
     * 实现 Map.remove 和相关方法。
     *
     * @param hash 键的哈希值
     * @param key 键
     * @param value 如果 matchValue 为 true，则要匹配的值，否则忽略
     * @param matchValue 如果为 true，则仅在值相等时才移除
     * @param movable 如果为 false，则在移除时不要移动其他节点
     * @return 节点，如果不存在则返回 null
     */
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 &&
                (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            // 检查第一个节点
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    do {
                        if (e.hash == hash &&
                                ((k = e.key) == key ||
                                        (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            if (node != null && (!matchValue || (v = node.value) == value ||
                    (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

    /**
     * 从此映射中移除所有映射。此调用返回后，映射将为空。
     */
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null; // 清空数组元素
        }
    }

    /**
     * 如果此映射将一个或多个键映射到指定值，则返回 true。
     *
     * @param value 要测试是否存在于映射中的值
     * @return 如果此映射将一个或多个键映射到指定值，则返回 true
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                            (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回此映射中包含的键的 {@link Set} 视图。
     * 该集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。
     * 如果在迭代集合的过程中修改了映射（除了通过迭代器自己的 <tt>remove</tt> 操作），
     * 则迭代结果未定义。该集合支持元素移除，通过 <tt>Iterator.remove</tt>、
     * <tt>Set.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作
     * 从映射中移除相应的映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 此映射中包含的键的集合视图
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 返回此映射中包含的值的 {@link Collection} 视图。
     * 该集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。
     * 如果在迭代集合的过程中修改了映射（除了通过迭代器自己的 <tt>remove</tt> 操作），
     * 则迭代结果未定义。该集合支持元素移除，通过 <tt>Iterator.remove</tt>、
     * <tt>Collection.remove</tt>、<tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作
     * 从映射中移除相应的映射。它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 此映射中包含的值的视图
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 返回此映射中包含的映射的 {@link Set} 视图。
     * 该集合由映射支持，因此对映射的更改会反映在集合中，反之亦然。
     * 如果在迭代集合的过程中修改了映射（除了通过迭代器自己的 <tt>remove</tt> 操作，
     * 或通过迭代器返回的映射条目上的 <tt>setValue</tt> 操作），则迭代结果未定义。
     * 该集合支持元素移除，通过 <tt>Iterator.remove</tt>、<tt>Set.remove</tt>、
     * <tt>removeAll</tt>、<tt>retainAll</tt> 和 <tt>clear</tt> 操作从映射中移除相应的映射。
     * 它不支持 <tt>add</tt> 或 <tt>addAll</tt> 操作。
     *
     * @return 此映射中包含的映射的集合视图
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // 覆盖 JDK8 Map 扩展方法

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        if ((e = getNode(hash(key), key)) != null &&
                ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        // 确保表已初始化，且容量足够
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            // 查找是否已存在键
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        if ((e = getNode(hash, key)) != null &&
                (oldValue = e.value) != null) {
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                return v;
            }
            else
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
        }
        else if (v != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            else
                v = value;
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
            return v;
        }
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // 克隆和序列化

    /**
     * 返回此 HashMap 实例的浅表副本：键和值本身不会被克隆。
     *
     * @return 此映射的浅表副本
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }

    // 这些方法在序列化 HashSet 时也会使用
    final float loadFactor() { return loadFactor; }
    final int capacity() {
        return (table != null) ? table.length :
                (threshold > 0) ? threshold :
                        DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * 将 HashMap 实例的状态保存到流中（即序列化它）。
     *
     * @serialData 发出 HashMap 的<i>容量</i>（桶数组的长度）（int），
     *             然后是<i>大小</i>（int，键值映射的数量），
     *             然后是每个键值映射的键（Object）和值（Object）。
     *             键值映射以任意顺序发出。
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        int buckets = capacity();
        // 写出阈值、负载因子和任何隐藏内容
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * 从流中重建此映射（即反序列化）。
     * @param s 流
     * @throws ClassNotFoundException 如果找不到序列化对象的类
     * @throws IOException 如果发生 I/O 错误
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();

        // 读取 loadFactor（忽略 threshold）
        float lf = fields.get("loadFactor", 0.75f);
        if (lf <= 0 || Float.isNaN(lf))
            throw new InvalidObjectException("Illegal load factor: " + lf);

        lf = Math.min(Math.max(0.25f, lf), 4.0f);
        HashMap.UnsafeHolder.putLoadFactor(this, lf);

        reinitialize();

        s.readInt();                // 读取并忽略桶的数量
        int mappings = s.readInt(); // 读取映射数量（size）
        if (mappings < 0) {
            throw new InvalidObjectException("Illegal mappings count: " + mappings);
        } else if (mappings == 0) {
            // 使用默认值
        } else if (mappings > 0) {
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                    DEFAULT_INITIAL_CAPACITY :
                    (fc >= MAXIMUM_CAPACITY) ?
                            MAXIMUM_CAPACITY :
                            tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                    (int)ft : Integer.MAX_VALUE);

            // 检查 Map.Entry[].class，因为它是我们实际创建的类型最近的公共类型
            SharedSecrets.getJavaOISAccess().checkArray(s, Map.Entry[].class, cap);
            @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // 读取键和值，并将映射放入 HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    // 支持在反序列化期间重置 final 字段
    private static final class UnsafeHolder {
        private UnsafeHolder() { throw new InternalError(); }
        private static final sun.misc.Unsafe unsafe
                = sun.misc.Unsafe.getUnsafe();
        private static final long LF_OFFSET;
        static {
            try {
                LF_OFFSET = unsafe.objectFieldOffset(HashMap.class.getDeclaredField("loadFactor"));
            } catch (NoSuchFieldException e) {
                throw new InternalError();
            }
        }
        static void putLoadFactor(HashMap<?, ?> map, float lf) {
            unsafe.putFloat(map, LF_OFFSET, lf);
        }
    }

    /* ------------------------------------------------------------ */
    // 迭代器

    abstract class HashIterator {
        Node<K,V> next;        // 要返回的下一个条目
        Node<K,V> current;     // 当前条目
        int expectedModCount;  // 用于快速失败
        int index;             // 当前槽位

        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // 前进到第一个条目
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator
            implements Iterator<K> {
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
            implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
            implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }

    /* ------------------------------------------------------------ */
    // 分割器

    static class HashMapSpliterator<K,V> {
        final HashMap<K,V> map;
        Node<K,V> current;          // 当前节点
        int index;                  // 当前索引，在 advance/split 时修改
        int fence;                  // 最后一个索引加一
        int est;                    // 大小估计
        int expectedModCount;       // 用于并发修改检查

        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // 首次使用时初始化 fence 和大小
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // 强制初始化
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
            extends HashMapSpliterator<K,V>
            implements Spliterator<K> {
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
            extends HashMapSpliterator<K,V>
            implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K,V>
            extends HashMapSpliterator<K,V>
            implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap 支持

    /*
     * 以下包保护方法设计为由 LinkedHashMap 覆盖，但不被任何其他子类覆盖。
     * 几乎所有其他内部方法也是包保护的，但被声明为 final，因此可以由 LinkedHashMap、
     * 视图类和 HashSet 使用。
     */

    // 创建一个普通（非树）节点
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // 用于从 TreeNode 转换为普通节点
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // 创建一个树箱节点
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // 用于 treeifyBin
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * 重置为初始默认状态。由 clone 和 readObject 调用。
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // 允许 LinkedHashMap 后处理的回调
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    // 仅从 writeObject 调用，以确保兼容的排序
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // 树箱

    /**
     * 树箱的条目。扩展了 LinkedHashMap.Entry（它又扩展了 Node），
     * 因此可以用作普通节点或链接节点的扩展。
     */
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // 红黑树链接
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // 需要在删除时取消链接下一个
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }

        /**
         * 返回包含此节点的树的根。
         */
        final TreeNode<K,V> root() {
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * 确保给定的根是其箱的第一个节点。
         */
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                if (root != first) {
                    Node<K,V> rn;
                    tab[index] = root;
                    TreeNode<K,V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K,V>)rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }

        /**
         * 从根节点 p 开始，查找具有给定哈希和键的节点。
         * kc 参数在第一次使用比较键时缓存 comparableClassFor(key)。
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                        (kc = comparableClassFor(k)) != null) &&
                        (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }

        /**
         * 对根节点调用 find。
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * 当哈希码相等且不可比较时，用于排序插入的平局打破实用程序。
         * 我们不需要总顺序，只需要一个一致的插入规则，以在重新平衡时保持等价性。
         * 超出必要的平局打破稍微简化了测试。
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                    (d = a.getClass().getName().
                            compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                        -1 : 1);
            return d;
        }

        /**
         * 从此节点链接的节点形成树。
         */
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K,V> p = root;;) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                (kc = comparableClassFor(k)) == null) ||
                                (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K,V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }

        /**
         * 返回替换从此节点链接的节点的非 TreeNode 列表。
         */
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
                Node<K,V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * putVal 的树版本。
         */
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                        (kc = comparableClassFor(k)) == null) ||
                        (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                                (q = ch.find(h, k, kc)) != null) ||
                                ((ch = p.right) != null &&
                                        (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * 移除给定节点，该节点必须在此调用之前存在。
         * 这比典型的红黑删除代码更复杂，因为我们不能将内部节点的内容与由“next”指针固定的叶后继节点交换，
         * 这些指针在遍历期间可独立访问。因此，我们改为交换树链接。
         * 如果当前树似乎节点太少，则箱将转换回普通箱。（测试在 2 到 6 个节点之间触发，具体取决于树结构。）
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            int n;
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (root == null
                    || (movable
                    && (root.right == null
                    || (rl = root.left) == null
                    || rl.left == null))) {
                tab[index] = first.untreeify(map);  // 太小，转换回普通箱
                return;
            }
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K,V> s = pr, sl;
                while ((sl = s.left) != null) // 寻找后继
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // 交换颜色
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p 是 s 的直接父节点
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    (root = replacement).red = false;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // 分离
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * 将树箱中的节点拆分为低位和高位树箱，如果现在太小则取消树化。
         * 仅从 resize 调用；请参阅上面关于拆分位和索引的讨论。
         *
         * @param map 映射
         * @param tab 用于记录箱头的表
         * @param index 被拆分的表索引
         * @param bit 要拆分的哈希位
         */
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            // 重新链接到 lo 和 hi 列表，保持顺序
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else 已经树化)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // 红黑树方法，全部改编自 CLR

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                                (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                        null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                                (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                        null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * 递归不变式检查
         */
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                    tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}