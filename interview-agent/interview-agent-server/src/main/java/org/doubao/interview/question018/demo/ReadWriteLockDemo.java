package org.doubao.interview.question018.demo;

import org.doubao.interview.question018.concurrent.SimpleReadWriteLock;

/**
 * ReadWriteLock 应用场景演示
 * 
 * 场景 1：缓存系统（读多写少）
 * 场景 2：配置管理器
 * 场景 3：共享数据字典
 */
public class ReadWriteLockDemo {
    
    /**
     * 场景 1：缓存系统
     * 支持并发读取，互斥写入
     */
    static class Cache<K, V> {
        private final java.util.Map<K, V> cache = new java.util.HashMap<>();
        private final SimpleReadWriteLock lock = new SimpleReadWriteLock();
        
        /**
         * 读取缓存
         */
        public V get(K key) {
            try {
                lock.readLock().lock();
                System.out.println("[读] 线程-" + Thread.currentThread().getName() 
                    + " 读取 key=" + key + ", 当前并发读数：" + lock.getReaders());
                
                // 模拟读取耗时
                Thread.sleep(500);
                
                return cache.get(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        /**
         * 写入缓存
         */
        public void put(K key, V value) {
            try {
                lock.writeLock().lock();
                System.out.println("[写] 线程-" + Thread.currentThread().getName() 
                    + " 写入 key=" + key + ", value=" + value);
                
                // 模拟写入耗时
                Thread.sleep(1000);
                
                cache.put(key, value);
                System.out.println("[写] ✓ 完成写入 key=" + key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        /**
         * 批量读取
         */
        public java.util.List<V> getAll(java.util.List<K> keys) {
            java.util.List<V> results = new java.util.ArrayList<>();
            try {
                lock.readLock().lock();
                for (K key : keys) {
                    results.add(cache.get(key));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.readLock().unlock();
            }
            return results;
        }
    }
    
    /**
     * 场景 2：配置管理器
     * 频繁读取配置，偶尔更新
     */
    static class ConfigManager {
        private final java.util.Map<String, String> config = new java.util.HashMap<>();
        private final SimpleReadWriteLock lock = new SimpleReadWriteLock();
        
        public ConfigManager() {
            // 初始化配置
            config.put("timeout", "3000");
            config.put("maxConnections", "100");
            config.put("retryCount", "3");
        }
        
        /**
         * 获取配置项
         */
        public String getConfig(String key) {
            try {
                lock.readLock().lock();
                return config.get(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        /**
         * 更新配置项
         */
        public void updateConfig(String key, String value) {
            try {
                lock.writeLock().lock();
                System.out.println("[配置更新] " + key + " = " + value);
                Thread.sleep(500);  // 模拟验证配置的耗时
                config.put(key, value);
                System.out.println("[配置更新] ✓ 已更新：" + key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        /**
         * 批量获取所有配置
         */
        public java.util.Map<String, String> getAllConfig() {
            try {
                lock.readLock().lock();
                return new java.util.HashMap<>(config);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new java.util.HashMap<>();
            } finally {
                lock.readLock().unlock();
            }
        }
    }
    
    /**
     * 场景 3：共享数据字典
     * 多线程查询字典，单线程维护
     */
    static class DataDictionary {
        private final java.util.Map<String, String> dictionary = new java.util.HashMap<>();
        private final SimpleReadWriteLock lock = new SimpleReadWriteLock();
        
        /**
         * 查询词条
         */
        public String lookup(String word) {
            try {
                lock.readLock().lock();
                System.out.println("[查询] \"" + word + "\" -> " + 
                    dictionary.getOrDefault(word, "(无此词)"));
                Thread.sleep(300);
                return dictionary.get(word);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        /**
         * 添加新词条
         */
        public void addWord(String word, String definition) {
            try {
                lock.writeLock().lock();
                System.out.println("[添加] \"" + word + "\" = \"" + definition + "\"");
                Thread.sleep(800);
                dictionary.put(word, definition);
                System.out.println("[添加] ✓ 已添加词条：" + word);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        /**
         * 删除词条
         */
        public void removeWord(String word) {
            try {
                lock.writeLock().lock();
                System.out.println("[删除] 移除词条 \"" + word + "\"");
                Thread.sleep(500);
                dictionary.remove(word);
                System.out.println("[删除] ✓ 已删除：" + word);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   ReadWriteLock 应用场景演示          ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // ========== 场景 1：缓存系统 ==========
        testCache();
        
        Thread.sleep(1000);
        
        // ========== 场景 2：配置管理器 ==========
        testConfigManager();
        
        Thread.sleep(1000);
        
        // ========== 场景 3：数据字典 ==========
        testDictionary();
    }
    
    /**
     * 测试缓存系统
     */
    private static void testCache() throws InterruptedException {
        System.out.println("【场景 1】缓存系统（读多写少）");
        System.out.println("──────────────────────────────────────");
        
        Cache<String, Integer> cache = new Cache<>();
        
        // 先写入一些数据
        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);
        
        System.out.println("\n-- 并发读取测试（5 个线程同时读） --");
        
        // 创建 5 个读线程
        Thread[] readThreads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int threadNum = i + 1;
            readThreads[i] = new Thread(() -> {
                cache.get("A");
                cache.get("B");
            }, "Read-" + threadNum);
        }
        
        // 同时启动所有读线程
        for (Thread thread : readThreads) {
            thread.start();
        }
        
        // 等待读线程完成
        for (Thread thread : readThreads) {
            thread.join();
        }
        
        System.out.println("\n-- 写操作测试（写时禁止读） --");
        
        // 启动写线程
        Thread writeThread = new Thread(() -> {
            cache.put("D", 4);
        }, "Write-1");
        
        // 在读的同时尝试写
        Thread concurrentRead = new Thread(() -> {
            cache.get("C");
        }, "Read-Concurrent");
        
        writeThread.start();
        Thread.sleep(200);  // 让写线程先开始
        concurrentRead.start();
        
        writeThread.join();
        concurrentRead.join();
        
        System.out.println("\n✓ 缓存系统测试完成\n");
    }
    
    /**
     * 测试配置管理器
     */
    private static void testConfigManager() throws InterruptedException {
        System.out.println("【场景 2】配置管理器（频繁读，偶尔写）");
        System.out.println("──────────────────────────────────────");
        
        ConfigManager config = new ConfigManager();
        
        System.out.println("-- 并发读取配置（3 个线程） --");
        
        // 创建 3 个读配置线程
        Thread[] readThreads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int threadNum = i + 1;
            readThreads[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    String timeout = config.getConfig("timeout");
                    String maxConn = config.getConfig("maxConnections");
                    System.out.println("[线程-" + threadNum + "] 读取配置：timeout=" 
                        + timeout + ", maxConnections=" + maxConn);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "ConfigRead-" + threadNum);
        }
        
        for (Thread thread : readThreads) {
            thread.start();
        }
        
        Thread.sleep(500);
        
        // 更新配置（需要等待读完成）
        System.out.println("\n-- 更新配置（需要等待读完成） --");
        Thread updateThread = new Thread(() -> {
            config.updateConfig("timeout", "5000");
            config.updateConfig("maxConnections", "200");
        }, "ConfigUpdate");
        
        updateThread.start();
        updateThread.join();
        
        for (Thread thread : readThreads) {
            thread.join();
        }
        
        System.out.println("\n✓ 配置管理器测试完成\n");
    }
    
    /**
     * 测试数据字典
     */
    private static void testDictionary() throws InterruptedException {
        System.out.println("【场景 3】共享数据字典");
        System.out.println("──────────────────────────────────────");
        
        DataDictionary dict = new DataDictionary();
        
        // 预先添加一些词条
        dict.addWord("Java", "一种编程语言");
        dict.addWord("Lock", "锁机制");
        dict.addWord("Cache", "缓存");
        
        System.out.println("\n-- 并发查询（4 个线程） --");
        
        // 创建 4 个查询线程
        Thread[] queryThreads = new Thread[4];
        String[] words = {"Java", "Lock", "Cache", "Unknown"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            queryThreads[i] = new Thread(() -> {
                dict.lookup(words[idx]);
            }, "Query-" + (i + 1));
        }
        
        for (Thread thread : queryThreads) {
            thread.start();
        }
        
        Thread.sleep(600);
        
        // 添加新词条（需要等待查询完成）
        System.out.println("\n-- 添加新词条（需要等待查询完成） --");
        Thread addThread = new Thread(() -> {
            dict.addWord("ReadWriteLock", "读写锁");
        }, "Add-Word");
        
        addThread.start();
        addThread.join();
        
        for (Thread thread : queryThreads) {
            thread.join();
        }
        
        System.out.println("\n✓ 数据字典测试完成\n");
    }
}
