package org.doubao.interview.question020.demo;

import org.doubao.interview.question020.concurrent.SimpleThreadLocal;

/**
 * ThreadLocal 应用场景演示
 * 
 * 场景 1：用户上下文管理
 * 场景 2：分布式追踪 ID
 * 场景 3：SimpleDateFormat 线程安全
 */
public class ThreadLocalDemo {
    
    /**
     * 场景 1：用户上下文
     * 每个线程维护自己的用户信息
     */
    static class UserContext {
        // 使用 ThreadLocal 存储当前线程的用户信息
        private static final SimpleThreadLocal<String> userId = new SimpleThreadLocal<>();
        private static final SimpleThreadLocal<String> username = new SimpleThreadLocal<>();
        
        /**
         * 设置当前线程的用户信息
         */
        public static void setUser(String uid, String uname) {
            userId.set(uid);
            username.set(uname);
            System.out.println("[设置用户] 线程-" + Thread.currentThread().getName() 
                + " -> userId=" + uid + ", username=" + uname);
        }
        
        /**
         * 获取当前线程的用户 ID
         */
        public static String getUserId() {
            return userId.get();
        }
        
        /**
         * 获取当前线程的用户名
         */
        public static String getUsername() {
            return username.get();
        }
        
        /**
         * 清除当前线程的用户信息
         * 重要：防止内存泄漏
         */
        public static void clear() {
            userId.remove();
            username.remove();
            System.out.println("[清除用户] 线程-" + Thread.currentThread().getName());
        }
        
        /**
         * 打印当前线程的用户信息
         */
        public static void printContext() {
            System.out.println("[线程-" + Thread.currentThread().getName() + "] "
                + "userId=" + getUserId() + ", username=" + getUsername());
        }
    }
    
    /**
     * 场景 2：分布式追踪 ID
     * 每个请求线程有独立的 traceId
     */
    static class TraceContext {
        // 使用 ThreadLocal 存储追踪 ID
        private static final SimpleThreadLocal<String> traceId = new SimpleThreadLocal<>();
        
        /**
         * 生成或获取当前线程的 traceId
         */
        public static String getTraceId() {
            String id = traceId.get();
            if (id == null) {
                // 如果没有，生成新的
                id = generateTraceId();
                traceId.set(id);
                System.out.println("[生成 traceId] 线程-" + Thread.currentThread().getName() 
                    + " -> " + id);
            }
            return id;
        }
        
        /**
         * 设置 traceId（用于链路追踪）
         */
        public static void setTraceId(String id) {
            traceId.set(id);
            System.out.println("[设置 traceId] 线程-" + Thread.currentThread().getName() 
                + " -> " + id);
        }
        
        /**
         * 清除 traceId
         */
        public static void clear() {
            String id = traceId.get();
            traceId.remove();
            System.out.println("[清除 traceId] 线程-" + Thread.currentThread().getName() 
                + " -> " + id);
        }
        
        /**
         * 生成 traceId
         */
        private static String generateTraceId() {
            return "TRACE-" + System.currentTimeMillis() + "-" 
                + Thread.currentThread().getId();
        }
    }
    
    /**
     * 场景 3：SimpleDateFormat 线程安全
     * 每个线程独立的日期格式化器
     */
    static class DateUtils {
        // 非线程安全的 SimpleDateFormat
        // private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 使用 ThreadLocal 保证线程安全
        private static final SimpleThreadLocal<java.text.SimpleDateFormat> dateFormatHolder = 
            new SimpleThreadLocal<>();
        
        /**
         * 获取当前线程的日期格式化器
         */
        private static java.text.SimpleDateFormat getDateFormat() {
            return dateFormatHolder.getOrDefault(
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            );
        }
        
        /**
         * 格式化日期（线程安全）
         */
        public static String format(java.util.Date date) {
            return getDateFormat().format(date);
        }
        
        /**
         * 解析日期字符串（线程安全）
         */
        public static java.util.Date parse(String dateString) throws java.text.ParseException {
            return getDateFormat().parse(dateString);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   ThreadLocal 应用场景演示             ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // ========== 场景 1：用户上下文管理 ==========
        testUserContext();
        
        Thread.sleep(1000);
        
        // ========== 场景 2：分布式追踪 ID ==========
        testTraceId();
        
        Thread.sleep(1000);
        
        // ========== 场景 3：SimpleDateFormat 线程安全 ==========
        testDateFormat();
    }
    
    /**
     * 测试用户上下文管理
     */
    private static void testUserContext() throws InterruptedException {
        System.out.println("【场景 1】用户上下文管理");
        System.out.println("──────────────────────────────────────");
        
        // 模拟多个并发请求，每个请求有不同的用户
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int index = i + 1;
            threads[i] = new Thread(() -> {
                // 设置当前请求的用户
                UserContext.setUser("user" + index, "用户" + index);
                
                // 模拟业务处理（多个方法调用）
                doBusinessLogic();
                
                // 清理用户信息
                UserContext.clear();
            }, "Request-" + index);
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
            Thread.sleep(100);  // 错开启动时间
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("\n✓ 用户上下文测试完成\n");
    }
    
    /**
     * 模拟业务逻辑处理
     */
    private static void doBusinessLogic() {
        // 模拟多个方法调用，都能获取到正确的用户信息
        methodA();
        methodB();
    }
    
    private static void methodA() {
        System.out.print("[Method A] ");
        UserContext.printContext();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void methodB() {
        System.out.print("[Method B] ");
        UserContext.printContext();
    }
    
    /**
     * 测试分布式追踪 ID
     */
    private static void testTraceId() throws InterruptedException {
        System.out.println("【场景 2】分布式追踪 ID");
        System.out.println("──────────────────────────────────────");
        
        // 模拟多个并发请求的链路追踪
        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int index = i + 1;
            threads[i] = new Thread(() -> {
                // 获取或生成 traceId
                String traceId = TraceContext.getTraceId();
                
                // 模拟调用链
                callService1(traceId);
                callService2(traceId);
                
                // 清理
                TraceContext.clear();
            }, "Service-" + index);
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("\n✓ 分布式追踪 ID 测试完成\n");
    }
    
    private static void callService1(String parentTraceId) {
        System.out.println("[Service 1] traceId=" + parentTraceId);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void callService2(String parentTraceId) {
        System.out.println("[Service 2] traceId=" + parentTraceId);
    }
    
    /**
     * 测试 SimpleDateFormat 线程安全
     */
    private static void testDateFormat() throws InterruptedException {
        System.out.println("【场景 3】SimpleDateFormat 线程安全");
        System.out.println("──────────────────────────────────────");
        
        // 创建多个线程并发格式化日期
        Thread[] threads = new Thread[5];
        final java.util.Date baseDate = new java.util.Date();
        
        for (int i = 0; i < 5; i++) {
            final int index = i + 1;
            threads[i] = new Thread(() -> {
                try {
                    // 并发格式化（不会出现线程安全问题）
                    String formatted = DateUtils.format(baseDate);
                    System.out.println("[线程-" + index + "] 格式化：" + formatted);
                    
                    // 并发解析
                    java.util.Date parsed = DateUtils.parse(formatted);
                    System.out.println("[线程-" + index + "] 解析成功：" + parsed);
                    
                } catch (java.text.ParseException e) {
                    System.err.println("[线程-" + index + "] 解析失败：" + e.getMessage());
                }
            }, "Format-" + index);
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("\n✓ SimpleDateFormat 线程安全测试完成\n");
    }
}
