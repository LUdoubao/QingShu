package org.doubao.interview.question020.demo;

import java.lang.ref.WeakReference;
import org.doubao.interview.question020.concurrent.SimpleThreadLocal;

/**
 * ThreadLocal 原理深度演示
 * 
 * 通过模拟 JDK 的 ThreadLocalMap 实现，展示：
 * 1. 每个线程独立的 Map 存储
 * 2. WeakReference 弱引用 key
 * 3. 哈希冲突的线性探测解决
 * 4. 内存泄漏的产生和预防
 */
public class ThreadLocalPrincipleDemo {
    
    /**
     * 模拟 ThreadLocalMap 的 Entry
     * 继承 WeakReference，key 是弱引用
     */
    static class Entry extends WeakReference<SimpleThreadLocal<?>> {
        Object value;
        
        public Entry(SimpleThreadLocal<?> key, Object value) {
            super(key);  // 弱引用 key
            this.value = value;  // 强引用 value
        }
    }
    
    /**
     * 简化的 ThreadLocalMap
     * 演示内部存储机制
     */
    static class SimpleThreadLocalMap {
        private Entry[] table;
        private int size = 0;
        private static final int DEFAULT_CAPACITY = 16;
        
        public SimpleThreadLocalMap() {
            table = new Entry[DEFAULT_CAPACITY];
        }
        
        /**
         * 存储值（简化版）
         */
        public void set(SimpleThreadLocal<?> key, Object value) {
            // 计算哈希位置
            int hash = System.identityHashCode(key);
            int index = hash & (table.length - 1);
            
            // 线性探测解决冲突
            while (table[index] != null) {
                SimpleThreadLocal<?> existingKey = table[index].get();
                
                // 如果 key 相同，更新 value
                if (existingKey == key) {
                    table[index].value = value;
                    return;
                }
                
                // 如果 key 已被 GC（为 null），替换它
                if (existingKey == null) {
                    table[index] = new Entry(key, value);
                    return;
                }
                
                // 继续探测下一个位置
                index = (index + 1) % table.length;
            }
            
            // 找到空位，存储
            table[index] = new Entry(key, value);
            size++;
            
            System.out.println("  [存储] key=" + key.hashCode() + ", value=" + value 
                + " -> 位置=" + index);
        }
        
        /**
         * 获取值（简化版）
         */
        public Object get(SimpleThreadLocal<?> key) {
            int hash = System.identityHashCode(key);
            int index = hash & (table.length - 1);
            
            // 线性探测查找
            while (table[index] != null) {
                SimpleThreadLocal<?> existingKey = table[index].get();
                
                if (existingKey == key) {
                    return table[index].value;
                }
                
                // 继续探测下一个位置
                index = (index + 1) % table.length;
            }
            
            return null;
        }
        
        /**
         * 删除指定 key
         */
        public void remove(SimpleThreadLocal<?> key) {
            int hash = System.identityHashCode(key);
            int index = hash & (table.length - 1);
            
            while (table[index] != null) {
                SimpleThreadLocal<?> existingKey = table[index].get();
                
                if (existingKey == key) {
                    table[index] = null;  // 清除 entry
                    size--;
                    System.out.println("  [删除] key=" + key.hashCode());
                    return;
                }
                
                index = (index + 1) % table.length;
            }
        }
        
        /**
         * 打印当前 Map 状态
         */
        public void printState() {
            System.out.println("  [Map 状态] 容量=" + table.length + ", 已用=" + size);
            for (int i = 0; i < table.length; i++) {
                if (table[i] != null) {
                    SimpleThreadLocal<?> key = table[i].get();
                    if (key != null) {
                        System.out.println("    位置" + i + ": key=" + key.hashCode() 
                            + ", value=" + table[i].value);
                    } else {
                        System.out.println("    位置" + i + ": key=null (已 GC), value=" 
                            + table[i].value + " ⚠️ 内存泄漏！");
                    }
                }
            }
        }
        
        /**
         * 清理所有 key 为 null 的 Entry
         */
        public void cleanStaleEntries() {
            int cleaned = 0;
            for (int i = 0; i < table.length; i++) {
                if (table[i] != null && table[i].get() == null) {
                    table[i].value = null;  // 帮助 GC
                    table[i] = null;
                    size--;
                    cleaned++;
                }
            }
            if (cleaned > 0) {
                System.out.println("  [清理] 清除了 " + cleaned + " 个 stale entries");
            }
        }
    }
    
    /**
     * 模拟 Thread 类
     */
    static class SimulatedThread {
        private String name;
        private SimpleThreadLocalMap threadLocals;  // 每个线程独立的 Map
        
        public SimulatedThread(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public SimpleThreadLocalMap getThreadLocals() {
            if (threadLocals == null) {
                threadLocals = new SimpleThreadLocalMap();
            }
            return threadLocals;
        }
        
        /**
         * 模拟线程结束，触发 GC
         */
        public void simulateGC() {
            System.gc();  // 触发 GC，弱引用 key 会被回收
            System.out.println("  [GC] 触发了垃圾回收，弱引用 key 被清理");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   ThreadLocal 原理深度演示             ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // ========== 演示 1：每个线程独立的 Map ==========
        testIndependentMaps();
        
        Thread.sleep(500);
        
        // ========== 演示 2：弱引用和内存泄漏 ==========
        testWeakReferenceAndMemoryLeak();
        
        Thread.sleep(500);
        
        // ========== 演示 3：哈希冲突的线性探测 ==========
        testLinearProbing();
    }
    
    /**
     * 演示每个线程有独立的 Map
     */
    private static void testIndependentMaps() {
        System.out.println("【演示 1】每个线程独立的 Map");
        System.out.println("──────────────────────────────────────");
        
        // 创建两个线程
        SimulatedThread thread1 = new SimulatedThread("Thread-1");
        SimulatedThread thread2 = new SimulatedThread("Thread-2");
        
        // 创建两个 ThreadLocal
        SimpleThreadLocal<String> tl1 = new SimpleThreadLocal<>();
        SimpleThreadLocal<String> tl2 = new SimpleThreadLocal<>();
        
        // Thread-1 存储数据
        System.out.println("\n-- Thread-1 存储数据 --");
        thread1.getThreadLocals().set(tl1, "Thread1-Value1");
        thread1.getThreadLocals().set(tl2, "Thread1-Value2");
        thread1.getThreadLocals().printState();
        
        // Thread-2 存储不同的数据
        System.out.println("\n-- Thread-2 存储数据 --");
        thread2.getThreadLocals().set(tl1, "Thread2-Value1");
        thread2.getThreadLocals().set(tl2, "Thread2-Value2");
        thread2.getThreadLocals().printState();
        
        // 验证隔离性
        System.out.println("\n-- 验证线程隔离 --");
        System.out.println("Thread-1 读取 tl1: " + thread1.getThreadLocals().get(tl1));
        System.out.println("Thread-2 读取 tl1: " + thread2.getThreadLocals().get(tl1));
        System.out.println("✓ 两个线程的数据完全独立\n");
    }
    
    /**
     * 演示弱引用和内存泄漏
     */
    private static void testWeakReferenceAndMemoryLeak() {
        System.out.println("【演示 2】弱引用和内存泄漏");
        System.out.println("──────────────────────────────────────");
        
        SimulatedThread thread = new SimulatedThread("Leak-Thread");
        
        // 创建 ThreadLocal 并存储数据
        System.out.println("\n-- 创建 ThreadLocal 并存储 --");
        SimpleThreadLocal<String> tl = new SimpleThreadLocal<>();
        int tlHashCode = tl.hashCode();
        thread.getThreadLocals().set(tl, "Important Data");
        thread.getThreadLocals().printState();
        
        // 模拟 ThreadLocal 对象被 GC 回收
        System.out.println("\n-- 模拟 ThreadLocal 对象被 GC --");
        tl = null;  // 断开强引用
        thread.simulateGC();
        
        // 查看 Map 状态
        System.out.println("\n-- GC 后的 Map 状态 --");
        thread.getThreadLocals().printState();
        
        // 演示正确的清理方式
        System.out.println("\n-- 正确做法：使用 remove() 清理 --");
        System.out.println("在真实使用中，应该在 finally 块中调用 remove():");
        System.out.println("  try {");
        System.out.println("      threadLocal.set(value);");
        System.out.println("      // 业务逻辑");
        System.out.println("  } finally {");
        System.out.println("      threadLocal.remove();  // 防止内存泄漏");
        System.out.println("  }\n");
    }
    
    /**
     * 演示线性探测解决哈希冲突
     */
    private static void testLinearProbing() {
        System.out.println("【演示 3】线性探测解决哈希冲突");
        System.out.println("──────────────────────────────────────");
        
        SimulatedThread thread = new SimulatedThread("Probe-Thread");
        
        // 创建多个 ThreadLocal，它们可能有相同的哈希位置
        System.out.println("\n-- 连续存储多个 ThreadLocal --");
        for (int i = 0; i < 5; i++) {
            SimpleThreadLocal<String> tl = new SimpleThreadLocal<>();
            thread.getThreadLocals().set(tl, "Value-" + i);
        }
        
        System.out.println("\n-- 最终 Map 状态 --");
        thread.getThreadLocals().printState();
        
        System.out.println("\n说明：");
        System.out.println("- 当哈希冲突时，使用线性探测找下一个空位");
        System.out.println("- 这就是为什么 ThreadLocal 建议使用奇数 hashCode");
        System.out.println("- JDK 通过 THREAD_LOCAL_HASH_CODE * 0x61c88647 来分散哈希值\n");
    }
}
