package org.doubao.user.service.service.impl.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.service.dto.core.*;
import org.doubao.user.service.entity.core.User;
import org.doubao.user.service.feign.core.AuthServiceClient;
import org.doubao.user.service.feign.core.OssServiceClient;
import org.doubao.user.service.mapper.core.UserMapper;
import org.doubao.user.service.messaging.UserEventPublisher;
import org.doubao.user.service.service.core.UserService;
import org.doubao.user.service.utils.UserUtil;
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
	public List<UserInfoDes> usersByIds(Set<Long> userIds) {
		List<UserInfoDes> userInfoList = new ArrayList<>();
		List<Long> noCacheId = new ArrayList<>(userIds);
		if (!noCacheId.isEmpty()) {
			List<User> users = this.listByIds(noCacheId);
			for (User user : users) {
				UserInfoDes userInfoDes = new UserInfoDes(user.getId(), user.getNickname());
				userInfoDes.setAvatarUrl(ossServiceClient.generateAccessUrl(
						user.getAvatarKey(),
						user.getStorageType()).getData()
				);
				userInfoList.add(userInfoDes);
			}
		}
		return userInfoList;
	}

	@Override
	public boolean checkUserExists(Map<String, String> request) {
		String userId = request.get("userId");
		return this.getById(userId) != null;
	}

	@Override
	public UserInfoProfileEdit editGet() {
		return editGet(UserContext.getUserId());
	}
	public UserInfoProfileEdit editGet(Long userId) {
		User user = userMapper.selectById(userId);
		UserInfoProfileEdit userInfoDes = new UserInfoProfileEdit(user.getId(), user.getNickname(), user.getUsername());
		// 动态生成头像URL
		userInfoDes.setAvatarUrl(ossServiceClient.generateAccessUrl(
				user.getAvatarKey(),
				user.getStorageType()
		).getData());
		userInfoDes.setBgUrl(ossServiceClient.generateAccessUrl(
				user.getBgKey(),
				user.getStorageType()
		).getData());
		userInfoDes.setSignature(user.getSignature());
		return userInfoDes;
	}

	@Override
	public String editGetEmail() {
		Long userId = UserContext.getUserId();
		User user = userMapper.selectById(userId);
		String email = user.getEmail();
		// 邮箱脱敏
		return email.substring(0, 3) + "****" + email.substring(email.length() - 4);
	}

	@Override
	public UserInfoProfileEdit getWithSignature(Long userId) {
		return editGet(userId);
	}

	@Override
	public void forgotPasswordCode(ForgotPasswordCodeDto forgotPasswordCodeDto) {
		String email = forgotPasswordCodeDto.getEmail();
		if (email == null) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		if (userMapper.findByEmail(email) == null) {
			throw new BusinessException(ErrorCode.USER_EMAIL_NOT_EXISTS);
		}
		sendEmailCodeAndSave(email, "您的重置密码验证码");
	}

	@Override
	public void forgotPasswordVerify(ForgotPasswordVerifyDto forgotPasswordVerifyDto) {
		String email = forgotPasswordVerifyDto.getEmail();
		String code = forgotPasswordVerifyDto.getCode();
		forgotPasswordVerify(email, code, false);
	}

	private void forgotPasswordVerify(String email, String code, boolean isClear) {
		if (email == null || code == null) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}

		String redisKey = "VERIFY_CODE:" + email;
		Object redisCode = redisTemplate.opsForValue().get(redisKey);
		if (redisCode == null || redisCode.equals("") || !String.valueOf(redisCode).equals(code)) {
			throw new BusinessException(ErrorCode.VERIFY_CODE_ERROR);
		}
		if (isClear) {
			redisTemplate.delete(redisKey);
		}
	}

	@Override
	public void forgotPasswordReset(ForgotPasswordResetDto forgotPasswordResetDto) {
		String email = forgotPasswordResetDto.getEmail();
		String code = forgotPasswordResetDto.getCode();

		String newPassword = forgotPasswordResetDto.getNewPassword();
		if (email == null || newPassword == null) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		if (!isValidNewPassword(newPassword)) {
			throw new BusinessException(ErrorCode.USER_INVALID_NEW_PASSWORD);
		}
		forgotPasswordVerify(email, code, true);

		if (userMapper.findByEmail(email) == null) {
			throw new BusinessException(ErrorCode.USER_EMAIL_NOT_EXISTS);
		}
		userMapper.updateUserPassword(email, passwordEncoder.encode(newPassword));
	}

	@Override
	public UserInfoProfile getById(Long id) {
		User user = userMapper.selectById(id);
		UserInfoProfile userInfoDes = new UserInfoProfile(user.getId(), user.getNickname(), user.getUsername());
		// 动态生成头像URL
		userInfoDes.setAvatarUrl(ossServiceClient.generateAccessUrl(
				user.getAvatarKey(),
				user.getStorageType()
		).getData());
		userInfoDes.setBgUrl(ossServiceClient.generateAccessUrl(
				user.getBgKey(),
				user.getStorageType()
		).getData());
		return userInfoDes;
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
	public UserLoginVo login(String username, String password) {
		LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(User::getUsername, username);
		User user = userMapper.selectOne(queryWrapper);

		if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
			throw new BusinessException(ErrorCode.USERNAME_PASSWORD_ERROR);
		}

		if (user.getStatus() == 1) {
			throw new BusinessException(ErrorCode.USER_DISABLED);
		}

		UserLoginVo userInfo = new UserLoginVo();
		userInfo.setId(user.getId());
		userInfo.setUsername(user.getUsername());
		UserLoginVo data = authServiceClient.login(userInfo).getData();
		// 动态生成头像URL
		userInfo.setAvatarUrl(ossServiceClient.generateAccessUrl(
				user.getAvatarKey(),
				user.getStorageType()
		).getData());
		userInfo.setToken(data.getToken());

		return userInfo;
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

		redisTemplate.delete(redisKey);
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
