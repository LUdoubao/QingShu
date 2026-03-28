package org.doubao.interview.question019.demo;

import org.doubao.interview.question019.concurrent.SimpleStampedLock;

/**
 * StampedLock 应用场景演示
 * 
 * 场景 1：点坐标计算（乐观读）
 * 场景 2：缓存系统（三种模式对比）
 * 场景 3：配置管理（锁升级）
 */
public class StampedLockDemo {
    
    /**
     * 场景 1：点坐标
     * 演示乐观读的高性能
     */
    static class Point {
        private double x, y;
        private final SimpleStampedLock lock = new SimpleStampedLock();
        
        /**
         * 移动点（写操作）
         */
        public void move(double newX, double newY) {
            long stamp;
            try {
                stamp = lock.writeLock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                System.out.println("[写] 线程-" + Thread.currentThread().getName() 
                    + " 移动点到 (" + newX + ", " + newY + ")");
                Thread.sleep(500);
                this.x = newX;
                this.y = newY;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlockWrite(stamp);
            }
        }
        
        /**
         * 距离计算（乐观读）
         */
        public double distanceFromOrigin() {
            // 尝试乐观读，不加锁
            long stamp = lock.tryOptimisticRead();
            
            // 读取数据
            double currentX = x;
            double currentY = y;
            
            // 校验期间是否有写操作
            if (!lock.validate(stamp)) {
                // 校验失败，升级为悲观读锁
                System.out.println("[乐观读] 校验失败，升级为悲观读锁");
                try {
                    stamp = lock.readLock();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return 0.0;
                }
                try {
                    currentX = x;
                    currentY = y;
                } finally {
                    lock.unlockRead(stamp);
                }
            } else {
                System.out.println("[乐观读] 校验成功，无需加锁");
            }
            
            return Math.sqrt(currentX * currentX + currentY * currentY);
        }
        
        /**
         * 获取坐标（悲观读）
         */
        public double[] getCoordinates() {
            long stamp;
            try {
                stamp = lock.readLock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new double[]{0, 0};
            }
            try {
                System.out.println("[悲观读] 线程-" + Thread.currentThread().getName() 
                    + " 获取坐标");
                Thread.sleep(200);
                return new double[]{x, y};
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new double[]{0, 0};
            } finally {
                lock.unlockRead(stamp);
            }
        }
    }
    
    /**
     * 场景 2：缓存系统
     * 对比三种模式的性能差异
     */
    static class Cache<K, V> {
        private final java.util.Map<K, V> cache = new java.util.HashMap<>();
        private final SimpleStampedLock lock = new SimpleStampedLock();
        
        /**
         * 写入缓存（写锁）
         */
        public void put(K key, V value) {
            long stamp;
            try {
                stamp = lock.writeLock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                System.out.println("[写锁] 写入 key=" + key);
                Thread.sleep(300);
                cache.put(key, value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlockWrite(stamp);
            }
        }
        
        /**
         * 读取缓存（乐观读）
         */
        public V getOptimistic(K key) {
            long stamp = lock.tryOptimisticRead();
            V value = cache.get(key);
            
            if (!lock.validate(stamp)) {
                System.out.println("[乐观读] 校验失败，重新获取");
                // 可升级为悲观读或直接返回
            }
            
            return value;
        }
        
        /**
         * 读取缓存（悲观读）
         */
        public V getPessimistic(K key) {
            long stamp;
            try {
                stamp = lock.readLock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            try {
                System.out.println("[悲观读] 读取 key=" + key);
                Thread.sleep(200);
                return cache.get(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                lock.unlockRead(stamp);
            }
        }
    }
    
    /**
     * 场景 3：配置管理
     * 演示锁升级功能
     */
    static class ConfigManager {
        private String configValue = "default";
        private final SimpleStampedLock lock = new SimpleStampedLock();
        
        /**
         * 读取配置（先乐观读，必要时升级）
         */
        public String getConfigAndMaybeUpdate(String newValue) {
            // 先乐观读
            long stamp = lock.tryOptimisticRead();
            String currentValue = configValue;
            
            if (!lock.validate(stamp)) {
                System.out.println("[配置] 乐观读失效，使用悲观读");
                try {
                    stamp = lock.readLock();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return currentValue;
                }
                try {
                    currentValue = configValue;
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            
            // 如果需要更新，尝试升级锁
            if (!currentValue.equals(newValue)) {
                System.out.println("[配置] 需要更新，尝试锁升级...");
                long writeStamp = lock.tryConvertToWriteLock(stamp);
                
                if (writeStamp != 0L) {
                    // 升级成功
                    System.out.println("[配置] 锁升级成功，执行更新");
                    try {
                        configValue = newValue;
                    } finally {
                        lock.unlockWrite(writeStamp);
                    }
                } else {
                    // 升级失败，重新获取写锁
                    System.out.println("[配置] 锁升级失败，重新获取写锁");
                    try {
                        stamp = lock.writeLock();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return currentValue;
                    }
                    try {
                        configValue = newValue;
                    } finally {
                        lock.unlockWrite(stamp);
                    }
                }
            }
            
            return currentValue;
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   StampedLock 应用场景演示             ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // ========== 场景 1：点坐标计算 ==========
        testPoint();
        
        Thread.sleep(1000);
        
        // ========== 场景 2：缓存系统 ==========
        testCache();
        
        Thread.sleep(1000);
        
        // ========== 场景 3：配置管理 ==========
        testConfigManager();
    }
    
    /**
     * 测试点坐标（乐观读）
     */
    private static void testPoint() throws InterruptedException {
        System.out.println("【场景 1】点坐标计算（乐观读）");
        System.out.println("──────────────────────────────────────");
        
        Point point = new Point();
        
        // 先设置初始位置
        point.move(3.0, 4.0);
        
        System.out.println("\n-- 并发读取（乐观读） --");
        
        // 创建多个读线程
        Thread[] readThreads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int threadNum = i + 1;
            readThreads[i] = new Thread(() -> {
                double distance = point.distanceFromOrigin();
                System.out.println("[线程-" + threadNum + "] 距离原点：" + distance);
            }, "Read-" + threadNum);
        }
        
        // 同时启动所有读线程
        for (Thread thread : readThreads) {
            thread.start();
            Thread.sleep(100);  // 错开启动
        }
        
        // 等待读线程完成
        for (Thread thread : readThreads) {
            thread.join();
        }
        
        System.out.println("\n-- 写时禁止读 --");
        
        // 写操作
        Thread writeThread = new Thread(() -> {
            point.move(6.0, 8.0);
        }, "Write-1");
        
        // 在读的同时尝试写
        Thread concurrentRead = new Thread(() -> {
            double[] coords = point.getCoordinates();
            System.out.println("[并发读] 坐标：(" + coords[0] + ", " + coords[1] + ")");
        }, "Read-Concurrent");
        
        writeThread.start();
        Thread.sleep(200);
        concurrentRead.start();
        
        writeThread.join();
        concurrentRead.join();
        
        System.out.println("\n✓ 点坐标测试完成\n");
    }
    
    /**
     * 测试缓存系统
     */
    private static void testCache() throws InterruptedException {
        System.out.println("【场景 2】缓存系统（三种模式对比）");
        System.out.println("──────────────────────────────────────");
        
        Cache<String, Integer> cache = new Cache<>();
        
        // 先写入一些数据
        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);
        
        System.out.println("\n-- 乐观读（无锁） --");
        
        // 乐观读测试
        Thread[] optimisticThreads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            optimisticThreads[i] = new Thread(() -> {
                String key = (char)('A' + idx % 3) + "";
                Integer value = cache.getOptimistic(key);
                System.out.println("[乐观读-" + idx + "] " + key + " = " + value);
            }, "Optimistic-" + (i + 1));
        }
        
        for (Thread thread : optimisticThreads) {
            thread.start();
        }
        
        for (Thread thread : optimisticThreads) {
            thread.join();
        }
        
        System.out.println("\n-- 悲观读（加锁） --");
        
        // 悲观读测试
        Thread[] pessimisticThreads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            pessimisticThreads[i] = new Thread(() -> {
                String key = (char)('A' + idx) + "";
                Integer value = cache.getPessimistic(key);
                System.out.println("[悲观读-" + idx + "] " + key + " = " + value);
            }, "Pessimistic-" + (i + 1));
        }
        
        for (Thread thread : pessimisticThreads) {
            thread.start();
            Thread.sleep(100);
        }
        
        for (Thread thread : pessimisticThreads) {
            thread.join();
        }
        
        System.out.println("\n✓ 缓存系统测试完成\n");
    }
    
    /**
     * 测试配置管理
     */
    private static void testConfigManager() throws InterruptedException {
        System.out.println("【场景 3】配置管理（锁升级）");
        System.out.println("──────────────────────────────────────");
        
        ConfigManager config = new ConfigManager();
        
        System.out.println("-- 读取并可能更新配置 --");
        
        // 测试配置更新
        Thread updateThread1 = new Thread(() -> {
            String result = config.getConfigAndMaybeUpdate("new_value_1");
            System.out.println("[配置 -1] 原值：" + result);
        }, "Update-1");
        
        Thread.sleep(100);
        
        Thread updateThread2 = new Thread(() -> {
            String result = config.getConfigAndMaybeUpdate("new_value_2");
            System.out.println("[配置 -2] 原值：" + result);
        }, "Update-2");
        
        updateThread1.start();
        updateThread2.start();
        
        updateThread1.join();
        updateThread2.join();
        
        System.out.println("\n✓ 配置管理测试完成\n");
    }
}
