package org.doubao.interview.agent.server.controller.question028;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question028.InjectionDemoRequest;
import org.doubao.interview.agent.api.dto.question028.InjectionDemoResponse;
import org.doubao.interview.agent.server.service.impl.question028.InjectionDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 依赖注入演示 Controller
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示@Autowired 和@Resource 的区别
 * 边界：仅用于学习和演示目的，不应用于生产环境
 * 线程安全：Controller 本身无状态，线程安全
 * 
 * 【面试知识点 - 问题 028】
 * 这个 Controller 通过实际的 Service 注入，展示了：
 * 1. 当存在多个 MessageService 实现类时，如何正确注入
 * 2. @Resource(name="xxx") 按名称注入的使用方式
 * 3. @Autowired + @Qualifier("xxx") 的配合使用方式
 * 4. 两种注入策略的实际效果和区别
 * 
 * 注意：这个 Controller 自身也使用了@Autowired 注入 Service
 * 但因为 InjectionDemoService 只有一个实现，所以不会产生歧义
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question028")
public class InjectionDemoController {
    
    /**
     * 注入演示服务
     * 
     * 【关键代码段注释】
     * 为什么这里可以直接使用@Autowired？
     * 1. InjectionDemoService 在当前项目中只有一个实现（它自己）
     * 2. Spring 按类型查找时能找到唯一的 bean
     * 3. 不需要@Qualifier，因为没有歧义
     * 4. 这正好对比说明了：当有多个同类型 bean 时才需要特殊处理
     */
    @Autowired
    private InjectionDemoService injectionDemoService;
    
    /**
     * 演示@Resource 按名称注入
     * 
     * 【方法注释】
     * 输入约束：request 参数不能为 null，message 字段不能为空
     * 输出语义：返回使用@Resource 注入的服务处理结果
     * 异常场景：
     *   - 参数校验失败返回 400 Bad Request
     *   - 服务处理异常返回 500 Internal Server Error
     * 性能注意点：无特殊性能考虑，纯内存操作
     * 
     * @param request 请求参数，包含消息内容和要使用的实现类
     * @return 演示结果，包含使用的注入方式和实际调用的服务
     */
    @PostMapping("/resource/by-name")
    public InjectionDemoResponse demoResourceByName(@Valid @RequestBody InjectionDemoRequest request) {
        log.info("[InjectionDemoController] 收到@Resource 演示请求，implName={}, message={}", 
                request.getImplName(), request.getMessage());
        
        InjectionDemoResponse response = injectionDemoService.demoResourceByName(request);
        
        log.info("[InjectionDemoController] @Resource 演示完成，injectionType={}, actualService={}", 
                response.getInjectionType(), response.getActualServiceClass());
        
        return response;
    }
    
    /**
     * 演示@Autowired + @Qualifier 注入
     * 
     * 【方法注释】
     * 输入约束：request 参数不能为 null，message 字段不能为空
     * 输出语义：返回使用@Autowired+@Qualifier 注入的服务处理结果
     * 异常场景：
     *   - 参数校验失败返回 400 Bad Request
     *   - 服务处理异常返回 500 Internal Server Error
     * 性能注意点：无特殊性能考虑
     * 
     * @param request 请求参数，包含消息内容和要使用的实现类
     * @return 演示结果，包含使用的注入方式和实际调用的服务
     */
    @PostMapping("/autowired/with-qualifier")
    public InjectionDemoResponse demoAutowiredWithQualifier(@Valid @RequestBody InjectionDemoRequest request) {
        log.info("[InjectionDemoController] 收到@Autowired+@Qualifier 演示请求，implName={}, message={}", 
                request.getImplName(), request.getMessage());
        
        InjectionDemoResponse response = injectionDemoService.demoAutowiredWithQualifier(request);
        
        log.info("[InjectionDemoController] @Autowired+@Qualifier 演示完成，injectionType={}, actualService={}", 
                response.getInjectionType(), response.getActualServiceClass());
        
        return response;
    }
    
    /**
     * 获取详细对比说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回@Autowired 和@Resource 的详细对比说明文本
     * 异常场景：无
     * 性能注意点：无
     * 
     * @return 详细对比说明
     */
    @GetMapping("/comparison")
    public String getComparison() {
        log.info("[InjectionDemoController] 获取@Autowired 和@Resource 的对比说明");
        return injectionDemoService.getDetailedComparison();
    }
    
    /**
     * 健康检查接口
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回服务健康状态
     * 异常场景：无
     * 性能注意点：无
     * 
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public String health() {
        log.info("[InjectionDemoController] 健康检查");
        return "Question028: @Autowired vs @Resource - OK";
    }
}
