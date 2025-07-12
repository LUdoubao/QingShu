package org.doubao.user.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.dto.UploadResult;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.user.server.dto.*;
import org.doubao.user.server.entity.User;
import org.doubao.user.server.feign.AuthServiceClient;
import org.doubao.user.server.feign.OssServiceClient;
import org.doubao.user.server.mapper.UserMapper;
import org.doubao.user.server.messaging.UserEventPublisher;
import org.doubao.user.server.service.UserService;
import org.doubao.user.server.vo.PageUserVo;
import org.doubao.user.server.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
	@Resource
	private UserMapper userMapper;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	@Resource
	private PasswordEncoder passwordEncoder;
	@Resource
	private UserEventPublisher userEventPublisher;
	@Resource
	private AuthServiceClient authServiceClient;

	@Resource
	private OssServiceClient ossServiceClient;

	@Override
	public String uploadAvatar(MultipartFile avatarFile, Long userId) {
		UploadResult result = ossServiceClient.uploadFile(
				avatarFile,
				"avatars/" + userId,
				"image"
		).getData();
		// 更新用户头像URL
		userMapper.updateUserAvatar(userId, result.getFileUrl());
		clearUserCache(userId);
		return result.getFileUrl();
	}

	@Override
	public UserVo getById(Long id) {
		String cacheKey = "USER:" + id;
		Object user = redisTemplate.opsForValue().get(cacheKey);
		if (user == null) {
			user = userMapper.selectById(id);
			UserVo userVo = UserVo.from((User) user);
			redisTemplate.opsForValue().set(cacheKey, userVo,
					Duration.ofMinutes(30 + new Random().nextInt(10)));
		}
		return JSON.toJavaObject(JSON.parseObject(JSON.toJSONString(user)),UserVo.class);
	}

	@Override
	public void register(UserDto userDto) {
		// 验证邮箱唯一性
		if (userMapper.findByEmail(userDto.getEmail()) != null) {
			throw new BusinessException("邮箱已被注册", ErrorCode.EMAIL_EXISTS);
		}

		sendEmailCodeAndSave(userDto.getEmail(), "您的注册验证码");
	}

	@Override
	public void completeRegistration(UserDto userDto) {
		String code = userDto.getCode();
		String redisKey = "VERIFY_CODE:" + userDto.getEmail();
		String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

		if (storedCode == null || !storedCode.equals(code)) {
			throw new BusinessException("验证码无效或已过期", ErrorCode.INVALID_VERIFY_CODE);
		}

		// 创建用户
		User user = new User();
		user.setUsername(userDto.getUsername());
		user.setPassword(passwordEncoder.encode(userDto.getPassword()));
		user.setEmail(userDto.getEmail());
		userMapper.insert(user);

		// 清理验证码
		redisTemplate.delete(redisKey);
	}

	@Override
	public UserVo login(String username, String password) {
		User user = userMapper.findByEmail(username);
		if (user == null) {
			user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
		}

		if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException("用户名或密码错误", ErrorCode.USERNAME_PASSWORD_ERROR);
		}

		if (user.getStatus() == 1) {
			throw new BusinessException("用户已被禁用", ErrorCode.USER_DISABLED);
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setId(user.getId().toString());
		userInfo.setUsername(user.getUsername());
		UserInfo data = authServiceClient.login(userInfo).getData();
		UserVo userVo = UserVo.from(user);
		userVo.setToken(data.getToken());
		return userVo;
	}

	@Override
	public void updateProfile(UserUpdateDto dto) {
		User user = new User();
		user.setId(dto.getUserId());
		BeanUtils.copyProperties(dto, user);
		userMapper.updateById(user);
		clearUserCache(dto.getUserId());
	}

	@Override
	public void changePassword(Long userId, PasswordChangeDto dto) {
		User user = userMapper.selectById(userId);
		if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
			throw new BusinessException("原密码错误", ErrorCode.OLD_PASSWORD_ERROR);
		}
		user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
		userMapper.updateById(user);
		clearUserCache(userId);
	}

	@Override
	public PageUserVo<UserVo> adminSearchUsers(int page, int size, Integer status, String email) {
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.eq("deleted", false);

		if (status != null) {
			wrapper.eq("status", status);
		}
		if (email != null && !email.isEmpty()) {
			wrapper.like("email", email);
		}

		Page<User> userPage = userMapper.selectPage(new Page<>(page, size), wrapper);
		// return PageUserVo.from(userPage).setRecords(
		// 		userPage.getRecords().stream()
		// 				.map(UserVo::from)
		// 				.collect(Collectors.toList())
		// );
		return null;
	}

	@Override
	public void adminUpdateStatus(Long userId, Integer status) {
		if (status != 0 && status != 1) {
			throw new BusinessException("无效的状态值", ErrorCode.INVALID_STATUS);
		}

		User user = new User();
		user.setId(userId);
		user.setStatus(status);
		userMapper.updateById(user);
		clearUserCache(userId);

		// 广播状态变更事件
		// Map<String, Object> event = new HashMap<>();
		// event.put("userId", userId);
		// event.put("newStatus", status);
		// rabbitTemplate.convertAndSend("user.status.exchange", "user.status", event);
	}

	@Override
	public void adminDeleteUser(Long userId) {
		userMapper.deleteById(userId);
		clearUserCache(userId);
	}

	@Override
	public void adminUpdateRole(Long userId, String role) {
		if (!"USER".equals(role) && !"ADMIN".equals(role)) {
			throw new BusinessException("无效的角色类型", ErrorCode.INVALID_ROLE);
		}

		User user = new User();
		user.setId(userId);
		user.setRole(role);
		userMapper.updateById(user);
		clearUserCache(userId);
	}

	@Override
	public void loginOut(HttpServletRequest request) {
		// 获取token
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")) {
			token = token.substring(7);
			Long expirationTime = authServiceClient.getTokenExpiration(token).getData();
			Long currentTime = System.currentTimeMillis();
			long ttl = expirationTime - currentTime;

			if (ttl > 0) {
				redisTemplate.opsForValue().set("BLACKLIST:" + token, "invalid", ttl, TimeUnit.MILLISECONDS);
			}
		}
	}

	@Override
	public Boolean checkEmail(String email) {
		return userMapper.findByEmail(email) == null;
	}

	@Override
	public Boolean sendEmailVerifyCode(String email) {
		sendEmailCodeAndSave(email, "您的修改邮箱验证码");
		return true;
	}

	@Override
	public void updateEmail(UpdateEmailDto dto) {
		String redisKey = "VERIFY_CODE:" + dto.getEmail();
		String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

		if (storedCode == null || !storedCode.equals(dto.getCode())) {
			throw new BusinessException("验证码无效或已过期", ErrorCode.INVALID_VERIFY_CODE);
		}
		User user = new User();
		user.setId(dto.getUserId());
		user.setEmail(dto.getEmail());
		userMapper.updateById(user);
	}

	private void clearUserCache(Long userId) {
		String cacheKey = "USER:" + userId;
		redisTemplate.delete(cacheKey);
	}

	private void sendEmailCodeAndSave(String email, String subject) {
		// 生成验证码（6位数字）
		String code = String.format("%06d", new Random().nextInt(999999));
		// 发送验证邮件
		userEventPublisher.sendVerificationEmail(email, code, subject);
		// 存储验证码到Redis（5分钟有效）
		String redisKey = "VERIFY_CODE:" + email;
		redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
	}
}
