package org.doubao.interview.agent.api.dto.question023;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 用户注册请求 DTO
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：封装用户注册请求参数，触发注册事件
 * 边界：仅用于演示场景
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
public class UserRegisterRequest {

    /**
     * 用户名称
     */
    @NotBlank(message = "用户名称不能为空")
    private String userName;

    /**
     * 用户邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 是否异步处理
     * true: 使用@Async 异步监听事件
     * false: 同步监听事件
     */
    private Boolean async = false;

    /**
     * 备注说明
     */
    private String remark;
}
