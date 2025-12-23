package org.doubao.user.service.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.service.annotation.LogRecord;
import org.doubao.user.service.dto.core.LoginDto;
import org.doubao.user.service.entity.log.OperationLog;
import org.doubao.user.service.service.log.OperationLogService;
import org.doubao.user.service.utils.WebUtils;
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

	@Pointcut("@annotation(org.doubao.user.service.log.annotation.LogRecord)")
	public void logPointCut() {
	}


	@AfterReturning(pointcut = "logPointCut()", returning = "result")
	public void doAfterReturning(JoinPoint joinPoint, Object result) {
		recordLog(joinPoint, "SUCCESS", result != null ? result.toString() : "");
	}

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


			log.setClientHardwareInfo(WebUtils.getUserAgent(request));


			log.setOperationResult(result);
			log.setOperationDetail(detail);


			log.setServiceName(serviceName);

			// 记录日志
			operationLogService.recordLog(log);
		} catch (Exception e) {
			log.error("AOP记录日志失败", e);
		}
	}

	private String getCurrentUsername() {
		UserLoginVo user = UserContext.getUser();
		return user != null ? user.getUsername() : "unknown";
	}
}
