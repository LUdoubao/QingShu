package org.doubao.interview.mysql.q002.index;

import java.util.*;

/**
 * Hash 索引简化实现（用于对比演示）
 * <p>
 * 核心特点：
 * 1. 基于 HashMap 实现
 * 2. O(1) 时间复杂度查询
 * 3. 只支持等值查询，不支持范围查询
 * <p>
 * 缺点：
 * - 不支持范围查询
 * - 不支持有序遍历
 * - 存在哈希冲突问题
 */
public class HashIndex {
    
    /**
     * Hash 桶节点
     */
    static class Bucket {
        int key;
        String value;
        Bucket next;
        
        public Bucket(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    
    /** Hash 表数组 */
    private Bucket[] table;
    
    /** 默认容量 */
    private static final int DEFAULT_CAPACITY = 16;
    
    /** 负载因子 */
    private static final float LOAD_FACTOR = 0.75f;
    
    /** 当前元素个数 */
    private int size = 0;
    
    /** 查询次数统计 */
    private int queryCount = 0;
    
    @SuppressWarnings("unchecked")
    public HashIndex() {
        table = new Bucket[DEFAULT_CAPACITY];
    }
    
    /**
     * 插入 key-value 对
     */
    public void insert(int key, String value) {
        System.out.println("[Hash 索引] 插入 key=" + key + ", value=" + value);
        
        int index = hash(key);
        Bucket current = table[index];
        
        // 检查是否已存在
        while (current != null) {
            if (current.key == key) {
                current.value = value;
                System.out.println("  [更新] key=" + key + " (hash=" + index + ")");
                return;
            }
            current = current.next;
        }
        
        // 头插法
        Bucket newBucket = new Bucket(key, value);
        newBucket.next = table[index];
        table[index] = newBucket;
        size++;
        
        System.out.println("  -> 插入成功 (hash=" + index + ", 链表长度=" + getChainLength(index) + ")");
        
        // 检查是否需要扩容
        if ((float) size / table.length > LOAD_FACTOR) {
            resize();
        }
    }
    
    /**
     * 查询 key
     */
    public String get(int key) {
        queryCount++;
        int index = hash(key);
        Bucket current = table[index];
        int depth = 0;
        
        while (current != null) {
            depth++;
            if (current.key == key) {
                System.out.println("[查询] key=" + key + " -> 找到 (hash=" + index + ", 深度=" + depth + ")");
                return current.value;
            }
            current = current.next;
        }
        
        System.out.println("[查询] key=" + key + " -> 未找到 (hash=" + index + ")");
        return null;
    }
    
    /**
     * 范围查询（不支持，需要全表扫描）
     */
    public List<String> rangeQuery(int startKey, int endKey) {
        System.out.println("[范围查询] [" + startKey + ", " + endKey + "] ⚠️ 需要全表扫描");
        
        List<String> result = new ArrayList<>();
        int scanned = 0;
        
        for (int i = 0; i < table.length; i++) {
            Bucket current = table[i];
            while (current != null) {
                scanned++;
                if (current.key >= startKey && current.key <= endKey) {
                    result.add(current.value);
                    System.out.print(current.key + " ");
                }
                current = current.next;
            }
        }
        
        System.out.println("(扫描了 " + scanned + " 个元素)");
        return result;
    }
    
    /**
     * 有序遍历（不支持，需要先收集再排序）
     */
    public void orderedTraversal() {
        System.out.println("[有序遍历] ⚠️ 需要先收集所有 key 并排序");
        
        List<Integer> keys = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            Bucket current = table[i];
            while (current != null) {
                keys.add(current.key);
                current = current.next;
            }
        }
        
        Collections.sort(keys);
        System.out.print("[排序后] ");
        for (int key : keys) {
            System.out.print(key + " ");
        }
        System.out.println();
    }
    
    /**
     * Hash 函数
     */
    private int hash(int key) {
        // 简化版 hash 函数
        return (key & 0x7FFFFFFF) % table.length;
    }
    
    /**
     * 扩容
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        System.out.println("  [扩容] 从 " + table.length + " 扩容到 " + (table.length * 2));
        
        Bucket[] oldTable = table;
        table = new Bucket[table.length * 2];
        size = 0;
        
        // 重新 hash 所有元素
        for (int i = 0; i < oldTable.length; i++) {
            Bucket current = oldTable[i];
            while (current != null) {
                Bucket next = current.next;
                current.next = table[hash(current.key)];
                table[hash(current.key)] = current;
                size++;
                current = next;
            }
        }
    }
    
    /**
     * 获取链表长度
     */
    private int getChainLength(int index) {
        int length = 0;
        Bucket current = table[index];
        while (current != null) {
            length++;
            current = current.next;
        }
        return length;
    }
    
    /**
     * 打印 Hash 索引结构
     */
    public void printTable() {
        System.out.println("\n========== Hash 索引结构 ==========");
        System.out.println("容量：" + table.length + ", 元素数：" + size);
        
        for (int i = 0; i < table.length; i++) {
            System.out.print("Bucket[" + i + "]: ");
            Bucket current = table[i];
            while (current != null) {
                System.out.print("(" + current.key + "," + current.value + ") -> ");
                current = current.next;
            }
            System.out.println("null");
        }
        System.out.println("================================\n");
    }
    
    /**
     * 获取查询次数
     */
    public int getQueryCount() {
        return queryCount;
    }
}
