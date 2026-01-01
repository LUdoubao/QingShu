package org.doubao.dialog.service.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.doubao.dialog.service.entity.DialogMessage;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 * 业务说明：适配对话业务场景的Redis缓存操作工具，包括会话列表缓存、未读消息数缓存、用户在线状态缓存等
 * 核心功能：1. 会话缓存管理 2. 消息缓存管理 3. 未读消息数管理 4. 用户在线状态管理
 * 适用场景：
 * 1. 会话列表缓存优化
 * 2. 消息历史记录缓存
 * 3. 未读消息数统计
 * 4. 用户在线状态维护
 * 封装Redis常用操作，解决序列化、过期时间、异常处理等通用问题
 */
@Component
public class RedisCacheUtil {
	// ========================= 新增：缓存键常量（会话/消息专用） =========================
	/** 单个会话缓存前缀：key格式=DIALOG_SESSION:{userId}:{sessionId} */
	public static final String DIALOG_SESSION_SINGLE_PREFIX = "DIALOG_SESSION:";
	/** 会话列表缓存前缀（ZSet类型，按最后消息时间排序）：key格式=DIALOG_SESSION_LIST_ZSET:{userId} */
	public static final String DIALOG_SESSION_LIST_ZSET_PREFIX = "DIALOG_SESSION_LIST_ZSET:";
	/** 单条消息缓存前缀：key格式=DIALOG_MESSAGE:{msgId} */
	public static final String DIALOG_MESSAGE_SINGLE_PREFIX = "DIALOG_MESSAGE:";
	/** 输入中状态缓存前缀：key格式=DIALOG_TYPING_STATUS:{userId} */
	public static final String DIALOG_TYPING_STATUS_PREFIX = "DIALOG_TYPING_STATUS:";

	/** 日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(RedisCacheUtil.class);
	/** Redis核心操作模板（Spring Data Redis提供，自动注入） */
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	/** ZSet操作对象 */
	private ZSetOperations<String, Object> zSetOperations;
	@PostConstruct
	public void init() {
		this.zSetOperations = redisTemplate.opsForZSet();
	}
	// ========================= 基础常量定义（对话业务专用） =========================
	/** 会话列表缓存前缀：key格式=DIALOG_SESSION_LIST:{userId} */
	public static final String DIALOG_SESSION_LIST_PREFIX = "DIALOG_SESSION_LIST:";
	/** 未读消息数缓存前缀：key格式=DIALOG_UNREAD_COUNT:{userId}，hashKey=sessionId */
	public static final String DIALOG_UNREAD_COUNT_PREFIX = "DIALOG_UNREAD_COUNT:";
	/** 用户在线状态缓存前缀：key格式=DIALOG_USER_ONLINE:{userId}，value=WebSocket会话ID */
	public static final String DIALOG_USER_ONLINE_PREFIX = "DIALOG_USER_ONLINE:";
	/** 分布式锁前缀：key格式=DIALOG_LOCK:{lockName} */
	public static final String DIALOG_LOCK_PREFIX = "DIALOG_LOCK:";

	// ========================= 新增：会话缓存操作 =========================
	/**
	 * 删除单个会话缓存（用户维度+会话维度）
	 * 业务说明：删除指定用户和会话的缓存数据，同步清理会话列表ZSet中的排序数据
	 * 业务流程：
	 * 1. 构建单个会话缓存Key
	 * 2. 删除单个会话缓存
	 * 3. 从会话列表ZSet中移除该会话
	 * 适用场景：会话删除、会话信息更新（先删缓存再更新数据库，避免脏数据）
	 * 参数校验：用户ID和会话ID不能为空
	 * 数据处理：同时清理单个会话缓存和列表排序数据
	 * @param userId 用户ID（会话归属者）
	 * @param sessionId 会话ID
	 */
	public void deleteSessionCache(Long userId, Long sessionId) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(sessionId)) {
			log.error("Redis deleteSessionCache failed | userId or sessionId is null");
			return;
		}

		// 1. 构建单个会话缓存Key
		String sessionSingleKey = buildSessionSingleKey(userId, sessionId);
		// 2. 删除单个会话缓存
		delete(sessionSingleKey);
		log.info("Redis deleteSessionCache success | userId: {}, sessionId: {}, key: {}",
				userId, sessionId, sessionSingleKey);

		// 3. 从会话列表ZSet中移除该会话（同步清理列表缓存）
		String sessionListZSetKey = buildSessionListZSetKey(userId);
		zSetOperations.remove(sessionListZSetKey, sessionId.toString());
		log.info("Redis remove session from list ZSet | userId: {}, sessionId: {}, key: {}",
				userId, sessionId, sessionListZSetKey);
	}

	/**
	 * 删除指定Key的缓存
	 * 业务说明：删除指定Key的缓存数据
	 * 业务流程：调用Redis模板删除指定Key
	 * 参数校验：缓存Key不能为空
	 * @param sessionSingleKey 需要删除的缓存Key
	 */
	public void delete(String sessionSingleKey) {
		redisTemplate.delete(sessionSingleKey);
	}

	/**
	 * 删除用户的全部会话列表缓存（ZSet类型）
	 * 业务说明：删除指定用户的所有会话列表缓存数据
	 * 业务流程：
	 * 1. 构建会话列表ZSet缓存Key
	 * 2. 删除ZSet类型的会话列表缓存
	 * 适用场景：用户会话列表批量更新、用户退出登录清理缓存
	 * 参数校验：用户ID不能为空
	 * @param userId 用户ID
	 */
	public void deleteSessionListCache(Long userId) {
		if (ObjectUtil.isNull(userId)) {
			log.warn("Redis deleteSessionListCache failed | userId is null");
			return;
		}

		String sessionListZSetKey = buildSessionListZSetKey(userId);
		delete(sessionListZSetKey);
		log.debug("Redis deleteSessionListCache success | userId: {}, key: {}",
				userId, sessionListZSetKey);
	}

	/**
	 * 获取会话的未读消息数（从Hash缓存中查询）
	 * 业务说明：从Redis Hash缓存中获取指定会话的未读消息数
	 * 业务流程：
	 * 1. 构建未读消息数Hash Key
	 * 2. 查询Hash中指定会话的未读数量
	 * 3. 返回未读消息数
	 * 依赖：未读消息数存储在DIALOG_UNREAD_COUNT:{userId}的Hash结构中，hashKey=sessionId
	 * 参数校验：用户ID和会话ID不能为空
	 * 数据处理：若缓存不存在返回0
	 * @param userId 用户ID
	 * @param sessionId 会话ID
	 * @return 未读消息数（null时返回0）
	 */
	public Integer getSessionUnreadCount(Long userId, Long sessionId) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(sessionId)) {
			log.error("Redis getSessionUnreadCount failed | userId or sessionId is null");
			return 0;
		}

		// 1. 构建未读消息数Hash Key
		String unreadCountHashKey = DIALOG_UNREAD_COUNT_PREFIX + userId;
		// 2. 查询Hash中指定会话的未读数量
		Integer unreadCount = hGet(unreadCountHashKey, sessionId.toString(), Integer.class);
		log.info("Redis getSessionUnreadCount success | userId: {}, sessionId: {}, unreadCount: {}",
				userId, sessionId, unreadCount);
		return unreadCount;
	}

	/**
	 * 从Hash中获取指定key的值
	 * 业务说明：从Redis Hash中获取指定字段的值并转换为指定类型
	 * 业务流程：
	 * 1. 调用Redis Hash操作获取字段值
	 * 2. 类型转换为Integer
	 * 参数校验：类型参数必须为Integer.class
	 * @param unreadCountHashKey Hash Key
	 * @param string Hash字段名
	 * @param integerClass 目标类型（Integer.class）
	 * @return 转换后的Integer值
	 */
	private Integer hGet(String unreadCountHashKey, String string, Class<Integer> integerClass) {
		Object object = redisTemplate.opsForHash().get(unreadCountHashKey, string);
		if (integerClass == Integer.class && object != null) {
			return (Integer) object;
		}
		return null;
	}

	/**
	 * 设置会话的未读消息数（覆盖式更新Hash缓存）
	 * 业务说明：设置指定会话的未读消息数到Redis Hash缓存中
	 * 业务流程：
	 * 1. 参数校验（用户ID、会话ID、未读数有效性）
	 * 2. 构建未读消息数Hash Key
	 * 3. 覆盖设置Hash中的未读数量
	 * 4. 刷新Hash缓存过期时间
	 * 适用场景：用户标记会话已读（未读数清零）、批量同步未读数
	 * 参数校验：用户ID和会话ID不能为空，未读数不能为负数
	 * 数据处理：设置7天过期时间，避免长期不活跃用户数据占用内存
	 * @param userId 用户ID
	 * @param sessionId 会话ID
	 * @param unreadCount 未读消息数（需≥0）
	 */
	public void setSessionUnreadCount(Long userId, Long sessionId, int unreadCount) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(sessionId)) {
			log.error("Redis setSessionUnreadCount failed | userId or sessionId is null");
			return;
		}
		if (unreadCount < 0) {
			log.error("Redis setSessionUnreadCount failed | unreadCount < 0 (userId: {}, sessionId: {}, count: {})",
					userId, sessionId, unreadCount);
			throw new IllegalArgumentException("未读消息数不能为负数");
		}

		// 1. 构建未读消息数Hash Key
		String unreadCountHashKey = DIALOG_UNREAD_COUNT_PREFIX + userId;
		// 2. 覆盖设置Hash中的未读数量
		hSet(unreadCountHashKey, sessionId.toString(), unreadCount);
		// 3. 刷新Hash缓存过期时间（7天，用户长期不活跃自动清理）
		expire(unreadCountHashKey, 7, TimeUnit.DAYS);
		log.info("Redis setSessionUnreadCount success | userId: {}, sessionId: {}, unreadCount: {}",
				userId, sessionId, unreadCount);
	}

	/**
	 * 获取单个会话缓存（从Value缓存中查询）
	 * 业务说明：从Redis Value缓存中获取指定会话的完整信息
	 * 业务流程：
	 * 1. 参数校验（用户ID和会话ID）
	 * 2. 构建单个会话缓存Key
	 * 3. 从Redis获取会话对象
	 * 4. 类型转换并返回
	 * 参数校验：用户ID和会话ID不能为空
	 * 数据处理：若缓存不存在返回null
	 * @param userId 用户ID（会话归属者）
	 * @param sessionId 会话ID
	 * @return 会话PO对象（缓存不存在返回null）
	 */
	public DialogSession getSessionCache(Long userId, Long sessionId) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(sessionId)) {
			log.error("Redis getSessionCache failed | userId or sessionId is null");
			return null;
		}

		String sessionSingleKey = buildSessionSingleKey(userId, sessionId);
		Object object = redisTemplate.opsForValue().get(sessionSingleKey);
		if (ObjectUtil.isNull(object)) {
			return null;
		}
		DialogSession sessionPO = (DialogSession) object;
		log.debug("Redis getSessionCache | userId: {}, sessionId: {}, exists: {}",
				userId, sessionId, ObjectUtil.isNotNull(sessionPO));
		return sessionPO;
	}

	/**
	 * 设置单个会话缓存（Value类型，带过期时间）
	 * 业务说明：将会话信息设置到Redis Value缓存中，带指定过期时间
	 * 业务流程：
	 * 1. 参数校验（用户ID、会话ID、会话对象、过期时间）
	 * 2. 构建单个会话缓存Key
	 * 3. 存储会话缓存（带过期时间）
	 * 4. 同步更新会话列表ZSet（若会话有最后消息时间）
	 * 适用场景：会话查询后缓存、会话信息更新后同步缓存
	 * 参数校验：用户ID、会话ID、会话对象不能为空，过期时间必须大于0
	 * 数据处理：同时更新单个会话缓存和列表排序数据
	 * @param userId 用户ID（会话归属者）
	 * @param sessionId 会话ID
	 * @param sessionPO 会话PO对象（需序列化）
	 * @param sessionCacheExpireSec 缓存过期时间（秒）
	 */
	public void setSessionCache(Long userId, Long sessionId, DialogSession sessionPO, Integer sessionCacheExpireSec) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(sessionId) || ObjectUtil.isNull(sessionPO)) {
			log.error("Redis setSessionCache failed | userId/sessionId/sessionPO is null");
			return;
		}
		if (ObjectUtil.isNull(sessionCacheExpireSec) || sessionCacheExpireSec <= 0) {
			log.error("Redis setSessionCache failed | invalid expire sec (userId: {}, sessionId: {}, sec: {})",
					userId, sessionId, sessionCacheExpireSec);
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CACHE_EXPIRE_TIME_INVALID);
		}

		// 1. 构建单个会话缓存Key
		String sessionSingleKey = buildSessionSingleKey(userId, sessionId);
		// 2. 存储会话缓存（带过期时间）
		setSession(sessionSingleKey, sessionPO, sessionCacheExpireSec, TimeUnit.SECONDS);
		log.info("Redis setSessionCache success | userId: {}, sessionId: {}, expireSec: {}",
				userId, sessionId, sessionCacheExpireSec);

		// 3. 同步更新会话列表ZSet（若会话列表存在，更新排序分数）
		if (ObjectUtil.isNotNull(sessionPO.getLastMsgTime())) {
			long score = sessionPO.getLastMsgTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			addSessionToListCache(userId, sessionId, score, sessionCacheExpireSec);
		}
	}

	/**
	 * 设置会话缓存
	 * 业务说明：将会话对象存储到Redis中，设置指定的过期时间
	 * 业务流程：调用Redis Value操作设置键值对和过期时间
	 * 参数校验：会话对象不能为空
	 * @param sessionSingleKey 缓存Key
	 * @param sessionPO 会话对象
	 * @param sessionCacheExpireSec 过期时间（秒）
	 * @param timeUnit 时间单位
	 */
	private void setSession(String sessionSingleKey, DialogSession sessionPO, Integer sessionCacheExpireSec, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(sessionSingleKey, sessionPO, sessionCacheExpireSec, timeUnit);
	}

	/**
	 * 将会话添加到用户的会话列表缓存（ZSet类型，按分数排序）
	 * 业务说明：将会话添加到用户会话列表的ZSet缓存中，按指定分数排序
	 * 业务流程：
	 * 1. 参数校验（用户ID、会话ID、过期时间）
	 * 2. 构建会话列表ZSet Key
	 * 3. 添加/更新ZSet中的会话（score更新会自动调整排序）
	 * 4. 刷新ZSet缓存过期时间
	 * 适用场景：创建新会话、接收新消息（更新会话排序）
	 * 参数校验：用户ID和会话ID不能为空，过期时间必须大于0
	 * 数据处理：使用最后消息时间戳作为排序分数，确保最新会话排在前面
	 * @param userId 用户ID
	 * @param sessionId 会话ID
	 * @param score 排序分数（建议用最后消息时间戳，确保最新会话排在前面）
	 * @param sessionCacheExpireSec 缓存过期时间（秒）
	 */
	public void addSessionToListCache(Long userId, Long sessionId, long score, Integer sessionCacheExpireSec) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(sessionId)) {
			log.error("Redis addSessionToListCache failed | userId or sessionId is null");
			return;
		}
		if (ObjectUtil.isNull(sessionCacheExpireSec) || sessionCacheExpireSec <= 0) {
			log.error("Redis addSessionToListCache failed | invalid expire sec (userId: {}, sessionId: {}, sec: {})",
					userId, sessionId, sessionCacheExpireSec);
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CACHE_EXPIRE_TIME_INVALID);
		}

		// 1. 构建会话列表ZSet Key
		String sessionListZSetKey = buildSessionListZSetKey(userId);
		// 2. 添加/更新ZSet中的会话（score更新会自动调整排序）
		zSetOperations.add(sessionListZSetKey, sessionId.toString(), score);
		// 3. 刷新ZSet缓存过期时间
		expire(sessionListZSetKey, sessionCacheExpireSec, TimeUnit.SECONDS);
		log.info("Redis addSessionToListCache success | userId: {}, sessionId: {}, score: {}, expireSec: {}",
				userId, sessionId, score, sessionCacheExpireSec);
	}


	// ========================= 新增：消息缓存操作 =========================
	/**
	 * 删除单个会话的所有消息缓存（批量删除，按消息ID前缀匹配）
	 * 业务说明：批量删除指定会话的所有消息缓存数据
	 * 业务流程：
	 * 1. 构建消息缓存Key的匹配前缀
	 * 2. 模糊查询所有匹配的消息Key
	 * 3. 批量删除消息缓存
	 * 适用场景：会话删除、消息批量清理
	 * 参数校验：会话ID不能为空
	 * 数据处理：使用模糊匹配删除会话下所有消息缓存
	 * @param sessionId 会话ID（消息所属会话）
	 */
	public void deleteMessageCache(Long sessionId) {
		if (ObjectUtil.isNull(sessionId)) {
			log.error("Redis deleteMessageCache failed | sessionId is null");
			return;
		}

		// 1. 构建消息缓存Key的匹配前缀（DIALOG_MESSAGE:{sessionId}:*）
		String messageKeyPrefix = buildMessageSingleKeyPrefix(sessionId);
		// 2. 模糊查询所有匹配的消息Key
		Set<String> messageKeys = redisTemplate.keys(messageKeyPrefix + "*");
		if (ObjectUtil.isEmpty(messageKeys) || messageKeys == null) {
			log.info("Redis deleteMessageCache | no message cache found (sessionId: {})", sessionId);
			return;
		}

		// 3. 批量删除消息缓存
		redisTemplate.delete(messageKeys);
		log.info("Redis deleteMessageCache success | sessionId: {}, deletedCount: {}",
				sessionId, messageKeys.size());
	}

	/**
	 * 获取单条消息缓存（从Value缓存中查询）
	 * 业务说明：从Redis Value缓存中获取指定消息的完整信息
	 * 业务流程：
	 * 1. 参数校验（消息ID）
	 * 2. 构建单条消息缓存Key
	 * 3. 从Redis获取消息对象
	 * 4. JSON反序列化并返回
	 * 参数校验：消息ID不能为空
	 * 数据处理：若缓存不存在返回null
	 * @param msgId 消息ID（格式建议：{sessionId}:{uuid}，确保唯一性）
	 * @return 消息PO对象（缓存不存在返回null）
	 */
	public DialogMessage getMessageCache(String msgId) {
		if (StrUtil.isBlank(msgId)) {
			log.warn("Redis getMessageCache failed | msgId is blank");
			return null;
		}

		String messageSingleKey = buildMessageSingleKey(msgId);
		DialogMessage messagePO = getMessage(messageSingleKey, DialogMessage.class);
		log.debug("Redis getMessageCache | msgId: {}, exists: {}",
				msgId, ObjectUtil.isNotNull(messagePO));
		return messagePO;
	}

	/**
	 * 获取消息对象
	 * 业务说明：从Redis获取指定Key的消息对象并反序列化
	 * 业务流程：
	 * 1. 从Redis获取原始对象
	 * 2. JSON序列化后再反序列化为目标类型
	 * 参数校验：消息类型必须为DialogMessage.class
	 * @param messageSingleKey 消息缓存Key
	 * @param dialogMessageClass 目标类型（DialogMessage.class）
	 * @return 反序列化后的消息对象
	 */
	private DialogMessage getMessage(String messageSingleKey, Class<DialogMessage> dialogMessageClass) {
		Object object = redisTemplate.opsForValue().get(messageSingleKey);
		if (object == null) {
			return null;
		}
		return JSON.parseObject(JSON.toJSONString(object), dialogMessageClass);
	}

	/**
	 * 设置单条消息缓存（Value类型，带过期时间）
	 * 业务说明：将消息信息设置到Redis Value缓存中，带指定过期时间
	 * 业务流程：
	 * 1. 参数校验（消息对象、消息ID、过期时间）
	 * 2. 构建单条消息缓存Key
	 * 3. 存储消息缓存（带过期时间）
	 * 适用场景：消息查询后缓存、消息发送后同步缓存
	 * 参数校验：消息对象和消息ID不能为空，过期时间必须大于0
	 * 数据处理：设置指定的过期时间
	 * @param messagePO 消息PO对象（需序列化）
	 * @param msgCacheExpireSec 缓存过期时间（秒）
	 */
	public void setMessageCache(DialogMessage messagePO, Integer msgCacheExpireSec) {
		if (ObjectUtil.isNull(messagePO) || StrUtil.isBlank(messagePO.getId())) {
			log.error("Redis setMessageCache failed | messagePO or msgId is null/blank");
			return;
		}
		if (ObjectUtil.isNull(msgCacheExpireSec) || msgCacheExpireSec <= 0) {
			log.error("Redis setMessageCache failed | invalid expire sec (msgId: {}, sec: {})",
					messagePO.getId(), msgCacheExpireSec);
			throw new IllegalArgumentException("消息缓存过期时间必须大于0秒");
		}

		String messageSingleKey = buildMessageSingleKey(messagePO.getId());
		// 存储消息缓存（带过期时间）
		setMessage(messageSingleKey, messagePO, msgCacheExpireSec, TimeUnit.SECONDS);
		log.debug("Redis setMessageCache success | msgId: {}, expireSec: {}",
				messagePO.getId(), msgCacheExpireSec);
	}

	/**
	 * 设置消息缓存
	 * 业务说明：将消息对象存储到Redis中，设置指定的过期时间
	 * 业务流程：调用Redis Value操作设置键值对和过期时间
	 * 参数校验：消息对象不能为空
	 * @param messageSingleKey 消息缓存Key
	 * @param messagePO 消息对象
	 * @param msgCacheExpireSec 过期时间（秒）
	 * @param timeUnit 时间单位
	 */
	private void setMessage(String messageSingleKey, DialogMessage messagePO, Integer msgCacheExpireSec, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(messageSingleKey, messagePO, msgCacheExpireSec, timeUnit);
	}


	// ========================= 新增：未读消息数递增操作 =========================
	/**
	 * 会话未读消息数原子递增（Hash类型，避免并发计数错误）
	 * 业务说明：将指定会话的未读消息数在Redis Hash中原子递增1
	 * 业务流程：
	 * 1. 参数校验（接收者ID、会话ID）
	 * 2. 构建未读消息数Hash Key
	 * 3. 原子递增1（若Hash或hashKey不存在，自动创建并初始化为1）
	 * 4. 刷新Hash缓存过期时间
	 * 适用场景：接收新消息时，递增对应会话的未读数
	 * 参数校验：接收者ID和会话ID不能为空
	 * 数据处理：使用原子操作避免并发问题，设置7天过期时间
	 * @param receiverId 接收者用户ID（未读数归属者）
	 * @param sessionId 会话ID
	 */
	public void incrementSessionUnreadCount(Long receiverId, Long sessionId) {
		if (ObjectUtil.isNull(receiverId) || ObjectUtil.isNull(sessionId)) {
			log.error("Redis incrementSessionUnreadCount failed | receiverId or sessionId is null");
			return;
		}

		// 1. 构建未读消息数Hash Key
		String unreadCountHashKey = DIALOG_UNREAD_COUNT_PREFIX + receiverId;
		// 2. 原子递增1（若Hash或hashKey不存在，自动创建并初始化为1）
		Long newUnreadCount = hIncrement(unreadCountHashKey, sessionId.toString(), 1);
		// 3. 刷新Hash缓存过期时间（7天）
		expire(unreadCountHashKey, 7, TimeUnit.DAYS);
		log.info("Redis incrementSessionUnreadCount success | receiverId: {}, sessionId: {}, newCount: {}",
				receiverId, sessionId, newUnreadCount);
	}

	/**
	 * Hash字段原子递增
	 * 业务说明：对Redis Hash中的指定字段进行原子递增操作
	 * 业务流程：调用Redis Hash的increment方法进行原子递增
	 * 参数校验：递增步长必须为整数
	 * @param unreadCountHashKey Hash Key
	 * @param hashKey Hash字段名
	 * @param i 递增步长
	 * @return 递增后的值
	 */
	private Long hIncrement(String unreadCountHashKey, String hashKey, int i) {
		return redisTemplate.opsForHash().increment(unreadCountHashKey, hashKey, i);
	}


	// ========================= 新增：内部工具方法（缓存Key构建） =========================
	/**
	 * 构建单个会话缓存Key（DIALOG_SESSION:{userId}:{sessionId}）
	 * 业务说明：根据用户ID和会话ID构建单个会话的Redis缓存Key
	 * 业务流程：使用格式化方法构建标准的会话缓存Key
	 * 参数校验：用户ID和会话ID不能为空
	 * @param userId 用户ID
	 * @param sessionId 会话ID
	 * @return 完整缓存Key
	 */
	private String buildSessionSingleKey(Long userId, Long sessionId) {
		return StrUtil.format("{}{}:{}", DIALOG_SESSION_SINGLE_PREFIX, userId, sessionId);
	}

	/**
	 * 构建会话列表ZSet缓存Key（DIALOG_SESSION_LIST_ZSET:{userId}）
	 * 业务说明：根据用户ID构建会话列表ZSet的Redis缓存Key
	 * 业务流程：使用格式化方法构建标准的会话列表缓存Key
	 * 参数校验：用户ID不能为空
	 * @param userId 用户ID
	 * @return 完整缓存Key
	 */
	private String buildSessionListZSetKey(Long userId) {
		return StrUtil.format("{}{}", DIALOG_SESSION_LIST_ZSET_PREFIX, userId);
	}

	/**
	 * 构建单条消息缓存Key（DIALOG_MESSAGE:{msgId}）
	 * 业务说明：根据消息ID构建单条消息的Redis缓存Key
	 * 业务流程：使用格式化方法构建标准的消息缓存Key
	 * 参数校验：消息ID不能为空
	 * @param msgId 消息ID
	 * @return 完整缓存Key
	 */
	private String buildMessageSingleKey(String msgId) {
		return StrUtil.format("{}{}", DIALOG_MESSAGE_SINGLE_PREFIX, msgId);
	}

	/**
	 * 构建会话下所有消息的缓存Key前缀（DIALOG_MESSAGE:{sessionId}:）
	 * 业务说明：根据会话ID构建该会话下所有消息缓存的Key前缀，用于模糊查询
	 * 业务流程：使用格式化方法构建消息缓存Key前缀
	 * 参数校验：会话ID不能为空
	 * 用途：用于模糊查询会话下的所有消息缓存
	 * @param sessionId 会话ID
	 * @return 消息缓存Key前缀
	 */
	private String buildMessageSingleKeyPrefix(Long sessionId) {
		return StrUtil.format("{}{}:", DIALOG_MESSAGE_SINGLE_PREFIX, sessionId);
	}

	/**
	 * 获取字符串类型的缓存值
	 * 业务说明：从Redis获取指定Key的字符串类型值
	 * 业务流程：
	 * 1. 检查目标类型是否为String
	 * 2. 从Redis获取原始对象
	 * 3. 转换为字符串并返回
	 * 参数校验：目标类型必须为String.class
	 * @param blacklistKey 缓存Key
	 * @param stringClass 目标类型（String.class）
	 * @return 字符串类型的缓存值
	 */
	public String getString(String blacklistKey, Class<String> stringClass) {
		if (stringClass == null) {
			return null;
		}
		if (stringClass == String.class) {
			Object object = redisTemplate.opsForValue().get(blacklistKey);
			if (object == null) {
				return null;
			}
			return object.toString();
		}
		return JSON.toJSONString(redisTemplate.opsForValue().get(blacklistKey));
	}

	/**
	 * 设置字符串类型的缓存值
	 * 业务说明：将字符串值设置到Redis中，带指定过期时间
	 * 业务流程：调用Redis Value操作设置键值对和过期时间
	 * 参数校验：字符串值不能为空
	 * @param onlineKey 缓存Key
	 * @param value 字符串值
	 * @param i 过期时间数值
	 * @param timeUnit 时间单位
	 */
	public void set(String onlineKey, String value, int i, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(onlineKey, value, i, timeUnit);
	}

	/**
	 * 设置缓存过期时间
	 * 业务说明：为指定的缓存Key设置过期时间
	 * 业务流程：调用Redis的expire方法设置过期时间
	 * 参数校验：过期时间必须大于0
	 * @param onlineKey 缓存Key
	 * @param i 过期时间数值
	 * @param timeUnit 时间单位
	 */
	public void expire(String onlineKey, int i, TimeUnit timeUnit) {
		redisTemplate.expire(onlineKey, i, timeUnit);
	}

	/**
	 * 设置Hash字段值
	 * 业务说明：在Redis Hash中设置指定字段的值
	 * 业务流程：调用Redis Hash的put方法设置字段值
	 * 参数校验：Hash Key和字段名不能为空
	 * @param unreadCountKey Hash Key
	 * @param string Hash字段名
	 * @param i 字段值
	 */
	public void hSet(String unreadCountKey, String string, int i) {
		redisTemplate.opsForHash().put(unreadCountKey, string, i);
	}
}