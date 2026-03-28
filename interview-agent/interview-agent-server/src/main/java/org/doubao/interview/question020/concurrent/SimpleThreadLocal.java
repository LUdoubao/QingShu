package org.doubao.interview.question020.concurrent;

/**
 * ThreadLocal（线程本地变量）简化实现
 * 
 * 核心功能：
 * 1. 为每个线程维护独立的变量副本
 * 2. 避免多线程共享变量的冲突
 * 3. 本质是"以空间换隔离"
 * 
 * 应用场景：
 * - 用户上下文信息（UserContext）
 * - 分布式追踪 ID（traceId）
 * - 数据库连接/会话管理
 * - SimpleDateFormat 线程安全
 * 
 * 【原理解析】
 * 
 * 1. 数据结构：
 *    - 每个 Thread 对象内部有一个 ThreadLocalMap 类型的成员变量 threadLocals
 *    - ThreadLocalMap 内部维护了一个 Entry[] 数组作为哈希表
 *    - Entry 继承自 WeakReference，key 是 ThreadLocal 实例（弱引用），value 是存储的值（强引用）
 * 
 * 2. 存储过程（set）：
 *    - 获取当前线程 t = Thread.currentThread()
 *    - 获取 t 的 threadLocals 映射表
 *    - 如果映射表不存在，创建新的 ThreadLocalMap
 *    - 如果存在，以当前 ThreadLocal 为 key 存储值
 *    - 使用开放寻址法解决哈希冲突
 * 
 * 3. 获取过程（get）：
 *    - 获取当前线程 t
 *    - 获取 t 的 threadLocals 映射表
 *    - 如果映射表存在，以当前 ThreadLocal 为 key 查找值
 *    - 如果找不到，返回 null
 * 
 * 4. 删除过程（remove）：
 *    - 获取当前线程的 threadLocals 映射表
 *    - 从映射表中移除当前 ThreadLocal 对应的 Entry
 *    - 重要：防止内存泄漏，必须手动调用
 * 
 * 5. 内存泄漏问题：
 *    - key 是弱引用，GC 时会被回收，变为 null
 *    - value 是强引用，只要线程存活就不会被回收
 *    - 如果 ThreadLocal 被回收但线程仍在运行（如线程池），value 无法回收
 *    - 解决：使用完必须调用 remove()
 * 
 * 6. 哈希冲突解决：
 *    - 使用线性探测法（开放寻址）
 *    - 计算 hash = threadLocalHashCode & (n-1)
 *    - 如果位置 i 被占用，检查 i+1, i+2...直到找到空位
 *    - 每个 ThreadLocal 的 hashCode 通过 THREAD_LOCAL_HASH_CODE 递增生成
 */
public class SimpleThreadLocal<T> {
    
    /**
     * 内部静态类，表示线程变量条目
     * 实际 JDK 中使用 ThreadLocalMap 实现
     */
    private static class ThreadLocalEntry<T> {
        /** 线程本地变量的值 */
        T value;
        
        public ThreadLocalEntry(T value) {
            this.value = value;
        }
    }
    
    /**
     * 使用 ThreadLocal 来存储每个线程的独立副本
     * 这里简化为直接使用 JDK 的 ThreadLocal 机制
     */
    private final java.lang.ThreadLocal<ThreadLocalEntry<T>> threadLocal = 
        new java.lang.ThreadLocal<ThreadLocalEntry<T>>() {
            @Override
            protected ThreadLocalEntry<T> initialValue() {
                return null;  // 初始值为 null
            }
        };
    
    /**
     * 获取当前线程的变量副本
     * @return 变量值，如果未设置则返回 null
     */
    public T get() {
        ThreadLocalEntry<T> entry = threadLocal.get();
        return entry != null ? entry.value : null;
    }
    
    /**
     * 设置当前线程的变量值
     * @param value 要设置的值
     */
    public void set(T value) {
        threadLocal.set(new ThreadLocalEntry<>(value));
    }
    
    /**
     * 移除当前线程的变量
     * 重要：使用完后必须调用，防止内存泄漏
     */
    public void remove() {
        threadLocal.remove();
    }
    
    /**
     * 获取当前线程的变量值，如果为 null 则设置初始值
     * @param initialValue 初始值
     * @return 变量值
     */
    public T getOrDefault(T initialValue) {
        T value = get();
        if (value == null) {
            set(initialValue);
            value = initialValue;
        }
        return value;
    }
    
    /**
     * 检查当前线程是否已设置变量
     * @return true 表示已设置
     */
    public boolean isSet() {
        return get() != null;
    }
}
