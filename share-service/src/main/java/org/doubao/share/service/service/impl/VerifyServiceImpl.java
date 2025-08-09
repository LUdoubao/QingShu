package org.doubao.share.service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.mall.common.util.IPUtil;
import org.doubao.share.service.dto.ShareVerifyDTO;
import org.doubao.share.service.entity.ShareLink;
import org.doubao.share.service.feign.UserServiceFeign;
import org.doubao.share.service.service.LinkService;
import org.doubao.share.service.service.RecordService;
import org.doubao.share.service.service.VerifyService;
import org.doubao.share.service.utils.EncryptionUtil;
import org.doubao.share.service.vo.VerifyResultVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class VerifyServiceImpl implements VerifyService {

	@Autowired
	private LinkService linkService;

	@Autowired
	private RecordService recordService;

	@Autowired
	private UserServiceFeign userServiceFeign;

	@Autowired
	private EncryptionUtil encryptionUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private static final String PASSWORD_ERROR_KEY_PREFIX = "share:passwordError:";
	private static final String VIEW_LIMIT_KEY_PREFIX = "share:viewLimit:";
	private static final int MAX_PASSWORD_ERROR_COUNT = 5;
	private static final int LOCK_TIME_HOURS = 1;

	@Override
	public VerifyResultVO verifyShareLink(ShareVerifyDTO dto, HttpServletRequest request) {
		VerifyResultVO result = new VerifyResultVO();

		// 1. 获取分享链接信息
		ShareLink shareLink = linkService.getShareLinkByUrl(dto.getShareLink());
		if (shareLink == null) {
			result.setValid(false);
			result.setMessage("分享链接不存在");
			return result;
		}

		// 2. 检查链接状态
		if (shareLink.getStatus() != 1) {
			result.setValid(false);
			result.setMessage("分享链接已失效");
			return result;
		}

		// 3. 检查有效期
		if (shareLink.getExpireTime() != null && LocalDateTime.now().isAfter(shareLink.getExpireTime())) {
			result.setValid(false);
			result.setMessage("分享链接已过期");
			return result;
		}

		// 4. 根据访问控制类型进行验证
		int accessControl = shareLink.getAccessControl();
		String ipAddress = IPUtil.getClientIp(request);

		switch (accessControl) {
			case 1: // 公开访问
				handleVerificationSuccess(shareLink, dto.getUserId(), ipAddress, accessControl, result);
				break;

			case 2: // 密码访问
				if (!verifyPassword(dto.getPassword(), shareLink.getEncryptPassword(), ipAddress)) {
					result.setValid(false);
					result.setMessage("密码错误");
					return result;
				}
				handleVerificationSuccess(shareLink, dto.getUserId(), ipAddress, accessControl, result);
				break;

			case 3: // 指定用户访问
				String userId = dto.getUserId();
				if (userId == null || userId.isEmpty()) {
					result.setValid(false);
					result.setMessage("请先登录");
					return result;
				}
				if (!verifyAuthorizedUser(dto.getUserId(), shareLink.getAuthorizedUsers())) {
					result.setValid(false);
					result.setMessage("您没有访问权限");
					return result;
				}
				handleVerificationSuccess(shareLink, dto.getUserId(), ipAddress, accessControl, result);
				break;

			default:
				result.setValid(false);
				result.setMessage("无效的访问控制类型");
		}

		return result;
	}

	private boolean verifyPassword(String inputPassword, String encryptPassword, String ipAddress) {
		// 检查是否被锁定
		String errorKey = PASSWORD_ERROR_KEY_PREFIX + ipAddress;
		Integer errorCount = (Integer) redisTemplate.opsForValue().get(errorKey);
		if (errorCount != null && errorCount >= MAX_PASSWORD_ERROR_COUNT) {
			throw new RuntimeException("密码错误次数过多，请" + LOCK_TIME_HOURS + "小时后再试");
		}

		try {
			// 解密并验证密码
			String decryptPassword = encryptionUtil.decrypt(encryptPassword);
			if (decryptPassword.equals(inputPassword)) {
				// 验证成功，清除错误计数
				redisTemplate.delete(errorKey);
				return true;
			}
		} catch (Exception e) {
			throw new RuntimeException("密码验证失败", e);
		}

		// 验证失败，增加错误计数
		int newErrorCount = (errorCount == null) ? 1 : errorCount + 1;
		redisTemplate.opsForValue().set(errorKey, newErrorCount, LOCK_TIME_HOURS, TimeUnit.HOURS);

		return false;
	}

	private boolean verifyAuthorizedUser(String userId, String authorizedUsersJson) {
		if (userId == null || userId.isEmpty()) {
			return false;
		}

		// 检查用户是否存在
		Map<String, String> request = new HashMap<>();
		request.put("userId", userId);
		if (!userServiceFeign.checkUserExists(request)) {
			return false;
		}

		try {
			List<String> authorizedUsers = objectMapper.readValue(authorizedUsersJson, new TypeReference<List<String>>() {});
			return authorizedUsers.contains(userId);
		} catch (Exception e) {
			throw new RuntimeException("验证授权用户失败", e);
		}
	}

	private void handleVerificationSuccess(ShareLink shareLink, String userId, String ipAddress,
										   int accessControl, VerifyResultVO result) {
		// 记录访问
		recordService.recordAccess(shareLink.getId(), userId, ipAddress, accessControl);

		// 触发浏览量统计（24小时内同一用户只统计一次）
		if (userId != null && !userId.isEmpty()) {
			String viewLimitKey = VIEW_LIMIT_KEY_PREFIX + userId + ":" + shareLink.getQuoteId();
			if (redisTemplate.opsForValue().get(viewLimitKey) == null) {
				sendViewCountMessage(shareLink.getQuoteId(), userId);
				redisTemplate.opsForValue().set(viewLimitKey, true, 24, TimeUnit.HOURS);
			}
		} else {
			// 匿名用户每次访问都统计
			sendViewCountMessage(shareLink.getQuoteId(), null);
		}

		// 设置验证成功结果
		result.setValid(true);
		result.setQuoteId(shareLink.getQuoteId());
		result.setMessage("验证通过");
	}

	private void sendViewCountMessage(String quoteId, String userId) {
		Map<String, Object> message = new HashMap<>();
		message.put("quoteId", quoteId);
		message.put("source", "SHARE");
		message.put("userId", userId);

		// rabbitTemplate.convertAndSend(VIEW_COUNT_EXCHANGE, VIEW_COUNT_ROUTING_KEY, message);
	}
}
