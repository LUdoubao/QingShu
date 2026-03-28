package org.doubao.interview.question017.demo;

import org.doubao.interview.question017.concurrent.SimpleSemaphore;

/**
 * Semaphore 应用场景演示
 * 
 * 场景 1：数据库连接池控制
 * 场景 2：API 限流器
 * 场景 3：停车场管理系统
 */
public class SemaphoreDemo {
    
    /**
     * 场景 1：数据库连接池控制
     * 限制同时访问数据库的连接数为 5 个
     */
    static class DatabaseConnectionPool {
        // 信号量控制最大连接数
        private final SimpleSemaphore semaphore;
        
        public DatabaseConnectionPool(int maxConnections) {
            this.semaphore = new SimpleSemaphore(maxConnections, true);
        }
        
        /**
         * 获取数据库连接
         */
        public void getConnection() throws InterruptedException {
            System.out.println("[线程：" + Thread.currentThread().getName() 
                + "] 请求数据库连接，当前可用：" + semaphore.availablePermits());
            
            semaphore.acquire();
            
            System.out.println("[线程：" + Thread.currentThread().getName() 
                + "] ✓ 获得数据库连接，剩余许可：" + semaphore.availablePermits());
        }
        
        /**
         * 释放数据库连接
         */
        public void releaseConnection() {
            semaphore.release();
            System.out.println("[线程：" + Thread.currentThread().getName() 
                + "] ✓ 释放数据库连接，剩余许可：" + semaphore.availablePermits());
        }
        
        /**
         * 执行数据库操作
         */
        public void executeQuery(String queryId) {
            try {
                getConnection();
                
                // 模拟数据库查询（耗时 2 秒）
                System.out.println("  └─ [查询-" + queryId + "] 正在执行...");
                Thread.sleep(2000);
                System.out.println("  └─ [查询-" + queryId + "] ✓ 执行完成");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                releaseConnection();
            }
        }
    }
    
    /**
     * 场景 2：API 限流器
     * 限制每秒最多处理 10 个请求
     */
    static class RateLimiter {
        private final SimpleSemaphore semaphore;
        private final int limit;
        
        public RateLimiter(int maxRequestsPerSecond) {
            this.limit = maxRequestsPerSecond;
            this.semaphore = new SimpleSemaphore(maxRequestsPerSecond);
        }
        
        /**
         * 处理请求
         */
        public void handleRequest(String requestId) {
            try {
                System.out.println("[请求-" + requestId + "] 等待处理...");
                
                // 尝试获取许可（最多等待 1 秒）
                boolean acquired = semaphore.tryAcquire(1, 1000);
                
                if (acquired) {
                    System.out.println("[请求-" + requestId + "] ✓ 开始处理，剩余配额：" 
                        + semaphore.availablePermits());
                    
                    // 模拟处理请求（耗时 0.5 秒）
                    Thread.sleep(500);
                    
                    System.out.println("[请求-" + requestId + "] ✓ 处理完成");
                    
                    // 释放许可
                    semaphore.release();
                } else {
                    System.out.println("[请求-" + requestId + "] ✗ 请求超时，拒绝服务");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        /**
         * 重置限流器
         */
        public void reset() {
            // 补充所有许可
            while (semaphore.availablePermits() < limit) {
                semaphore.release();
            }
            System.out.println("[限流器] 已重置，当前可用：" + semaphore.availablePermits());
        }
    }
    
    /**
     * 场景 3：停车场管理系统
     * 限制停车场内的车辆数
     */
    static class ParkingLot {
        private final SimpleSemaphore semaphore;
        private final int totalSpaces;
        
        public ParkingLot(int totalSpaces) {
            this.totalSpaces = totalSpaces;
            this.semaphore = new SimpleSemaphore(totalSpaces);
        }
        
        /**
         * 车辆进入停车场
         */
        public void enter(String carId) {
            try {
                System.out.println("[车辆-" + carId + "] 尝试进入停车场，剩余车位：" 
                    + semaphore.availablePermits());
                
                // 尝试获取车位（最多等待 3 秒）
                boolean entered = semaphore.tryAcquire(1, 3000);
                
                if (entered) {
                    System.out.println("[车辆-" + carId + "] ✓ 进入成功，剩余车位：" 
                        + semaphore.availablePermits());
                    
                    // 模拟停车（耗时 5 秒）
                    Thread.sleep(5000);
                    
                    leave(carId);
                } else {
                    System.out.println("[车辆-" + carId + "] ✗ 停车场已满，离开");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        /**
         * 车辆离开停车场
         */
        public void leave(String carId) {
            semaphore.release();
            System.out.println("[车辆-" + carId + "] ✓ 离开停车场，剩余车位：" 
                + semaphore.availablePermits());
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   Semaphore 应用场景演示              ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // ========== 场景 1：数据库连接池 ==========
        testDatabaseConnectionPool();
        
        Thread.sleep(2000);
        
        // ========== 场景 2：API 限流器 ==========
        testRateLimiter();
        
        Thread.sleep(2000);
        
        // ========== 场景 3：停车场管理 ==========
        testParkingLot();
    }
    
    /**
     * 测试数据库连接池
     */
    private static void testDatabaseConnectionPool() throws InterruptedException {
        System.out.println("【场景 1】数据库连接池控制（最大 3 个连接）");
        System.out.println("──────────────────────────────────────");
        
        DatabaseConnectionPool pool = new DatabaseConnectionPool(3);
        
        // 创建 5 个线程模拟并发查询
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final String queryId = "Q" + (i + 1);
            threads[i] = new Thread(() -> {
                pool.executeQuery(queryId);
            }, "DB-Thread-" + (i + 1));
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
            Thread.sleep(200);  // 错开启动时间
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("\n✓ 数据库连接池测试完成\n");
    }
    
    /**
     * 测试 API 限流器
     */
    private static void testRateLimiter() throws InterruptedException {
        System.out.println("【场景 2】API 限流器（每秒最多 3 个请求）");
        System.out.println("──────────────────────────────────────");
        
        RateLimiter limiter = new RateLimiter(3);
        
        // 模拟 6 个并发请求
        Thread[] threads = new Thread[6];
        for (int i = 0; i < 6; i++) {
            final String requestId = "R" + (i + 1);
            threads[i] = new Thread(() -> {
                limiter.handleRequest(requestId);
            }, "Request-Thread-" + (i + 1));
        }
        
        // 同时启动所有请求
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待 2 秒后重置限流器
        Thread.sleep(2000);
        limiter.reset();
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("\n✓ API 限流器测试完成\n");
    }
    
    /**
     * 测试停车场管理系统
     */
    private static void testParkingLot() throws InterruptedException {
        System.out.println("【场景 3】停车场管理（5 个车位）");
        System.out.println("──────────────────────────────────────");
        
        ParkingLot parkingLot = new ParkingLot(5);
        
        // 模拟 8 辆车到达
        Thread[] threads = new Thread[8];
        for (int i = 0; i < 8; i++) {
            final String carId = "京 A-" + String.format("%03d", i + 1);
            threads[i] = new Thread(() -> {
                parkingLot.enter(carId);
            }, "Car-" + carId);
        }
        
        // 分批次启动车辆
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
            Thread.sleep(500);  // 每 0.5 秒到达一辆车
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("\n✓ 停车场管理测试完成\n");
    }
}
