package org.doubao.user.server.log.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.server.core.dto.LoginDto;
import org.doubao.user.server.log.annotation.LogRecord;
import org.doubao.user.server.log.entity.OperationLog;
import org.doubao.user.server.log.service.OperationLogService;
import org.doubao.user.server.log.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 日志记录AOP切面
 */
@Aspect
@Component
public class LogRecordAspect {

	private static final Logger log = LoggerFactory.getLogger(LogRecordAspect.class);
	@Autowired
	private OperationLogService operationLogService;

	@Value("${spring.application.name}")
	private String serviceName;

	/**
	 * 定义切点：标注了@LogRecord注解的方法
	 */
	@Pointcut("@annotation(org.doubao.user.server.log.annotation.LogRecord)")
	public void logPointCut() {
	}

	/**
	 * 方法执行成功后记录日志
	 */
	@AfterReturning(pointcut = "logPointCut()", returning = "result")
	public void doAfterReturning(JoinPoint joinPoint, Object result) {
		recordLog(joinPoint, "SUCCESS", result != null ? result.toString() : "");
	}

	/**
	 * 方法抛出异常时记录日志
	 */
	@AfterThrowing(pointcut = "logPointCut()", throwing = "e")
	public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
		recordLog(joinPoint, "FAIL", e.getMessage());
	}

	/**
	 * 记录日志
	 */
	private void recordLog(JoinPoint joinPoint, String result, String detail) {
		try {
			// 获取当前请求
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attributes == null) {
				return;
			}
			HttpServletRequest request = attributes.getRequest();

			// 获取注解信息
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			Method method = signature.getMethod();
			LogRecord logAnnotation = method.getAnnotation(LogRecord.class);

			// 构建日志对象
			OperationLog log = new OperationLog();
			String requestURI = request.getRequestURI();
			log.setTargetUrl(requestURI);

			// 登录接口从参数LoginDto loginDto获取用户名
			if ("/user/login".equals(requestURI)) {
				Object arg = joinPoint.getArgs()[0];
				LoginDto loginDto = (LoginDto) arg;
				log.setUsername(loginDto.getUsername());
			} else {
				log.setUsername(getCurrentUsername());
			}


			// 设置操作类型
			log.setOperationType(logAnnotation.operationType());

			// 设置网络信息
			log.setSourceIp(WebUtils.getIpAddress(request));
			log.setSourcePort(request.getLocalPort());

			// 设置客户端信息
			log.setClientHardwareInfo(WebUtils.getUserAgent(request));

			// 设置操作结果和详情
			log.setOperationResult(result);
			log.setOperationDetail(detail);

			// 设置服务名
			log.setServiceName(serviceName);

			// 记录日志
			operationLogService.recordLog(log);
		} catch (Exception e) {
			log.error("AOP记录日志失败", e);
		}
	}

	/**
	 * 获取当前登录用户名
	 * 实际项目中从SecurityContext或Token中获取
	 */
	private String getCurrentUsername() {
		UserLoginVo user = UserContext.getUser();
		return user != null ? user.getUsername() : "unknown";
	}
}