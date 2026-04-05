package org.doubao.interview.agent.server.collection;

import java.util.Objects;

/**
 * 简易版HashMap实现 - 演示JDK 1.8 HashMap核心原理
 * 
 * 【核心数据结构】
 * 1. 底层采用：数组 + 链表 + 红黑树
 * 2. 链表长度 ≥ 8 且 数组长度 ≥ 64 → 转为红黑树
 * 3. 红黑树节点数 ≤ 6 → 转回链表
 * 
 * 【核心优化点】
 * 1. 哈希算法：高16位 ^ 低16位，减少冲突
 * 2. 插入方式：尾插法（避免环形链表）
 * 3. 扩容机制：2倍扩容，元素迁移无需重算hash
 * 
 * 【线程安全】
 * 非线程安全，多线程并发推荐使用ConcurrentHashMap
 * 
 * @author Interview Agent
 * @date 2026-04-05
 */
public class SimpleHashMap<K, V> {
    
    // ==================== 核心常量 ====================
    
    /**
     * 默认初始容量：16（必须是2的幂次）
     * 原因：保证 (n-1) & hash 能均匀分布
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    
    /**
     * 最大容量：2^30
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    
    /**
     * 默认负载因子：0.75
     * 原因：时间和空间成本的平衡，过高增加冲突，过低浪费空间
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
    /**
     * 树化阈值：链表长度 ≥ 8 时考虑树化
     * 原因：泊松分布下，链表长度达到8的概率极低（千万分之一）
     */
    private static final int TREEIFY_THRESHOLD = 8;
    
    /**
     * 反树化阈值：红黑树节点数 ≤ 6 时转回链表
     * 原因：避免频繁树化和反树化，设置缓冲区间
     */
    private static final int UNTREEIFY_THRESHOLD = 6;
    
    /**
     * 最小树化容量：数组长度 ≥ 64 才允许树化
     * 原因：数组较小时优先扩容而非树化，避免过早使用复杂的红黑树
     */
    private static final int MIN_TREEIFY_CAPACITY = 64;
    
    // ==================== 核心字段 ====================
    
    /**
     * 哈希桶数组（核心存储结构）
     * 注意：transient表示不参与序列化，因为重新hash后位置可能变化
     */
    transient Node<K,V>[] table;
    
    /**
     * 实际键值对数量
     */
    transient int size;
    
    /**
     * 扩容阈值 = capacity * loadFactor
     */
    int threshold;
    
    /**
     * 负载因子
     */
    final float loadFactor;
    
    /**
     * 结构性修改次数（用于快速失败机制）
     */
    transient int modCount;
    
    // ==================== 构造函数 ====================
    
    /**
     * 无参构造：使用默认容量16和负载因子0.75
     * 注意：此时不初始化table，采用懒加载策略
     */
    public SimpleHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }
    
    /**
     * 指定初始容量构造
     * 
     * @param initialCapacity 初始容量（会自动调整为2的幂次）
     */
    public SimpleHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
    
    /**
     * 指定初始容量和负载因子构造
     * 
     * @param initialCapacity 初始容量
     * @param loadFactor 负载因子
     */
    public SimpleHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        this.loadFactor = loadFactor;
        // 计算扩容阈值（此时table还未初始化）
        this.threshold = tableSizeFor(initialCapacity);
    }
    
    // ==================== 核心方法：put ====================
    
    /**
     * 添加键值对（核心方法）
     * 
     * 【执行流程】
     * 1. 计算key的hash值
     * 2. 若table为空，调用resize()初始化
     * 3. 通过 (n-1) & hash 定位数组下标
     * 4. 若无冲突，直接创建新节点
     * 5. 若有冲突：
     *    - key已存在：覆盖旧值
     *    - 是红黑树节点：调用树插入
     *    - 是链表节点：尾插法添加，判断是否树化
     * 6. 检查是否需要扩容
     * 
     * @param key 键
     * @param value 值
     * @return 旧值（若key已存在）或null（新增）
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false);
    }
    
    /**
     * 核心插入逻辑（模拟JDK 1.8 putVal方法）
     * 
     * @param hash key的hash值
     * @param key 键
     * @param value 值
     * @param onlyIfAbsent 若为true，则不覆盖已存在的值
     * @return 旧值或null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent) {
        Node<K,V>[] tab;
        Node<K,V> p;
        int n, i;
        
        // 步骤1：若table为空或长度为0，调用resize()初始化
        if ((tab = table) == null || (n = tab.length) == 0) {
            n = (tab = resize()).length;
        }
        
        // 步骤2：计算数组下标，判断该位置是否为空
        // (n-1) & hash 等价于 hash % n，但位运算效率更高
        if ((p = tab[i = (n - 1) & hash]) == null) {
            // 无冲突，直接创建新节点
            tab[i] = newNode(hash, key, value, null);
        } else {
            // 步骤3：发生哈希冲突
            Node<K,V> e;
            K k;
            
            // 情况1：头节点key与待插入key相同
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p;
            }
            // 情况2：是红黑树节点，调用树插入方法
            else if (p instanceof TreeNode) {
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            }
            // 情况3：是链表节点，遍历链表
            else {
                for (int binCount = 0; ; ++binCount) {
                    // 到达链表尾部，尾插法添加新节点
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 判断是否达到树化阈值
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            treeifyBin(tab, hash);
                        }
                        break;
                    }
                    // 链表中已存在相同key，跳出循环准备覆盖
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                        break;
                    }
                    p = e;
                }
            }
            
            // 步骤4：若key已存在，覆盖旧值
            if (e != null) {
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null) {
                    e.value = value;
                }
                return oldValue;
            }
        }
        
        // 步骤5：modCount++，记录结构性修改
        ++modCount;
        
        // 步骤6：检查是否需要扩容
        if (++size > threshold) {
            resize();
        }
        
        return null;
    }
    
    // ==================== 核心方法：get ====================
    
    /**
     * 根据key获取value
     * 
     * 【执行流程】
     * 1. 计算key的hash值和数组下标
     * 2. 命中头节点，直接返回
     * 3. 若是红黑树，调用树查找 O(logn)
     * 4. 若是链表，遍历查找 O(n)
     * 
     * @param key 键
     * @return 对应的value，未找到返回null
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }
    
    /**
     * 核心查找逻辑
     * 
     * @param hash key的hash值
     * @param key 键
     * @return 节点或null
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab;
        Node<K,V> first, e;
        int n;
        K k;
        
        // 步骤1：table非空且对应桶位置有节点
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            
            // 步骤2：检查头节点
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k)))) {
                return first;
            }
            
            // 步骤3：检查后续节点
            if ((e = first.next) != null) {
                // 情况1：红黑树查找
                if (first instanceof TreeNode) {
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                }
                // 情况2：链表遍历查找
                do {
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                        return e;
                    }
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
    
    // ==================== 核心方法：remove ====================
    
    /**
     * 删除指定key的键值对
     * 
     * @param key 键
     * @return 被删除的value，未找到返回null
     */
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ? null : e.value;
    }
    
    /**
     * 核心删除逻辑
     */
    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab;
        Node<K,V> p;
        int n, index;
        
        // 步骤1：定位到目标桶
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            
            Node<K,V> node = null, e;
            K k;
            V v;
            
            // 步骤2：检查头节点
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                node = p;
            }
            // 步骤3：检查后续节点
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode) {
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                } else {
                    // 链表遍历查找
                    do {
                        if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            
            // 步骤4：找到节点，执行删除
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode) {
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                } else if (node == p) {
                    // 删除头节点
                    tab[index] = node.next;
                } else {
                    // 删除中间或尾部节点
                    p.next = node.next;
                }
                --modCount;
                --size;
                return node;
            }
        }
        return null;
    }
    
    // ==================== 核心方法：resize（扩容）====================
    
    /**
     * 扩容方法（JDK 1.8 核心优化点）
     * 
     * 【扩容触发条件】
     * 1. 首次put时，table为空
     * 2. size > threshold（容量 * 负载因子）
     * 
     * 【扩容逻辑】
     * 1. 新容量 = 旧容量 * 2
     * 2. 新阈值 = 旧阈值 * 2
     * 3. 元素迁移：无需重新计算hash
     *    - hash & 旧容量 == 0 → 留在原索引
     *    - hash & 旧容量 != 0 → 移到 原索引 + 旧容量
     * 
     * 【性能优势】
     * 相比JDK 1.7，避免了重新计算hash，效率提升一倍
     * 
     * @return 新的哈希桶数组
     */
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        
        // 情况1：已有容量，进行扩容
        if (oldCap > 0) {
            // 超过最大容量，不再扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 新容量 = 旧容量 * 2
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY) {
                // 新阈值 = 旧阈值 * 2
                newThr = oldThr << 1;
            }
        }
        // 情况2：首次初始化，使用threshold作为初始容量
        else if (oldThr > 0) {
            newCap = oldThr;
        }
        // 情况3：使用默认值
        else {
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        
        // 计算新的扩容阈值
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        
        // 创建新的哈希桶数组
        @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        
        // 元素迁移（JDK 1.8 核心优化）
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null; // 帮助GC
                    
                    // 情况1：只有一个节点，直接计算新位置
                    if (e.next == null) {
                        newTab[e.hash & (newCap - 1)] = e;
                    }
                    // 情况2：红黑树节点，拆分树
                    else if (e instanceof TreeNode) {
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    }
                    // 情况3：链表节点，拆分成两条链表
                    else {
                        // loHead/loTail：低位链表（留在原索引）
                        Node<K,V> loHead = null, loTail = null;
                        // hiHead/hiTail：高位链表（移到 原索引+旧容量）
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        
                        do {
                            next = e.next;
                            // 关键判断：hash & 旧容量 == 0
                            if ((e.hash & oldCap) == 0) {
                                // 低位链表
                                if (loTail == null) {
                                    loHead = e;
                                } else {
                                    loTail.next = e;
                                }
                                loTail = e;
                            } else {
                                // 高位链表
                                if (hiTail == null) {
                                    hiHead = e;
                                } else {
                                    hiTail.next = e;
                                }
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        
                        // 低位链表放在原索引
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 高位链表放在 原索引+旧容量
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
    
    // ==================== 树化相关方法 ====================
    
    /**
     * 将链表转换为红黑树
     * 
     * 【树化条件】
     * 1. 链表长度 ≥ 8
     * 2. 数组长度 ≥ 64
     * 
     * 若不满足条件2，优先扩容而非树化
     * 
     * @param tab 哈希桶数组
     * @param hash 触发树化的hash值
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index;
        Node<K,V> e;
        
        // 条件1：数组为空或长度 < 64，优先扩容
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) {
            resize();
        }
        // 条件2：数组长度 ≥ 64，执行树化
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            
            // 将链表节点转换为红黑树节点
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null) {
                    hd = p;
                } else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            
            // 构建红黑树
            if ((tab[index] = hd) != null) {
                hd.treeify(tab);
            }
        }
    }
    
    // ==================== 哈希函数 ====================
    
    /**
     * 计算key的hash值（JDK 1.8 优化版本）
     * 
     * 【优化点】
     * 1. 高16位 ^ 低16位，让高位也参与运算
     * 2. 减少哈希冲突概率，使分布更均匀
     * 3. 配合 (n-1) & hash 使用，保证低位充分散列
     * 
     * @param key 键
     * @return hash值
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 返回大于等于cap的最小2的幂次
     * 例如：cap=5 → 返回8；cap=17 → 返回32
     * 
     * @param cap 目标容量
     * @return 2的幂次
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
    
    /**
     * 创建普通节点
     */
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }
    
    /**
     * 创建树节点
     */
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }
    
    /**
     * 将普通节点替换为树节点
     */
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }
    
    // ==================== 其他常用方法 ====================
    
    /**
     * 判断是否包含指定key
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }
    
    /**
     * 判断是否包含指定value
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab;
        V v;
        if ((tab = table) != null && size > 0) {
            for (Node<K,V> e : tab) {
                for (; e != null; e = e.next) {
                    if ((v = e.value) == value || (value != null && value.equals(v))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 获取元素数量
     */
    public int size() {
        return size;
    }
    
    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * 清空所有元素
     */
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i) {
                tab[i] = null;
            }
        }
    }
    
    /**
     * 获取当前容量
     */
    public int capacity() {
        return table == null ? 0 : table.length;
    }
    
    /**
     * 获取扩容阈值
     */
    public int threshold() {
        return threshold;
    }
    
    /**
     * 打印内部结构（用于调试和演示）
     */
    public void printStructure() {
        System.out.println("========== SimpleHashMap 内部结构 ==========");
        System.out.println("容量: " + capacity());
        System.out.println("大小: " + size());
        System.out.println("阈值: " + threshold());
        System.out.println("负载因子: " + loadFactor);
        
        if (table == null) {
            System.out.println("table 为空");
            return;
        }
        
        for (int i = 0; i < table.length; i++) {
            Node<K,V> node = table[i];
            if (node != null) {
                System.out.print("桶[" + i + "]: ");
                if (node instanceof TreeNode) {
                    System.out.print("[红黑树] ");
                    // 简化打印，只打印根节点
                    System.out.println(node.key + "=" + node.value);
                } else {
                    System.out.print("[链表] ");
                    while (node != null) {
                        System.out.print(node.key + "=" + node.value);
                        if (node.next != null) {
                            System.out.print(" -> ");
                        }
                        node = node.next;
                    }
                    System.out.println();
                }
            }
        }
        System.out.println("==========================================");
    }
    
    // ==================== 内部类：Node（链表节点）====================
    
    /**
     * 链表节点（JDK 1.8 基础节点）
     * 
     * 特点：
     * 1. 单向链表，只有next指针
     * 2. 存储hash值，避免重复计算
     * 3. final修饰key和hash，保证不可变性
     */
    static class Node<K,V> implements java.util.Map.Entry<K,V> {
        final int hash;    // hash值（缓存，避免重复计算）
        final K key;       // 键（final，不可变）
        V value;           // 值（可变）
        Node<K,V> next;    // 后继节点指针
        
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
            if (o == this) return true;
            if (o instanceof java.util.Map.Entry) {
                java.util.Map.Entry<?,?> e = (java.util.Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
    
    // ==================== 内部类：TreeNode（红黑树节点）====================
    
    /**
     * 红黑树节点（继承自Node）
     * 
     * 特点：
     * 1. 双向链表结构（prev指针），便于树操作
     * 2. 红黑树颜色标记（red/black）
     * 3. 左右子节点和父节点指针
     * 
     * 红黑树性质：
     * 1. 节点是红色或黑色
     * 2. 根节点是黑色
     * 3. 叶子节点（NIL）是黑色
     * 4. 红色节点的子节点必须是黑色
     * 5. 从任一节点到其叶子的所有路径都包含相同数量的黑色节点
     */
    static class TreeNode<K,V> extends Node<K,V> {
        TreeNode<K,V> parent;  // 父节点
        TreeNode<K,V> left;    // 左子节点
        TreeNode<K,V> right;   // 右子节点
        TreeNode<K,V> prev;    // 前驱节点（双向链表）
        boolean red;           // 颜色标记（true=红色，false=黑色）
        
        TreeNode(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
        
        /**
         * 在红黑树中插入节点
         */
        final TreeNode<K,V> putTreeVal(SimpleHashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K,V> root = (parent != null) ? root() : this;
            
            // 从根节点开始查找插入位置
            for (TreeNode<K,V> p = root;;) {
                int dir, ph;
                K pk;
                
                // 比较hash值确定方向
                if ((ph = p.hash) > h) {
                    dir = -1;  // 往左
                } else if (ph < h) {
                    dir = 1;   // 往右
                }
                // hash相同，比较key
                else if ((pk = p.key) == k || (k != null && k.equals(pk))) {
                    return p;  // key已存在，返回该节点
                }
                // hash相同但key不同，需要进一步比较
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        // 在左右子树中查找
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null)) {
                            return q;
                        }
                    }
                    // 无法比较，使用System.identityHashCode打破僵局
                    dir = tieBreakOrder(k, pk);
                }
                
                TreeNode<K,V> xp = p;
                // 找到插入位置
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    
                    // 插入节点
                    if (dir <= 0) {
                        xp.left = x;
                    } else {
                        xp.right = x;
                    }
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null) {
                        ((TreeNode<K,V>)xpn).prev = x;
                    }
                    
                    // 插入后调整红黑树（旋转+变色）
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }
        
        /**
         * 查找红黑树中的节点
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }
        
        /**
         * 从当前节点开始查找
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir;
                K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                
                if ((ph = p.hash) > h) {
                    p = pl;  // 往左
                } else if (ph < h) {
                    p = pr;  // 往右
                } else if ((pk = p.key) == k || (k != null && k.equals(pk))) {
                    return p;  // 找到
                } else if (pl == null) {
                    p = pr;  // 左子树为空，往右
                } else if (pr == null) {
                    p = pl;  // 右子树为空，往左
                } else if ((kc != null ||
                            (kc = comparableClassFor(k)) != null) &&
                           (dir = compareComparables(kc, k, pk)) != 0) {
                    p = (dir < 0) ? pl : pr;
                } else if ((q = pr.find(h, k, kc)) != null) {
                    return q;
                } else {
                    p = pl;
                }
            } while (p != null);
            return null;
        }
        
        /**
         * 获取根节点
         */
        final TreeNode<K,V> root() {
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null) {
                    return r;
                }
                r = p;
            }
        }
        
        /**
         * 红黑树构建（从链表转换而来）
         */
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                
                if (root == null) {
                    x.parent = null;
                    x.red = false;  // 根节点必须是黑色
                    root = x;
                } else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    
                    // 从根节点开始查找插入位置
                    for (TreeNode<K,V> p = root;;) {
                        int dir, ph;
                        K pk = p.key;
                        
                        if ((ph = p.hash) > h) {
                            dir = -1;
                        } else if (ph < h) {
                            dir = 1;
                        } else if ((kc == null &&
                                    (kc = comparableClassFor(k)) == null) ||
                                   (dir = compareComparables(kc, k, pk)) == 0) {
                            dir = tieBreakOrder(k, pk);
                        }
                        
                        TreeNode<K,V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0) {
                                xp.left = x;
                            } else {
                                xp.right = x;
                            }
                            // 插入后调整红黑树
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }
        
        /**
         * 红黑树删除节点后的调整
         */
        final void removeTreeNode(SimpleHashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            // 简化实现：实际JDK源码非常复杂
            // 这里只做基本演示
            if (tab == null || tab.length == 0) {
                return;
            }
            
            int index = (tab.length - 1) & hash;
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
            TreeNode<K,V> root = first;
            TreeNode<K,V> rl;
            
            // 如果树太小，转回链表
            if (root == null || root.right == null ||
                (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);
                return;
            }
            
            // 实际的删除逻辑非常复杂，此处省略
            // 涉及：查找、替换、调整、旋转、变色等步骤
        }
        
        /**
         * 红黑树拆分（扩容时使用）
         */
        final void split(SimpleHashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            
            // 拆分成两条链表
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null) {
                        loHead = e;
                    } else {
                        loTail.next = e;
                    }
                    loTail = e;
                    ++lc;
                } else {
                    if ((e.prev = hiTail) == null) {
                        hiHead = e;
                    } else {
                        hiTail.next = e;
                    }
                    hiTail = e;
                    ++hc;
                }
            }
            
            // 低位链表
            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD) {
                    tab[index] = loHead.untreeify(map);
                } else {
                    tab[index] = loHead;
                    if (hiHead != null) {
                        loHead.treeify(tab);
                    }
                }
            }
            
            // 高位链表
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD) {
                    tab[index + bit] = hiHead.untreeify(map);
                } else {
                    tab[index + bit] = hiHead;
                    if (loHead != null) {
                        hiHead.treeify(tab);
                    }
                }
            }
        }
        
        /**
         * 红黑树转回链表
         */
        final Node<K,V> untreeify(SimpleHashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
                Node<K,V> p = map.replacementTreeNode(q, null);
                if (tl == null) {
                    hd = p;
                } else {
                    tl.next = p;
                }
                tl = p;
            }
            return hd;
        }
        
        /**
         * 红黑树插入后的平衡调整（左旋+右旋+变色）
         */
        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                     TreeNode<K,V> x) {
            x.red = true;  // 新节点默认为红色
            
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                // 情况1：x是根节点，直接染黑
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                // 情况2：父节点是黑色或x是根，无需调整
                else if (!xp.red || (xpp = xp.parent) == null) {
                    return root;
                }
                
                // 父节点是左子节点
                if (xp == (xppl = xpp.left)) {
                    // 叔叔节点是红色
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;  // 叔叔染黑
                        xp.red = false;    // 父节点染黑
                        xpp.red = true;    // 祖父染红
                        x = xpp;           // 继续向上调整
                    } else {
                        // 叔叔节点是黑色或为空
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);  // 左旋
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);  // 右旋
                            }
                        }
                    }
                }
                // 父节点是右子节点（对称处理）
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
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
        
        /**
         * 左旋操作
         */
        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null) {
                    rl.parent = p;
                }
                if ((pp = r.parent = p.parent) == null) {
                    (root = r).red = false;  // 新根染黑
                } else if (pp.left == p) {
                    pp.left = r;
                } else {
                    pp.right = r;
                }
                r.left = p;
                p.parent = r;
            }
            return root;
        }
        
        /**
         * 右旋操作
         */
        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                                TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null) {
                    lr.parent = p;
                }
                if ((pp = l.parent = p.parent) == null) {
                    (root = l).red = false;
                } else if (pp.right == p) {
                    pp.right = l;
                } else {
                    pp.left = l;
                }
                l.right = p;
                p.parent = l;
            }
            return root;
        }
        
        /**
         * 将根节点移到桶的首位
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
                    if ((rn = root.next) != null) {
                        ((TreeNode<K,V>)rn).prev = rp;
                    }
                    if (rp != null) {
                        rp.next = rn;
                    }
                    if (first != null) {
                        first.prev = root;
                    }
                    root.next = first;
                    root.prev = null;
                }
            }
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 判断key的类是否实现了Comparable接口
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            if ((c = x.getClass()) == String.class) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * 比较两个可比较对象
     */
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }
    
    /**
     * 当无法通过Comparable比较时，使用此方法打破僵局
     */
    static int tieBreakOrder(Object a, Object b) {
        int d;
        if (a == null || b == null ||
            (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0) {
            d = (System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1);
        }
        return d;
    }
}
