package org.doubao.user.server.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.user.server.core.dto.PasswordChangeDto;
import org.doubao.user.server.core.dto.UpdateEmailDto;
import org.doubao.user.server.core.dto.UserDto;
import org.doubao.user.server.core.dto.UserUpdateDto;
import org.doubao.user.server.core.entity.User;
import org.doubao.user.server.core.feign.AuthServiceClient;
import org.doubao.user.server.core.feign.OssServiceClient;
import org.doubao.user.server.core.mapper.UserMapper;
import org.doubao.user.server.core.messaging.UserEventPublisher;
import org.doubao.user.server.core.service.UserService;
import org.doubao.user.server.core.utils.UserUtil;
import org.doubao.user.server.core.vo.PageUserVo;
import org.doubao.user.server.core.vo.UserVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.*;
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

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	@Override
	public String uploadAvatar(MultipartFile avatarFile, Long userId) {
		FileUploadResult result = ossServiceClient.uploadFile(
				avatarFile).getData();
		// 更新用户头像URL
		userMapper.updateUserAvatar(userId, result.getFileKey(), result.getStorageType());
		clearUserCache(userId);
		return result.getAccessUrl();
	}

	@Override
	public String uploadBg(MultipartFile file, Long userId) {
		FileUploadResult result = ossServiceClient.uploadFile(
				file).getData();
		userMapper.updateUserBg(userId, result.getFileKey(), result.getStorageType());
		clearUserCache(userId);
		return result.getAccessUrl();
	}

	@Override
	public List<UserInfo> usersByIds(Set<Long> userIds) {
		List<Long> noCacheId = new ArrayList<>();
		List<UserVo> userVoList = new ArrayList<>();
		List<UserInfo> userInfoList = new ArrayList<>();
		for (Long userId : userIds) {
			String cacheKey = "USER:" + userId;
			Object user = redisTemplate.opsForValue().get(cacheKey);
			if (user != null) {
				UserVo userVo = JSON.toJavaObject(JSON.parseObject(JSON.toJSONString(user)), UserVo.class);
				userVoList.add(userVo);
			} else {
				noCacheId.add(userId);
			}
		}
		if (!noCacheId.isEmpty()) {
			List<User> users = this.listByIds(noCacheId);
			for (User user : users) {
				String cacheKey = "USER:" + user.getId();
				UserVo userVo = UserVo.from(user);
				// 动态生成头像URL
				userVo.setAvatarUrl(ossServiceClient.generateAccessUrl(
						user.getAvatarKey(),
						user.getStorageType()
				).getData());
				userVo.setBgUrl(ossServiceClient.generateAccessUrl(
						user.getBgKey(),
						user.getStorageType()
				).getData());
				userVoList.add(userVo);
				redisTemplate.opsForValue().set(cacheKey, userVo,
						Duration.ofMinutes(30 + new Random().nextInt(10)));
			}
		}
		// UserVo 转 UserInfo
		userVoList.forEach(userVo -> {
			UserInfo userInfo = UserVo.fromVo(userVo);
			userInfoList.add(userInfo);
		});
		return userInfoList;
	}

	@Override
	public boolean checkUserExists(Map<String, String> request) {
		String userId = request.get("userId");
		return this.getById(userId) != null;
	}

	@Override
	public UserVo getById(Long id) {
		String cacheKey = "USER:" + id;
		Object user = redisTemplate.opsForValue().get(cacheKey);
		if (user == null) {
			user = userMapper.selectById(id);
			User userEntity = (User) user;
			UserVo userVo = UserVo.from(userEntity);
			// 动态生成头像URL
			userVo.setAvatarUrl(ossServiceClient.generateAccessUrl(
					userEntity.getAvatarKey(),
					userEntity.getStorageType()
			).getData());
			userVo.setBgUrl(ossServiceClient.generateAccessUrl(
					userEntity.getBgKey(),
					userEntity.getStorageType()
			).getData());

			redisTemplate.opsForValue().set(cacheKey, userVo,
					Duration.ofMinutes(30 + new Random().nextInt(10)));
			return userVo;
		}
		return JSON.toJavaObject(JSON.parseObject(JSON.toJSONString(user)),UserVo.class);
	}

	@Override
	public void register(UserDto userDto) {
		// 验证邮箱唯一性
		if (userMapper.findByEmail(userDto.getEmail()) != null) {
			throw new BusinessException(ErrorCode.EMAIL_EXISTS);
		}
		// 验证用户名唯一性
		if (userMapper.findByUsername(userDto.getUsername())) {
			throw new BusinessException(ErrorCode.USERNAME_EXISTS);
		}

		sendEmailCodeAndSave(userDto.getEmail(), "您的注册验证码");
	}

	@Override
	public void completeRegistration(UserDto userDto) {
		String code = userDto.getCode();
		String redisKey = "VERIFY_CODE:" + userDto.getEmail();
		String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

		if (storedCode == null || !storedCode.equals(code)) {
			throw new BusinessException(ErrorCode.INVALID_VERIFY_CODE);
		}

		// 创建用户
		User user = new User();
		user.setUsername(userDto.getUsername());
		user.setPassword(passwordEncoder.encode(userDto.getPassword()));
		user.setEmail(userDto.getEmail());

		// 生成昵称
		String nickname = UserUtil.generateArtisticNickname();
		// 校验昵称是否已存在
		if (userMapper.existsByNickname(nickname)) {
			// 生成随机数昵称
			nickname = nickname + new Random().nextInt(1000);
		}
		// 设置昵称
		user.setNickname(nickname);
		userMapper.insert(user);

		// 清理验证码
		redisTemplate.delete(redisKey);
	}

	@Override
	public UserVo login(String username, String password) {

		User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));

		if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException(ErrorCode.USERNAME_PASSWORD_ERROR);
		}

		if (user.getStatus() == 1) {
			throw new BusinessException(ErrorCode.USER_DISABLED);
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setId(user.getId().toString());
		userInfo.setUsername(user.getUsername());
		UserInfo data = authServiceClient.login(userInfo).getData();
		UserVo userVo = UserVo.from(user);
		// 动态生成头像URL
		userVo.setAvatarUrl(ossServiceClient.generateAccessUrl(
				user.getAvatarKey(),
				user.getStorageType()
		).getData());
		userVo.setBgUrl(ossServiceClient.generateAccessUrl(
				user.getBgKey(),
				user.getStorageType()
		).getData());
		userVo.setToken(data.getToken());
		String cacheKey = "USER:" + user.getId();
		redisTemplate.opsForValue().set(cacheKey, userVo,
				Duration.ofMinutes(30 + new Random().nextInt(10)));
		return userVo;
	}

	@Override
	public void updateProfile(UserUpdateDto dto) {
		User user = new User();
		if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
			// 昵称校验
			if (userMapper.existsByNickname(dto.getNickname())) {
				throw new BusinessException(ErrorCode.USER_NICKNAME_EXISTS);
			}
		}
		user.setId(dto.getUserId());
		BeanUtils.copyProperties(dto, user);
		user.setRole(null);
		this.updateById(user);
		clearUserCache(dto.getUserId());
	}

	@Override
	public void changePassword(PasswordChangeDto dto) {
		Long userId = UserContext.getUserId();
		// 2. 参数校验（新密码复杂度）
		if (!isValidNewPassword(dto.getNewPassword())) {
			throw new BusinessException(ErrorCode.USER_INVALID_NEW_PASSWORD);
		}
		User user = userMapper.selectById(userId);
		if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
			throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
		}
		user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
		userMapper.updateById(user);
		clearUserCache(userId);
	}
	// 新密码复杂度校验
	private boolean isValidNewPassword(String newPassword) {
		if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 20) {
			return false;
		}
		// 正则：包含至少1个字母和1个数字
		return newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).+$");
	}
	@Override
	public PageUserVo<UserVo> adminSearchUsers(int page, int size, Integer status, String email) {
		return null;
	}

	@Override
	public void adminUpdateStatus(Long userId, Integer status) {
		if (status != 0 && status != 1) {
			throw new BusinessException(ErrorCode.INVALID_STATUS);
		}

		User user = new User();
		user.setId(userId);
		user.setStatus(status);
		user.setRole(null);
		userMapper.updateById(user);
		clearUserCache(userId);
	}

	@Override
	public void adminDeleteUser(Long userId) {
		userMapper.deleteById(userId);
		clearUserCache(userId);
	}

	@Override
	public void adminUpdateRole(Long userId, String role) {
		if (!"USER".equals(role) && !"ADMIN".equals(role)) {
			throw new BusinessException(ErrorCode.INVALID_ROLE);
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
			throw new BusinessException(ErrorCode.INVALID_VERIFY_CODE);
		}
		User user = new User();
		user.setId(dto.getUserId());
		user.setEmail(dto.getEmail());
		user.setRole(null);
		userMapper.updateById(user);
	}

	private void clearUserCache(Long userId) {
		String cacheKey = "USER:" + userId;
		redisTemplate.delete(cacheKey);
	}

	private void sendEmailCodeAndSave(String email, String subject) {
		// 生成验证码（6位数字）
		String code = String.format("%06d", new Random().nextInt(999999));
		logger.info("----------------Generated verification code: {}", code);
		// 发送验证邮件
		userEventPublisher.sendVerificationEmail(email, code, subject);
		// 存储验证码到Redis（5分钟有效）
		String redisKey = "VERIFY_CODE:" + email;
		redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
	}
}
