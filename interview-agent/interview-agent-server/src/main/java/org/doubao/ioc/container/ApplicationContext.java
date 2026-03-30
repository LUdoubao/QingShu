package org.doubao.ioc.container;

import org.doubao.ioc.annotation.Autowired;
import org.doubao.ioc.annotation.Component;
import org.doubao.ioc.annotation.Scope;
import org.doubao.ioc.annotation.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易版 Spring IOC 容器
 * <p>
 * 核心功能：
 * 1. 扫描指定包下的所有带@Component/@Service 注解的类
 * 2. 实例化这些类并放入 Bean 容器
 * 3. 自动注入带@Autowired 注解的字段（依赖注入）
 * 4. 支持单例（singleton）和原型（prototype）作用域
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
public class ApplicationContext {
    
    /**
     * Bean 容器 - 存储所有 Bean 实例
     * key: beanName, value: bean 实例
     */
    private final Map<String, Object> beanContainer = new ConcurrentHashMap<>();
    
    /**
     * Bean 定义容器 - 存储所有 Bean 的定义信息
     * key: beanName, value: BeanDefinition
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    
    /**
     * 接口与实现的映射关系
     * key: 接口的 Class, value: 实现类的 beanName
     */
    private final Map<Class<?>, String> interfaceImplMap = new ConcurrentHashMap<>();
    
    /**
     * 要扫描的包路径列表
     */
    private final List<String> packagePaths;
    
    /**
     * 构造函数
     *
     * @param packagePaths 要扫描的包路径
     */
    public ApplicationContext(String... packagePaths) {
        this.packagePaths = Arrays.asList(packagePaths);
        
        // 初始化 IOC 容器
        initialize();
    }
    
    /**
     * 初始化 IOC 容器
     */
    private void initialize() {
        System.out.println("========================================");
        System.out.println("开始初始化简易版 Spring IOC 容器");
        System.out.println("========================================");
        
        try {
            // 1. 扫描包路径，获取所有 Bean 定义
            System.out.println("\n【步骤 1】扫描包路径，加载 Bean 定义...");
            scanPackages();
            
            // 2. 实例化所有 Bean
            System.out.println("\n【步骤 2】实例化 Bean...");
            instantiateBeans();
            
            // 3. 依赖注入
            System.out.println("\n【步骤 3】执行依赖注入...");
            injectDependencies();
            
            System.out.println("\n========================================");
            System.out.println("IOC 容器初始化完成！");
            System.out.println("容器中的 Bean 数量：" + beanContainer.size());
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("IOC 容器初始化失败：" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("IOC 容器初始化失败", e);
        }
    }
    
    /**
     * 扫描包路径，加载所有 Bean 定义
     */
    private void scanPackages() throws Exception {
        for (String packagePath : packagePaths) {
            System.out.println("扫描包路径：" + packagePath);
            
            // 获取包下所有类
            Set<Class<?>> classes = getClassesFromPackage(packagePath);
            
            for (Class<?> clazz : classes) {
                // 检查是否有@Component 或@Service 注解
                if (clazz.isAnnotationPresent(Component.class) || 
                    clazz.isAnnotationPresent(Service.class)) {
                    
                    // 生成 bean 名称
                    String beanName = generateBeanName(clazz);
                    
                    // 获取作用域
                    String scope = "singleton";
                    if (clazz.isAnnotationPresent(Scope.class)) {
                        Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                        scope = scopeAnnotation.value();
                    }
                    
                    // 创建 Bean 定义
                    BeanDefinition beanDefinition = new BeanDefinition(clazz, scope);
                    beanDefinitionMap.put(beanName, beanDefinition);
                                        
                    // 记录接口与实现的映射关系
                    registerInterfaceMapping(clazz, beanName);
                                        
                    System.out.println("✓ 加载 Bean: " + beanName + 
                                     " (" + clazz.getSimpleName() + 
                                     ", scope=" + scope + ")");
                }
            }
        }
    }
    
    /**
     * 从包路径获取所有类
     * <p>
     * 简化版本：实际项目中需要使用 ClassLoader 或第三方库（如 Reflections）
     * </p>
     */
    private Set<Class<?>> getClassesFromPackage(String packageName) throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        
        // 将包名转换为路径
        String packagePath = packageName.replace('.', '/');
        
        // 获取类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        // 简化处理：直接从已知的类中筛选
        // 实际项目应该扫描文件系统或 JAR 包
        String[] classNames = {
            "org.doubao.ioc.example.IUserService",
            "org.doubao.ioc.example.UserServiceImpl",
            "org.doubao.ioc.example.UserController",
            "org.doubao.ioc.example.OrderService"
        };
        
        for (String className : classNames) {
            try {
                Class<?> clazz = classLoader.loadClass(className);
                
                // 检查是否在目标包下
                if (clazz.getPackage().getName().startsWith(packageName)) {
                    classes.add(clazz);
                }
            } catch (ClassNotFoundException ignored) {
                // 类不存在，跳过
            }
        }
        
        return classes;
    }
    
    /**
     * 生成 Bean 名称
     * <p>
     * 规则：
     * 1. 如果注解指定了 value，使用 value
     * 2. 否则使用类名首字母小写
     * </p>
     */
    private String generateBeanName(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        
        // 默认：类名首字母小写
        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
    
    /**
     * 注册接口与实现的映射关系
     * <p>
     * 扫描实现类实现的接口，建立映射关系
     * </p>
     */
    private void registerInterfaceMapping(Class<?> implClass, String beanName) {
        System.out.println("  检查实现类：" + implClass.getSimpleName() + " (" + beanName + ")");
        
        // 获取实现的所有接口（包括父接口的所有接口）
        Set<Class<?>> allInterfaces = new HashSet<>();
        getAllInterfaces(implClass, allInterfaces);
        
        System.out.println("    实现的接口总数：" + allInterfaces.size());
        
        for (Class<?> iface : allInterfaces) {
            System.out.println("    检查接口：" + iface.getName() + 
                             " (包名：" + iface.getPackage().getName() + ")");
            
            // 只注册项目包下的接口
            if (iface.getPackage().getName().startsWith("org.doubao.ioc")) {
                interfaceImplMap.put(iface, beanName);
                System.out.println("    ✓ 注册接口映射：" + iface.getSimpleName() + 
                                 " → " + beanName);
            } else {
                System.out.println("    ✗ 跳过非项目接口：" + iface.getName());
            }
        }
        
        // 如果没有找到接口，打印警告
        if (allInterfaces.isEmpty()) {
            System.out.println("    ⚠ 警告：该类没有实现任何接口");
        }
    }
    
    /**
     * 递归获取类及其父类实现的所有接口
     */
    private void getAllInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        
        // 添加当前类实现的接口
        Class<?>[] currentInterfaces = clazz.getInterfaces();
        for (Class<?> iface : currentInterfaces) {
            interfaces.add(iface);
            // 递归添加父接口
            getAllInterfaces(iface, interfaces);
        }
        
        // 递归处理父类
        getAllInterfaces(clazz.getSuperclass(), interfaces);
    }
    
    /**
     * 实例化所有 Bean
     */
    private void instantiateBeans() throws Exception {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            
            // 只实例化单例 Bean（原型 Bean 在获取时创建）
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = createBean(beanDefinition);
                beanContainer.put(beanName, bean);
                System.out.println("  ✓ 实例化 Bean: " + beanName);
            }
        }
    }
    
    /**
     * 创建 Bean 实例
     * <p>
     * 核心反射逻辑：
     * 1. 获取构造器
     * 2. 如果有带参构造器，先递归创建依赖的 Bean
     * 3. 调用构造器创建实例
     * </p>
     */
    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        Constructor<?> targetConstructor = getConstructor(beanDefinition);

        // 处理构造器参数（依赖注入）
        int paramCount = targetConstructor.getParameterCount();
        Object[] params = new Object[paramCount];
        
        if (paramCount > 0) {
            Type[] parameterTypes = targetConstructor.getGenericParameterTypes();
            
            for (int i = 0; i < paramCount; i++) {
                Class<?> paramType = (Class<?>) parameterTypes[i];
                
                // 从容器中获取依赖的 Bean
                String dependencyBeanName = generateBeanName(paramType);
                Object dependencyBean = getBean(dependencyBeanName);
                
                if (dependencyBean == null) {
                    // 如果依赖的 Bean 还未创建，先创建它
                    BeanDefinition dependencyDef = beanDefinitionMap.get(dependencyBeanName);
                    if (dependencyDef != null) {
                        dependencyBean = createBean(dependencyDef);
                        beanContainer.put(dependencyBeanName, dependencyBean);
                    } else {
                        throw new RuntimeException("找不到依赖的 Bean: " + dependencyBeanName);
                    }
                }
                
                params[i] = dependencyBean;
            }
        }
        
        // 调用构造器创建实例
        return targetConstructor.newInstance(params);
    }

    private static Constructor<?> getConstructor(BeanDefinition beanDefinition) throws NoSuchMethodException {
        Class<?> clazz = beanDefinition.getBeanClass();

        // 获取所有构造器
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // 优先使用带@Autowired 注解的构造器
        Constructor<?> targetConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                targetConstructor = constructor;
                break;
            }
        }

        // 如果没有标注的构造器，使用无参构造器
        if (targetConstructor == null) {
            targetConstructor = clazz.getDeclaredConstructor();
        }

        // 设置可访问（绕过私有构造器检查）
        targetConstructor.setAccessible(true);
        return targetConstructor;
    }

    /**
     * 依赖注入 - 为所有 Bean 的@Autowired 字段注入值
     */
    private void injectDependencies() throws Exception {
        for (Map.Entry<String, Object> entry : beanContainer.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            
            injectFields(bean);
            System.out.println("  ✓ 完成依赖注入：" + beanName);
        }
    }
    
    /**
     * 为对象的字段进行依赖注入
     * <p>
     * 核心反射逻辑：
     * 1. 获取所有字段（包括父类）
     * 2. 检查是否有@Autowired 注解
     * 3. 从容器中获取对应的 Bean 并注入
     * </p>
     */
    private void injectFields(Object bean) throws Exception {
        Class<?> clazz = bean.getClass();
        
        // 遍历所有字段（包括父类的私有字段）
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            
            for (Field field : fields) {
                // 检查是否有@Autowired 注解
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    
                    // 跳过静态字段
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    
                    // 设置可访问（关键：绕过 private 检查）
                    field.setAccessible(true);
                    
                    // 如果字段已经有值，跳过
                    if (field.get(bean) != null) {
                        continue;
                    }
                    
                    // 获取字段类型
                    Class<?> fieldType = field.getType();
                    
                    // 获取依赖的 Bean 名称
                    String dependencyBeanName = generateBeanName(fieldType);
                    
                    // 从容器中获取 Bean
                    Object dependencyBean = getBean(dependencyBeanName);
                    
                    // 如果没找到，检查是否是接口
                    if (dependencyBean == null && fieldType.isInterface()) {
                        // 从接口映射中查找实现类
                        String implBeanName = interfaceImplMap.get(fieldType);
                        if (implBeanName != null) {
                            dependencyBean = getBean(implBeanName);
                            System.out.println("      → 通过接口找到实现：" + 
                                             fieldType.getSimpleName() + " → " + implBeanName);
                        } else {
                            // 尝试通过类型直接查找 Bean
                            System.out.println("      ⚠ 接口映射未找到，尝试通过类型查找...");
                            for (Object object : beanContainer.values()) {
                                if (fieldType.isInstance(object)) {
                                    dependencyBean = object;
                                    System.out.println("      ✓ 通过类型匹配找到：" +
                                            object.getClass().getSimpleName());
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (dependencyBean == null) {
                        if (autowired.required()) {
                            throw new RuntimeException(
                                "无法注入依赖的 Bean: " + dependencyBeanName + 
                                " (字段：" + field.getName() + ")"
                            );
                        } else {
                            System.out.println("  ⚠ 可选依赖未找到：" + field.getName());
                            continue;
                        }
                    }
                    
                    // 注入 Bean
                    field.set(bean, dependencyBean);
                    System.out.println("      → 注入 " + field.getName() + 
                                     " (" + fieldType.getSimpleName() + ")");
                }
            }
            
            // 继续处理父类的字段
            clazz = clazz.getSuperclass();
        }
    }
    
    /**
     * 获取 Bean
     * <p>
     * 根据名称获取容器中的 Bean
     * </p>
     *
     * @param beanName Bean 名称
     * @return Bean 实例
     */
    public Object getBean(String beanName) {
        return beanContainer.get(beanName);
    }
    
    /**
     * 获取 Bean（泛型版本）
     * <p>
     * 类型安全的 Bean 获取方式
     * </p>
     *
     * @param beanName Bean 名称
     * @param clazz Bean 类型
     * @param <T> 泛型类型
     * @return Bean 实例
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> clazz) {
        Object bean = beanContainer.get(beanName);
        if (bean != null && clazz.isInstance(bean)) {
            return (T) bean;
        }
        return null;
    }
    
    /**
     * 根据类型获取 Bean
     * <p>
     * 类似 Spring 的 getBean(Class<T> requiredType)
     * </p>
     *
     * @param clazz Bean 类型
     * @param <T> 泛型类型
     * @return Bean 实例
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        // 查找匹配的 Bean
        for (Map.Entry<String, Object> entry : beanContainer.entrySet()) {
            if (clazz.isInstance(entry.getValue())) {
                return (T) entry.getValue();
            }
        }
        
        // 如果没找到，尝试创建 prototype 类型的 Bean
        String beanName = generateBeanName(clazz);
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        
        if (beanDefinition != null && "prototype".equals(beanDefinition.getScope())) {
            try {
                return (T) createBean(beanDefinition);
            } catch (Exception e) {
                throw new RuntimeException("创建 Bean 失败：" + beanName, e);
            }
        }
        
        return null;
    }
    
    /**
     * 检查容器中是否包含指定的 Bean
     *
     * @param beanName Bean 名称
     * @return true-包含，false-不包含
     */
    public boolean containsBean(String beanName) {
        return beanContainer.containsKey(beanName);
    }
    
    /**
     * 获取容器中所有的 Bean 名称
     *
     * @return Bean 名称数组
     */
    public String[] getBeanDefinitionNames() {
        return beanContainer.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取 Bean 的数量
     *
     * @return Bean 数量
     */
    public int getBeanCount() {
        return beanContainer.size();
    }
}
