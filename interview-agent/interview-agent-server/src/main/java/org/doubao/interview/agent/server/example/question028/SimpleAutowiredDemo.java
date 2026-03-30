package org.doubao.interview.agent.server.example.question028;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化版@Autowired 实现原理演示
 * 
 * 【类注释】
 * 职责：通过简化版的代码，演示 Spring@Autowired 的底层工作原理
 * 边界：这是教学示例，不是完整的 Spring 实现，省略了很多细节
 * 线程安全：单线程演示，不考虑并发
 * 
 * 【面试知识点 - 问题 028 扩展】
 * 这个类用 100 行左右的代码，模拟了 Spring 依赖注入的核心流程：
 * 1. 自定义注解@SimpleAutowired
 * 2. 简单的 Bean 容器（Map 实现）
 * 3. 反射扫描字段上的注解
 * 4. 按类型查找并注入 Bean
 * 5. 多实现场景下的异常处理
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
public class SimpleAutowiredDemo {
    
    // ==================== 第一部分：定义注解 ====================
    
    /**
     * 简化的@Autowired 注解
     * 只保留最核心的功能：标记需要注入的字段
     */
    @Target(ElementType.FIELD)  // 只能用在字段上
    @Retention(RetentionPolicy.RUNTIME)  // 运行时可见，才能通过反射读取
    public @interface SimpleAutowired {
        /**
         * 可选的 Bean 名称
         * 不指定时按类型注入
         * 指定时按名称注入
         */
        String value() default "";
    }
    
    // ==================== 第二部分：Bean 容器 ====================
    
    /**
     * 简化的 Bean 工厂
     * 用一个 Map 存储所有 Bean，类似 Spring 的 DefaultListableBeanFactory
     */
    static class SimpleBeanFactory {
        // 存储 Bean 的 Map：key=beanName, value=bean 实例
        private Map<String, Object> beanMap = new HashMap<>();
        
        /**
         * 注册 Bean 到容器
         * 类似 Spring 的 registerBeanDefinition + createBean
         */
        public void registerBean(String beanName, Object bean) {
            beanMap.put(beanName, bean);
            System.out.println("[BeanFactory] 注册 Bean: " + beanName + 
                " -> " + bean.getClass().getSimpleName());
        }
        
        /**
         * 按类型获取 Bean（核心方法！）
         * 类似 Spring 的 getBean(Class<T>)
         */
        public <T> T getBean(Class<T> requiredType) {
            System.out.println("[BeanFactory] 按类型查找 Bean: " + requiredType.getSimpleName());
            
            Object foundBean = null;
            String foundBeanName = null;
            
            // 遍历所有 Bean，查找匹配类型的
            for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
                if (requiredType.isInstance(entry.getValue())) {
                    if (foundBean != null) {
                        // 找到多个匹配的 Bean，抛出异常
                        // 这就是 Spring 报 NoUniqueBeanDefinitionException 的原因！
                        throw new RuntimeException(
                            String.format("找到多个 %s 类型的 Bean: %s 和 %s", 
                                requiredType.getSimpleName(), foundBeanName, entry.getKey()));
                    }
                    foundBean = entry.getValue();
                    foundBeanName = entry.getKey();
                }
            }
            
            if (foundBean == null) {
                // 没找到匹配的 Bean
                throw new RuntimeException("未找到 " + requiredType.getSimpleName() + " 类型的 Bean");
            }
            
            System.out.println("[BeanFactory] 找到 Bean: " + foundBeanName);
            return requiredType.cast(foundBean);
        }
        
        /**
         * 按名称获取 Bean
         * 类似 Spring 的 getBean(String name)
         */
        public Object getBean(String beanName) {
            System.out.println("[BeanFactory] 按名称查找 Bean: " + beanName);
            Object bean = beanMap.get(beanName);
            if (bean == null) {
                throw new RuntimeException("未找到名为 " + beanName + " 的 Bean");
            }
            return bean;
        }
    }
    
    // ==================== 第三部分：后处理器（核心逻辑） ====================
    
    /**
     * 简化的后处理器
     * 模拟 Spring 的 AutowiredAnnotationBeanPostProcessor
     */
    static class SimpleAutowiredProcessor {
        
        private SimpleBeanFactory beanFactory;
        
        public SimpleAutowiredProcessor(SimpleBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }
        
        /**
         * 处理 Bean 的依赖注入
         * 类似 Spring 的 postProcessProperties()
         */
        public void processInjection(Object bean) {
            Class<?> clazz = bean.getClass();
            System.out.println("\n[AutowiredProcessor] 开始处理 Bean: " + 
                clazz.getSimpleName());
            
            // 扫描所有字段（包括父类，这里简化为只扫描当前类）
            for (Field field : clazz.getDeclaredFields()) {
                // 检查字段是否有@SimpleAutowired 注解
                if (field.isAnnotationPresent(SimpleAutowired.class)) {
                    System.out.println("  [扫描] 发现需要注入的字段：" + field.getName() + 
                        " (" + field.getType().getSimpleName() + ")");
                    
                    // 执行注入
                    injectField(field, bean);
                }
            }
        }
        
        /**
         * 注入单个字段
         * 类似 Spring 的 AutowiredFieldElement.inject()
         */
        private void injectField(Field field, Object bean) {
            try {
                // 获取注解信息
                SimpleAutowired annotation = field.getAnnotation(SimpleAutowired.class);
                String beanName = annotation.value();
                Class<?> fieldType = field.getType();
                
                Object dependency;
                
                if (!beanName.isEmpty()) {
                    // 指定了名称，按名称查找
                    System.out.println("  [注入] 按名称查找：" + beanName);
                    dependency = beanFactory.getBean(beanName);
                } else {
                    // 没指定名称，按类型查找
                    System.out.println("  [注入] 按类型查找：" + fieldType.getSimpleName());
                    dependency = beanFactory.getBean(fieldType);
                }
                
                // 使用反射设置字段值
                // 这就是 Spring 注入的最后一道工序！
                field.setAccessible(true);  // 即使是 private 字段也能注入
                field.set(bean, dependency);
                
                System.out.println("  [成功] 字段注入完成：" + field.getName() + 
                    " = " + dependency.getClass().getSimpleName());
                
            } catch (Exception e) {
                System.out.println("  [失败] 字段注入失败：" + field.getName());
                e.printStackTrace();
            }
        }
    }
    
    // ==================== 第四部分：演示用的业务类 ====================
    
    /**
     * 消息服务接口
     */
    interface MessageService {
        String sendMessage(String message);
    }
    
    /**
     * 消息服务实现 A
     */
    static class MessageServiceImplA implements MessageService {
        @Override
        public String sendMessage(String message) {
            return "[ServiceA] 发送消息：" + message;
        }
    }
    
    /**
     * 消息服务实现 B
     */
    static class MessageServiceImplB implements MessageService {
        @Override
        public String sendMessage(String message) {
            return "[ServiceB] >> 发送消息：" + message;
        }
    }
    
    /**
     * 用户服务（使用@Autowired 注入）
     */
    static class UserService {
        
        // 使用自定义的@Autowired 注解
        @SimpleAutowired
        private MessageService messageService;
        
        public void testSend() {
            String result = messageService.sendMessage("Hello World");
            System.out.println("UserService 调用结果：" + result);
        }
    }
    
    /**
     * 订单服务（指定 bean 名称注入）
     */
    static class OrderService {
        
        // 指定使用 ServiceB
        @SimpleAutowired("messageServiceImplB")
        private MessageService messageService;
        
        public void testSend() {
            String result = messageService.sendMessage("Order Created");
            System.out.println("OrderService 调用结果：" + result);
        }
    }
    
    // ==================== 第五部分：运行演示 ====================
    
    public static void main(String[] args) {
        System.out.println("========== @Autowired 原理演示 ==========\n");
        
        // 1. 创建 Bean 工厂
        SimpleBeanFactory factory = new SimpleBeanFactory();
        
        // 2. 注册 Bean（类似 Spring 的@Component 扫描）
        factory.registerBean("messageServiceImplA", new MessageServiceImplA());
        factory.registerBean("messageServiceImplB", new MessageServiceImplB());
        
        // 3. 创建 UserService 实例
        UserService userService = new UserService();
        
        // 4. 创建后处理器并处理注入
        SimpleAutowiredProcessor processor = new SimpleAutowiredProcessor(factory);
        
        try {
            // 5. 执行注入（这里会失败，因为有多个 MessageService 实现）
            System.out.println("【演示 1】不指定 bean 名称，按类型注入...");
            processor.processInjection(userService);
            userService.testSend();
        } catch (Exception e) {
            System.out.println("\n❌ 预期中的错误：有多个同类型 Bean 时报错\n");
        }
        
        // 6. 创建 OrderService 实例（指定了 bean 名称）
        OrderService orderService = new OrderService();
        
        try {
            // 7. 执行注入（这次会成功，因为指定了具体的 bean 名称）
            System.out.println("\n【演示 2】指定 bean 名称，精确注入...");
            processor.processInjection(orderService);
            orderService.testSend();
            System.out.println("\n✅ 指定 bean 名称后注入成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("\n========== 演示结束 ==========");
        System.out.println("\n【核心结论】");
        System.out.println("1. @Autowired 默认按类型注入");
        System.out.println("2. 当有多个同类型 Bean 时会报错");
        System.out.println("3. 使用@SimpleAutowired(\"beanName\") 可以指定具体的 Bean");
        System.out.println("4. Spring 的@Autowired+@Qualifier 也是同样的原理");
    }
}
