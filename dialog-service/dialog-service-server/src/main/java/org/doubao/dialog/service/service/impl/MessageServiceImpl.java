package org.doubao.dialog.service.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.dialog.service.config.DialogWebSocketHandler;
import org.doubao.dialog.service.dto.*;
import org.doubao.dialog.service.entity.AssistantDialog;
import org.doubao.dialog.service.entity.AssistantMessage;
import org.doubao.dialog.service.entity.DialogMessage;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.enums.ChatModule;
import org.doubao.dialog.service.enums.DialogStatusEnum;
import org.doubao.dialog.service.enums.MessagePushType;
import org.doubao.dialog.service.enums.SenderTypeEnum;
import org.doubao.dialog.service.feign.AIServiceClient;
import org.doubao.dialog.service.feign.UserFeignClient;
import org.doubao.dialog.service.mapper.AssistantDialogMapper;
import org.doubao.dialog.service.mapper.AssistantMessageMapper;
import org.doubao.dialog.service.mapper.DialogSessionMapper;
import org.doubao.dialog.service.messaging.DialogEventPublisher;
import org.doubao.dialog.service.req.MessageSendReq;
import org.doubao.dialog.service.service.*;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.doubao.dialog.service.vo.DialogQuery;
import org.doubao.dialog.service.vo.DialogVO;
import org.doubao.dialog.service.vo.MessageVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息管理服务实现类
 * 业务说明：处理对话消息的发送、接收、存储、推送等功能，支持AI助手对话和用户间私信
 * 核心逻辑：1. 消息存储（MongoDB）与会话同步（MySQL）2. 实时推送（WebSocket）与离线通知（MQ）3. 消息状态管理
 * 适用场景：
 * 1. AI助手对话功能
 * 2. 用户间私信系统
 * 3. 消息历史记录管理
 */
@Service
public class MessageServiceImpl extends ServiceImpl<AssistantMessageMapper, AssistantMessage> implements MessageService {
	// ===================== 常量定义 =====================
	/** AI助手固定ID */
	private static final Long AI_SENDER_ID = 10000L;
	/** 消息预览最大长度（20字） */
	private static final Integer MSG_PREVIEW_MAX_LEN = 20;
	/** 消息缓存过期时间（秒）- 30分钟 */
	private static final Integer MSG_CACHE_EXPIRE_SEC = 1800;

	// ===================== 依赖注入 =====================
	/** MongoDB操作模板，用于存储消息历史记录 */
	@Autowired
	private MongoTemplate mongoTemplate;

	/** 对话事件发布器，用于发送离线通知 */
	@Resource
	private DialogEventPublisher dialogEventPublisher;
	
	/** 会话Mapper，用于会话信息的持久化存储 */
	@Autowired
	private DialogSessionMapper sessionMapper;

	/** 会话服务，用于会话的创建、查询等操作 */
	@Autowired
	private SessionService sessionService;

	/** 用户Feign客户端，用于获取用户信息和检查聊天权限 */
	@Autowired
	private UserFeignClient userFeignClient;

	/** RabbitMQ模板，用于消息队列操作 */
	@Autowired
	private RabbitTemplate rabbitTemplate;

	/** WebSocket处理器，用于实时消息推送 */
	@Autowired
	private DialogWebSocketHandler webSocketHandler;

	/** Redis缓存工具，用于会话和消息的缓存管理 */
	@Autowired
	private RedisCacheUtil redisCacheUtil;

	/** 日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
	
	/** 对话Mapper，用于AI对话信息的持久化存储 */
	@Autowired
	private AssistantDialogMapper dialogMapper;

	/** 消息Mapper，用于AI消息的持久化存储 */
	@Autowired
	private AssistantMessageMapper messageMapper;

	/** 知识库服务，用于匹配用户问题并提供预设回复 */
	@Autowired
	private KnowledgeService knowledgeService;

	/** AI服务客户端，用于调用AI生成回复 */
	@Autowired
	private AIServiceClient aiServiceClient;

	/** WebSocket服务，用于消息推送 */
	@Autowired
	private WebSocketService webSocketService;

	/** AI服务最大token数配置 */
	@Value("${ai.max-tokens:200}")
	private Integer maxTokens;

	/** AI服务温度参数配置 */
	@Value("${ai.temperature:0.7}")
	private Double temperature;

	/**
	 * 处理用户消息并生成回复
	 * 业务说明：处理用户发送的消息，通过知识库匹配或AI调用生成回复，然后推送给用户
	 * 业务流程：
	 * 1. 检查是否需要创建新对话（无对话ID）
	 * 2. 保存用户消息到数据库
	 * 3. 优先匹配知识库获取预设回复
	 * 4. 知识库无匹配时检查是否需要人工接管
	 * 5. 不需要人工接管时调用AI服务生成回复
	 * 6. 保存AI回复并推送给用户
	 * 事务说明：使用事务确保消息保存和对话状态的一致性
	 * 异常处理：发生异常时回滚所有操作
	 * @param request 用户消息请求，包含用户ID、对话ID、消息内容等信息
	 * @return 回复消息对象，包含回复内容、发送时间等信息
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public MessageDTO handleUserMessage(MessageRequest request) {
		log.info("处理用户消息: {}", request);

		// 1. 获取或创建对话
		Long dialogId = request.getDialogId();
		String title = "";
		if (dialogId == null) {
			AssistantDialog dialog = createNewDialog(request);
			dialogId = dialog.getId();
			title = dialog.getTitle();
		}

		// 2. 保存用户消息
		saveMessage(dialogId, request.getUserId(), request.getContent(), SenderTypeEnum.USER.getCode());

		// 3. 生成回复（优先匹配知识库）
		String replyContent = knowledgeService.matchKnowledge(request.getContent());
		boolean isAdmin = false;

		// 4. 知识库无匹配时调用AI
		if (replyContent == null) {
			// 检查是否需要人工接管
			if (needAdminIntervene(request.getContent(), dialogId)) {
				// 通知管理员
				webSocketService.notifyAdmin(dialogId, request.getContent());
				replyContent = "正在为您连接管理员（预计1-3分钟）";
			} else {
				// 调用AI生成回复
				replyContent = callAiService(dialogId, request);
			}
		}

		// 5. 保存回复消息
		saveMessage(dialogId, null, replyContent, SenderTypeEnum.AI.getCode());

		// 6. 推送回复给用户（WebSocket）
		MessageDTO replyMessage = buildReplyMessage(dialogId, replyContent, isAdmin, SenderTypeEnum.AI.getCode(), title);
		webSocketService.pushToUser(request.getUserId(), replyMessage);

		// 7. 返回结果
		return replyMessage;
	}

	/**
	 * 获取用户的对话历史列表
	 * 业务说明：分页获取指定用户的对话历史记录，用于展示用户与AI的对话列表
	 * 业务流程：调用数据库查询方法按用户ID分页获取对话记录
	 * 参数校验：用户ID不能为空
	 * 数据处理：按创建时间倒序排列，每页返回指定数量的记录
	 * @param userId 用户ID，标识需要查询的用户
	 * @param pageNum 页码，从1开始
	 * @param pageSize 每页大小
	 * @return 分页的对话列表，包含对话ID、标题、状态等信息
	 */
	@Override
	public IPage<DialogVO> getUserDialogHistory(Long userId, int pageNum, int pageSize) {
		Page<DialogVO> page = new Page<>(pageNum, pageSize);
		return dialogMapper.selectByUserId(page, userId);
	}

	/**
	 * 获取对话详情
	 * 业务说明：获取指定对话的详细信息，包括基本信息和最后一条消息
	 * 业务流程：
	 * 1. 根据对话ID查询对话基本信息
	 * 2. 查询该对话的最后一条消息
	 * 3. 封装为VO对象返回
	 * 参数校验：对话ID不能为空
	 * 数据处理：若对话不存在返回null，若存在则补充最后一条消息信息
	 * @param dialogId 对话ID，标识需要查询的对话
	 * @return 对话详情对象，包含基本信息和最后消息
	 */
	@Override
	public DialogVO getDialogDetail(Long dialogId) {
		AssistantDialog dialog = dialogMapper.selectById(dialogId);
		if (dialog == null) {
			return null;
		}

		DialogVO vo = new DialogVO();
		vo.setDialogId(dialog.getId());
		vo.setUserId(dialog.getUserId());
		vo.setStatus(dialog.getStatus());
		vo.setCreatedTime(dialog.getCreatedTime());

		// 获取最后一条消息
		AssistantMessage lastMsg = messageMapper.selectLastByDialogId(dialogId);
		if (lastMsg != null) {
			vo.setLastMessage(lastMsg);
		}

		return vo;
	}

	/**
	 * 管理员获取对话列表
	 * 业务说明：分页获取需要处理的对话列表，支持按状态、用户、时间等条件筛选
	 * 业务流程：调用数据库查询方法按管理员查询条件分页获取对话记录
	 * 参数校验：查询条件、页码、页大小需符合基本规范
	 * 数据处理：按最后消息时间倒序排列，支持多种筛选条件
	 * @param query 查询条件，包含状态、用户ID、时间范围、关键词等筛选条件
	 * @param pageNum 页码，从1开始
	 * @param pageSize 每页大小
	 * @return 分页的对话列表，包含对话ID、用户信息、状态、最后消息等
	 */
	@Override
	public IPage<DialogVO> getAdminDialogList(DialogQuery query, int pageNum, int pageSize) {
		Page<DialogVO> page = new Page<>(pageNum, pageSize);
		return dialogMapper.selectByAdminQuery(page, query);
	}

	/**
	 * 更新对话状态
	 * 业务说明：更新指定对话的状态（如活跃、已解决、待跟进等）
	 * 业务流程：
	 * 1. 构建对话更新对象，设置新状态和更新时间
	 * 2. 调用数据库更新方法修改对话状态
	 * 3. 返回操作结果
	 * 事务说明：使用事务确保状态更新的一致性
	 * 异常处理：发生异常时回滚操作
	 * @param dialogId 对话ID，标识需要更新状态的对话
	 * @param status 新的状态值，参考DialogStatusEnum
	 * @return 操作是否成功，true=成功，false=失败
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateDialogStatus(Long dialogId, Integer status) {
		AssistantDialog dialog = new AssistantDialog();
		dialog.setId(dialogId);
		dialog.setStatus(status);
		dialog.setUpdatedTime(LocalDateTime.now());
		return dialogMapper.updateById(dialog) > 0;
	}

	/**
	 * 创建新对话
	 * 业务说明：为用户创建新的AI对话记录，包括生成对话标题和保存对话信息
	 * 业务流程：
	 * 1. 调用AI服务根据用户消息内容生成对话标题
	 * 2. 构建对话对象，设置用户ID、标题、状态等信息
	 * 3. 保存对话记录到数据库
	 * 4. 返回创建的对话对象
	 * 参数校验：用户请求不能为空
	 * 数据处理：对话状态默认设为活跃，创建和更新时间设为当前时间
	 * @param request 用户消息请求，包含用户ID、消息内容等信息
	 * @return 创建的对话对象，包含对话ID、标题等信息
	 */
	private AssistantDialog createNewDialog(MessageRequest  request) {
		String title = callAiService(null, request);
		log.info("==============AI服务返回的标题：{}", title);
		AssistantDialog dialog = new AssistantDialog();
		dialog.setUserId(request.getUserId());
		dialog.setTitle(title);
		dialog.setStatus(DialogStatusEnum.ACTIVE.getCode());
		dialog.setCreatedTime(LocalDateTime.now());
		dialog.setUpdatedTime(LocalDateTime.now());
		dialogMapper.insert(dialog);
		return dialog;
	}

	/**
	 * 保存消息
	 * 业务说明：将消息保存到数据库，用于记录对话历史
	 * 业务流程：
	 * 1. 构建消息对象，设置对话ID、发送者ID、内容、发送者类型等信息
	 * 2. 设置消息发送时间为当前时间
	 * 3. 保存消息记录到数据库
	 * 参数校验：对话ID、内容不能为空
	 * 数据处理：消息发送时间设为当前时间
	 * @param dialogId 对话ID，标识消息所属对话
	 * @param senderId 发送者ID，标识消息发送方
	 * @param content 消息内容，存储实际的消息文本
	 * @param senderType 发送者类型，标识是用户还是AI发送
	 */
	private void saveMessage(Long dialogId, Long senderId, String content, int senderType) {
		AssistantMessage message = new AssistantMessage();
		message.setDialogId(dialogId);
		message.setSenderId(senderId);
		message.setContent(content);
		message.setSenderType(senderType);
		message.setSendTime(LocalDateTime.now());
		messageMapper.insert(message);
	}

	/**
	 * 构建回复消息
	 * 业务说明：构建用于返回给前端的回复消息对象
	 * 业务流程：
	 * 1. 创建消息DTO对象，设置对话ID、内容、是否为管理员回复、发送时间、发送者类型
	 * 2. 若标题不为空则设置到消息对象
	 * 3. 返回构建的消息对象
	 * 参数校验：对话ID、内容不能为空
	 * 数据处理：发送时间设为当前时间，标题可选设置
	 * @param dialogId 对话ID，标识消息所属对话
	 * @param content 消息内容，存储实际的消息文本
	 * @param isAdmin 是否为管理员回复
	 * @param senderType 发送者类型，标识消息发送方类型
	 * @param title 对话标题，可选参数
	 * @return 构建的消息DTO对象
	 */
	private MessageDTO buildReplyMessage(Long dialogId, String content, boolean isAdmin, int senderType, String  title) {
		MessageDTO messageDTO = new MessageDTO(
				dialogId,
				content,
				isAdmin,
				LocalDateTime.now(),
				senderType
		);
		if (title != null  && !title.isEmpty()) {
			messageDTO.setTitle(title);
		}
		return messageDTO;
	}

	/**
	 * 判断是否需要人工接管
	 * 业务说明：根据用户消息内容和对话历史判断是否需要人工（管理员）介入处理
	 * 业务流程：
	 * 1. 检查消息内容是否包含人工、管理员等关键词
	 * 2. 检查AI是否连续多次无法提供有效回复
	 * 3. 返回是否需要人工接管的判断结果
	 * 参数校验：消息内容和对话ID不能为空
	 * 智能判断：结合关键词匹配和AI回复质量进行综合判断
	 * @param content 用户发送的消息内容
	 * @param dialogId 对话ID，用于查询对话历史
	 * @return 是否需要人工接管，true=需要，false=不需要
	 */
	private boolean needAdminIntervene(String content, Long dialogId) {
		// 1. 包含关键词（人工、管理员等）
		if (content.contains("人工") || content.contains("管理员")) {
			return true;
		}

		// 2. AI连续2次无法回复
		List<AssistantMessage> lastMessages = messageMapper.selectLastNByDialogId(dialogId, 4);
		if (lastMessages.size() >= 4) {
			// 检查最近4条消息是否为"用户-AI-用户-AI"且AI回复为默认内容
			boolean isNeed = true;
			for (int i = 0; i < 4; i += 2) {
				if (!Objects.equals(lastMessages.get(i).getSenderType(), SenderTypeEnum.USER.getCode())) {  // 非用户消息
					isNeed = false;
					break;
				}
				if (!Objects.equals(lastMessages.get(i + 1).getSenderType(), SenderTypeEnum.AI.getCode())) {  // 非AI消息
					isNeed = false;
					break;
				}
				String aiReply = lastMessages.get(i+1).getContent();
				if (!aiReply.contains("无法回复") && !aiReply.contains("不理解")) {
					isNeed = false;
					break;
				}
			}
			return isNeed;
		}

		return false;
	}

	/**
	 * 调用AI服务生成回复
	 * 业务说明：构建对话上下文并调用AI服务生成回复内容
	 * 业务流程：
	 * 1. 构建AI对话上下文（包含系统提示、历史消息、当前消息）
	 * 2. 设置AI请求参数（用户ID、消息、AI类型、模型等）
	 * 3. 调用AI服务客户端生成回复
	 * 4. 处理AI服务响应或异常情况
	 * 异常处理：AI服务调用失败时返回默认提示语
	 * 参数校验：消息请求不能为空
	 * @param dialogId 对话ID，用于获取对话历史，可为空（创建标题时）
	 * @param messageRequest 消息请求对象，包含用户ID、内容、AI类型等信息
	 * @return AI生成的回复内容，调用失败时返回默认提示
	 */
	private String callAiService(Long dialogId, MessageRequest messageRequest) {
		try {
			// 构建对话上下文
			List<ChatMessage> messages = buildAIChatContext(dialogId, messageRequest);

			// 调用AI服务
			AIRequest request = new AIRequest();
			request.setUserId(String.valueOf(messageRequest.getUserId()));
			request.setMessages(messages);
			request.setAiType(messageRequest.getAiType());
			request.setModel(messageRequest.getAiModel());
			request.setMaxTokens(maxTokens);
			request.setTemperature(temperature);

			AIResponse response = aiServiceClient.generateReply(request).getData();

			if (response.getSuccess() != null && !response.getSuccess()) {
				log.error("AI服务返回错误: {}", response.getErrorMsg());
				return "抱歉，暂时无法理解您的问题，请换一种方式提问";
			}

			return response.getContent();
		} catch (Exception e) {
			log.error("调用AI服务失败", e);
			return "抱歉，暂时无法理解您的问题，请换一种方式提问";
		}
	}

	/**
	 * 构建AI对话上下文
	 * 业务说明：根据对话历史和当前消息构建AI服务所需的对话上下文
	 * 业务流程：
	 * 1. 确定对话模块类型（诗词、标题、帮助等）
	 * 2. 设置系统提示词（根据模块类型）
	 * 3. 添加历史消息（倒序排列，最新的在前）
	 * 4. 添加当前用户消息
	 * 参数校验：消息请求不能为空
	 * 数据处理：历史消息最多取最近6条，按时间倒序排列
	 * @param dialogId 对话ID，用于获取历史消息，可为空（创建标题时）
	 * @param messageRequest 消息请求对象，包含模块类型、内容等信息
	 * @return 构建的对话上下文列表
	 */
	private List<ChatMessage> buildAIChatContext(Long dialogId,  MessageRequest messageRequest) {
		List<ChatMessage> context = new ArrayList<>();
		List<AssistantMessage> history = new ArrayList<>(6);
		if (dialogId != null) {
			history = messageMapper.selectLastNByDialogId(dialogId, 6);  // 取最近6条
		}
		String systemContent = "";
		ChatModule chatModule;
		if (dialogId == null) {
			chatModule = ChatModule.CHAT_TITLE;
		} else {
			String module = messageRequest.getModule();
			// 构造ChatModule
			chatModule = ChatModule.getByValue(module);
			if (chatModule == null) {
				chatModule = ChatModule.CHAT_HELP;
			}
		}
		switch (chatModule) {
			case CHAT_POETRY:
				systemContent = "你是引文网站的智能助手，仅回答与诗词相关的问题（如诗词全文、诗词注释、诗词赏析等）。" +
						"若问题与诗词无关，回复：'抱歉，我只能回答与诗词相关的问题哦~'" +
						"请用中文回答。" +
						"字数不超过两百字";
				break;
			case CHAT_TITLE:
				systemContent = "你是引文网站的智能助手。\n" +
						"\n" +
						"**你的唯一任务是：根据用户当前发送的消息内容，概括其核心意图，生成一个简短的对话标题。**\n" +
						"\n" +
						"**标题要求：**\n" +
						"1.  **仅基于用户当前消息的意图概括。**\n" +
						"2.  **字数：5 到 10 个中文字符。**\n" +
						"3.  **直接输出标题，禁止回答消息内容本身或执行任何操作。**\n" +
						"4.  **不得添加任何解释、说明或其他额外信息。**\n" +
						"\n" +
						"**注意：你只需要生成标题，不需要执行用户请求的操作或回复请求内容。**" +
						"例如：用户发送消息：'你好，如何创建引文，你能帮助我吗' ,你生成并回复的对话标题应该是: '如何创建引文' ";
				break;
			case CHAT_HELP:
			default:
				systemContent = "你是引文网站的智能助手，仅回答与网站功能相关的问题（如创建引文、添加标签等）。" +
						"网站功能包括：翎枢阁（首页）、墨渊境（引文列表）、采翎编（引文管理）、执翎台（新建引文）等。" +
						"请用中文回答。" +
						"若问题与网站功能无关，回复：'抱歉，我只能回答与引文网站相关的问题哦~'";
		}
		log.info("generateReply: {}", chatModule.getName());


		ChatMessage system = new ChatMessage(
				"system",
				systemContent
		);
		// 添加系统提示
		context.add(system);

		if (!history.isEmpty()) {
			// 倒序添加历史消息（最早的在前）
			for (int i = history.size() - 1; i >= 0; i--) {
				AssistantMessage msg = history.get(i);
				String role = Objects.equals(msg.getSenderType(), SenderTypeEnum.USER.getCode())
						? "user"
						: "assistant";
				context.add(new ChatMessage(role, msg.getContent()));
			}
		}

		// 添加当前消息
		context.add(new ChatMessage("user", messageRequest.getContent()));
		return context;
	}


	// ===================== 私信接口实现 =====================

	/**
	 * 发送消息核心逻辑：
	 * 1. 校验会话归属（发送者是否为会话参与者）
	 * 2. 构建消息PO并插入MongoDB
	 * 3. 更新会话最后消息信息（MySQL）
	 * 4. 更新接收方未读计数（Redis+MySQL）
	 * 5. 推送消息：在线用WebSocket，离线用MQ触发系统通知
	 * 业务说明：处理用户间私信消息的发送，包括校验、存储、推送等完整流程
	 * 业务流程：
	 * 1. 校验会话和发送者合法性
	 * 2. 校验消息内容合规性
	 * 3. 构建消息对象并保存到MongoDB
	 * 4. 更新会话的最后消息信息
	 * 5. 增加接收方未读计数
	 * 6. 推送消息给接收者
	 * 事务说明：使用事务确保数据一致性
	 * 参数校验：会话ID、内容等必须符合规范
	 * @param sendReq 消息发送请求，包含会话ID、内容、类型等信息
	 * @param senderId 发送者ID，标识消息发送方
	 * @return 保存的消息ID，用于后续操作
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public String sendMessage(MessageSendReq sendReq, Long senderId) {
		// 1. 解析请求参数
		Long sessionId = sendReq.getSessionId();
		String content = sendReq.getContent();
		String extInfo = sendReq.getExtInfo();
		String tmpId = "";
		if (extInfo != null && !extInfo.isEmpty()) {
			JSONObject parse = (JSONObject) JSONObject.parse(extInfo);
			tmpId = parse.getString("tempId");
		}
		DialogMessage.ContentTypeEnum contentType = DialogMessage.ContentTypeEnum.valueOf(sendReq.getContentType());

		// 2. 校验会话合法性（发送者是否为会话参与者）
		DialogSession sessionPO = validateSessionAndSender(sessionId, senderId);

		// 3. 确定接收者ID（会话所有者为senderId则接收者是targetId，反之亦然）
		Long receiverId = Objects.equals(sessionPO.getUserId(), senderId)
				? sessionPO.getTargetId()
				: sessionPO.getUserId();
		// 3.1 校验接收方隐私权限
		Boolean check = userFeignClient.checkChatPermission(receiverId, senderId).getData();
		if (!check) {
			throw new BusinessException(ErrorCode.USER_CHAT_PRIVACY_NOT_OPEN);
		}
		// 4. 校验消息内容（非空、长度限制）
		validateMessageContent(content, contentType);

		// 5. 构建消息PO并插入MongoDB
		DialogMessage messagePO = buildMessagePO(sessionId, senderId, receiverId, content, contentType);
		DialogMessage savedMsg = mongoTemplate.insert(messagePO);

		// 6. 更新会话最后消息信息（MySQL）
		updateSessionLastMsg(sessionPO, savedMsg, content, contentType);

		// 7. 处理接收方未读计数（AI接收者无需未读）
		if (!Objects.equals(receiverId, AI_SENDER_ID)) {
			incrementReceiverUnreadCount(receiverId, sessionId);
		}

		// 8.通知发送方消息已发送成功
		if (tmpId != null && !tmpId.isEmpty()) {
			webSocketHandler.sendMsgSuccess(
					messagePO.getSenderId(),
					messagePO.getId(),
					tmpId
			);
		}

		// 推送消息给接收者
		pushMessageToReceiver(savedMsg, receiverId, sessionPO);

		// 9. 返回消息ID（MongoDB的ObjectId）
		return String.valueOf(savedMsg.getId());
	}

	/**
	 * 修复后的查询历史消息核心逻辑：
	 * 1. 校验当前用户与会话的关联关系
	 * 2. 获取双方独立会话ID（当前用户的会话 + 对方用户的会话）
	 * 3. MongoDB分页查询双方会话下的所有消息（按发送时间倒序）
	 * 4. 转换为VO并补充发送者信息（头像、昵称）
	 * 5. 返回分页结果
	 * 业务说明：查询用户与对方的私信历史记录，支持分页获取
	 * 业务流程：
	 * 1. 校验当前用户与会话的关联关系
	 * 2. 获取双方独立会话ID
	 * 3. 构建MongoDB分页查询条件
	 * 4. 执行查询并统计总数
	 * 5. 转换为VO并补充发送者信息
	 * 6. 标记当前用户接收的消息为已读
	 * 参数校验：会话ID、用户ID不能为空
	 * 数据处理：按发送时间倒序排列，支持分页查询
	 * @param sessionId 会话ID，标识需要查询的会话
	 * @param userId 用户ID，标识当前操作用户
	 * @param pageNum 页码，从1开始
	 * @param pageSize 每页大小
	 * @return 分页的消息列表，包含消息详情和发送者信息
	 */
	@Override
	public Page<MessageVO> getMessageHistory(Long sessionId, Long userId, Integer pageNum, Integer pageSize) {
		// 1. 校验当前用户的会话合法性（获取当前用户的会话，提取聊天对方ID）
		DialogSession currentUserSession = sessionService.getSessionByIdAndUserId(sessionId, userId);
		if (currentUserSession == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}
		// 确认当前用户是会话参与者（防非法访问）
		if (!Objects.equals(currentUserSession.getUserId(), userId) && !Objects.equals(currentUserSession.getTargetId(), userId)) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_ALLOW_SEE);
		}

		// 2. 获取聊天对方ID（targetId），查询对方与当前用户的独立会话
		Long targetId = Objects.equals(currentUserSession.getUserId(), userId)
				? currentUserSession.getTargetId()
				: currentUserSession.getUserId();
		// 查询对方用户的会话（对方作为userId，当前用户作为targetId）
		DialogSession targetUserSession = getTargetUserSession(targetId, userId);
		log.info("targetUserSession: {}", JSONObject.toJSONString(targetUserSession));

		// 3. 收集双方会话ID（过滤null，避免空指针）
		List<Long> sessionIds = new ArrayList<>();
		sessionIds.add(currentUserSession.getId());
		if (targetUserSession != null) {
			sessionIds.add(targetUserSession.getId());
		}
		log.info("sessionIds: {}", JSONObject.toJSONString(sessionIds));

		// 4. 构建MongoDB分页查询条件（查询双方会话下的所有消息，按发送时间倒序）
		long skip = (long) (pageNum - 1) * pageSize; // 计算跳过条数（MongoDB分页从0开始）
		Criteria criteria = Criteria.where("sessionId").in(sessionIds) // 关键：查询双方会话ID
				.and("deleted").is(0); // 排除已删除消息
		Query query = Query.query(criteria)
				.with(Sort.by(Sort.Direction.DESC, "sendTime")) // 按发送时间倒序（最新消息在前）
				.with(Sort.by(Sort.Direction.DESC, "_id")) // 同一时间消息按ID倒序，避免乱序
				.skip(skip)
				.limit(pageSize);

		// 5. 执行查询（统计总数 + 查询消息列表）
		long total = mongoTemplate.count(Query.query(criteria), DialogMessage.class); // 修正：查询消息表（DialogMessage）
		log.info("total: {}", total);

		List<DialogMessage> messagePOList = mongoTemplate.find(query, DialogMessage.class);
		log.info("messagePOList: {}", JSONObject.toJSONString(messagePOList));

		// 6. 转换为VO并补充发送者信息（保持原有逻辑，新增消息方向判断）
		List<MessageVO> messageVOList = messagePOList.stream()
				.map(messagePO -> convertToMessageVO(messagePO, userId, sessionId))
				.collect(Collectors.toList());
		log.info("messageVOList: {}", JSONObject.toJSONString(messageVOList));

		// 7. 构建分页结果
		Page<MessageVO> resultPage = new Page<>(pageNum, pageSize);
		resultPage.setTotal(total);
		resultPage.setRecords(messageVOList);

		// 8. 标记当前用户接收的消息为已读（优化：查询后自动标已读）
		markReceivedMessagesAsRead(sessionIds, userId, messagePOList);

		return resultPage;
	}
	/**
	 * 标记当前用户接收的消息为已读（批量处理）
	 * @param sessionIds 双方会话ID列表
	 * @param currentUserId 当前用户ID（接收者）
	 * @param messagePOList 已查询的消息列表
	 */
	private void markReceivedMessagesAsRead(List<Long> sessionIds, Long currentUserId, List<DialogMessage> messagePOList) {
		if (messagePOList.isEmpty()) {
			return;
		}
		List<String> unreadMsgIds = messagePOList.stream()
				.filter(msg -> Objects.equals(msg.getReceiverId(), currentUserId))
				.filter(msg -> Objects.equals(msg.getStatus(), DialogMessage.MessageStatusEnum.SENT))
				.map(DialogMessage::getId)
				.collect(Collectors.toList());
		log.info("-----------------------unreadMsgIds: {}", JSONObject.toJSONString(unreadMsgIds));
		if (unreadMsgIds.isEmpty()) {
			return;
		}
		// 批量标记已读（复用原有逻辑，遍历双方会话ID处理）
		for (Long sessionId : sessionIds) {
			try {
				markMessagesAsRead(sessionId, currentUserId, unreadMsgIds);
			} catch (Exception e) {
				log.warn("标记会话[{}]消息已读失败", sessionId, e);
			}
		}
	}

	/**
	 * 查询对方用户与当前用户的独立会话
	 * 业务说明：查询对方用户与当前用户之间的独立会话，用于构建双方消息查询条件
	 * 业务流程：
	 * 1. 根据对方用户ID和当前用户ID构建查询条件
	 * 2. 查询数据库获取对方用户的会话记录
	 * 3. 若会话不存在则自动创建
	 * 4. 返回会话对象
	 * 参数校验：对方用户ID和当前用户ID不能为空
	 * 数据处理：若会话不存在则创建新的会话记录并更新缓存
	 * @param targetUserId 对方用户ID（会话所有者）
	 * @param currentUserId 当前用户ID（会话目标）
	 * @return 对方用户的会话对象，若不存在则创建并返回
	 */
	private DialogSession getTargetUserSession(Long targetUserId, Long currentUserId) {
		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getUserId, targetUserId) // 对方是会话所有者
				.eq(DialogSession::getTargetId, currentUserId) // 目标是当前用户
				.eq(DialogSession::getSessionType, "USER") // 仅用户间会话（排除AI会话）
				.eq(DialogSession::getDeleted, 0) // 未删除
				.last("limit 1"); // 确保只返回一条（唯一索引保障）
		DialogSession dialogSession = sessionMapper.selectOne(queryWrapper);
		if (dialogSession == null) {
			log.info("对方未创建会话");
			dialogSession = new DialogSession();
			dialogSession.setUserId(targetUserId);
			dialogSession.setTargetId(currentUserId);
			dialogSession.setSessionType(DialogSession.SessionTypeEnum.USER);
			dialogSession.setCreatedId(currentUserId);
			dialogSession.setUpdatedId(currentUserId);
			sessionMapper.insert(dialogSession);
			redisCacheUtil.deleteSessionCache(targetUserId, dialogSession.getId());
			redisCacheUtil.deleteSessionListCache(targetUserId);
		}
		return dialogSession;
	}

	/**
	 * 重发失败消息核心逻辑：
	 * 1. 校验消息归属（发送者是否为消息所有者）
	 * 2. 校验消息状态（必须是FAILED）
	 * 3. 更新消息状态为SENT（MongoDB）
	 * 4. 重新推送消息给接收者
	 * 业务说明：重新发送之前发送失败的消息，确保消息可靠传递
	 * 业务流程：
	 * 1. 校验消息归属和状态
	 * 2. 更新消息状态为已发送
	 * 3. 重新查询消息信息
	 * 4. 重新推送消息给接收者
	 * 事务说明：使用事务确保状态更新和推送的一致性
	 * 参数校验：消息ID和发送者ID不能为空，消息状态必须为FAILED
	 * @param msgId 消息ID，标识需要重发的消息
	 * @param senderId 发送者ID，标识消息的原始发送者
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resendMessage(String msgId, Long senderId) {
		// 1. 查询消息并校验归属
		DialogMessage messagePO = getMessageByIdAndSenderId(msgId, senderId);
		if (messagePO == null) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_EXIST);
		}

		// 2. 校验消息状态（仅FAILED状态可重发）
		if (messagePO.getStatus() != DialogMessage.MessageStatusEnum.FAILED) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW_RESEND);
		}

		// 3. 更新消息状态为SENT（MongoDB）
		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.SENT.getValue())
				.set("sendTime", LocalDateTime.now()) // 重发时间更新为当前时间
				.set("updatedAt", LocalDateTime.now()); // 新增updatedAt字段（需在PO中添加）
		mongoTemplate.updateFirst(
				Query.query(Criteria.where("_id").is(msgId)),
				update,
				DialogSession.class
		);

		// 4. 重新查询更新后的消息
		messagePO = mongoTemplate.findById(msgId, DialogMessage.class);
		if (messagePO == null) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW_RESEND_QUERY);
		}

		// 5. 查询会话信息（用于推送）
		DialogSession sessionPO = sessionService.getSessionByIdAndUserId(messagePO.getSessionId(), senderId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_EXIST_IN_SESSION);
		}

		// 6. 重新推送消息给接收者
		pushMessageToReceiver(messagePO, messagePO.getReceiverId(), sessionPO);
	}

	/**
	 * 清空会话消息核心逻辑：
	 * 1. 校验会话归属
	 * 2. 物理删除MongoDB中该会话的所有消息
	 * 3. 重置会话最后消息信息（MySQL）
	 * 4. 删除消息缓存
	 * 业务说明：清空指定会话的所有消息记录，用于用户清理聊天记录
	 * 业务流程：
	 * 1. 校验会话归属（当前用户是否为会话参与者）
	 * 2. 物理删除MongoDB中该会话的所有消息
	 * 3. 重置会话的最后消息信息
	 * 4. 同步缓存和未读计数
	 * 事务说明：使用事务确保数据一致性
	 * 参数校验：会话ID和用户ID不能为空
	 * @param sessionId 会话ID，标识需要清空消息的会话
	 * @param userId 用户ID，标识操作用户
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void clearSessionMessages(Long sessionId, Long userId) {
		// 1. 校验会话归属
		DialogSession sessionPO = sessionService.getSessionByIdAndUserId(sessionId, userId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}

		// 2. 物理删除MongoDB中该会话的所有消息
		Query query = Query.query(Criteria.where("sessionId").is(sessionId));
		mongoTemplate.remove(query, DialogSession.class);

		// 3. 重置会话最后消息信息（MySQL）
		sessionPO.setLastMsgId(null);
		sessionPO.setLastMsgContent(null);
		sessionPO.setLastMsgTime(LocalDateTime.now());
		sessionPO.setUnreadCount(0); // 清空消息时同步清零未读
		sessionPO.setUpdatedTime(LocalDateTime.now());
		sessionMapper.updateById(sessionPO);

		// 4. 同步缓存（重置会话信息+清空未读计数）
		redisCacheUtil.setSessionCache(userId, sessionId, sessionPO, MSG_CACHE_EXPIRE_SEC);
		redisCacheUtil.setSessionUnreadCount(userId, sessionId, 0);

		// 5. 删除消息缓存（若有）
		redisCacheUtil.deleteMessageCache(sessionId);
	}

	/**
	 * 标记消息为已读核心逻辑：
	 * 1. 校验会话归属（当前用户是否为接收者）
	 * 2. 构建查询条件（msgIds为空则查询所有未读消息）
	 * 3. 更新消息状态为READ（MongoDB）
	 * 4. 推送已读状态给发送者（WebSocket）
	 * 5. 同步未读计数（清零）
	 * 业务说明：将指定消息标记为已读状态，更新未读计数
	 * 业务流程：
	 * 1. 校验会话归属和接收者身份
	 * 2. 构建MongoDB查询条件
	 * 3. 统计未读消息数量
	 * 4. 更新消息状态为已读
	 * 5. 同步未读计数
	 * 6. 推送已读状态给发送者
	 * 事务说明：使用事务确保状态更新的一致性
	 * 参数校验：会话ID、接收者ID不能为空
	 * @param sessionId 会话ID，标识消息所属会话
	 * @param receiverId 接收者ID，标识消息接收方
	 * @param msgIds 消息ID列表，为空表示标记所有未读消息
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void markMessagesAsRead(Long sessionId, Long receiverId, List<String> msgIds) {
		// 1. 校验会话归属（当前用户是否为接收者）
		DialogSession sessionPO = sessionService.getSessionByIdAndReceiverId(sessionId, receiverId);
		if (sessionPO == null) {
			log.warn("会话不存在");
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}
		DialogSession mySession = getTargetUserSession(receiverId, sessionPO.getUserId());
		// 2. 构建MongoDB查询条件
		Criteria criteria = Criteria.where("sessionId").is(sessionId)
				.and("receiverId").is(receiverId)
				.and("status").is(DialogMessage.MessageStatusEnum.SENT.getValue()); // 仅SENT状态可标记为READ

		// 若msgIds不为空，添加消息ID条件
		if (!msgIds.isEmpty()) {
			criteria.and("_id").in(msgIds);
		}

		Query query = Query.query(criteria);

		// 3. 统计未读消息数量（用于后续同步未读计数）
		long unreadCount = mongoTemplate.count(query, DialogMessage.class);
		if (unreadCount == 0) {
			return; // 无未读消息，无需操作
		}

		// 4. 更新消息状态为READ（MongoDB）
		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.READ.getValue())
				.set("readTime", LocalDateTime.now())
				.set("updatedAt", LocalDateTime.now());
		mongoTemplate.updateMulti(query, update, DialogMessage.class);

		// 5. 同步未读计数（清零）
		sessionService.clearSessionUnread(receiverId, mySession.getId());

		// 6. 推送已读状态给发送者（获取发送者ID并推送）
		Long senderId = Objects.equals(sessionPO.getUserId(), receiverId)
				? sessionPO.getTargetId()
				: sessionPO.getUserId();
		pushReadStatusToSender(senderId, sessionId, msgIds);
	}

	/**
	 * 根据消息ID和发送者ID查询消息（用于重发校验）
	 * 优先从缓存查询，缓存不存在则从MongoDB查询并同步到缓存
	 * 业务说明：根据消息ID和发送者ID查询消息记录，用于重发消息校验
	 * 业务流程：
	 * 1. 优先从缓存查询消息
	 * 2. 缓存不存在则从MongoDB查询
	 * 3. MongoDB查询结果同步到缓存
	 * 4. 返回查询结果
	 * 参数校验：消息ID和发送者ID不能为空
	 * 数据处理：查询结果会同步到缓存以提高后续查询效率
	 * @param msgId 消息ID，标识需要查询的消息
	 * @param senderId 发送者ID，标识消息发送方
	 * @return 消息对象，若不存在返回null
	 */
	@Override
	public DialogMessage getMessageByIdAndSenderId(String msgId, Long senderId) {
		// 1. 优先从缓存查询
		DialogMessage messagePO = redisCacheUtil.getMessageCache(msgId);
		if (messagePO != null) {
			// 校验消息归属（缓存可能过期，需二次校验）
			if (Objects.equals(messagePO.getSenderId(), senderId)) {
				return messagePO;
			} else {
				throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW);
			}
		}

		// 2. 缓存不存在，从MongoDB查询
		Query query = Query.query(Criteria.where("_id").is(msgId)
				.and("senderId").is(senderId));
		messagePO = mongoTemplate.findOne(query, DialogMessage.class);

		// 3. MongoDB查询结果同步到缓存（存在则同步）
		if (messagePO != null) {
			redisCacheUtil.setMessageCache(messagePO, MSG_CACHE_EXPIRE_SEC);
		}

		// 4. 返回结果
		return messagePO;
	}

	// ===================== 辅助方法 =====================

	/**
	 * 校验会话和发送者合法性（发送者是否为会话参与者）
	 * 业务说明：校验发送者是否为指定会话的合法参与者，防止非法发送消息
	 * 业务流程：
	 * 1. 从SessionService获取会话信息（包含缓存逻辑）
	 * 2. 若未找到，尝试查询发送者为targetId的会话
	 * 3. 检查会话是否存在且未删除
	 * 4. 检查发送者是否为会话参与者
	 * 参数校验：会话ID和发送者ID不能为空
	 * 异常处理：不合法时抛出业务异常
	 * @param sessionId 会话ID，标识消息所属会话
	 * @param senderId 发送者ID，标识消息发送方
	 * @return 会话PO对象，校验通过时返回
	 */
	private DialogSession validateSessionAndSender(Long sessionId, Long senderId) {
		// 1. 查询会话（从SessionService获取，已包含缓存逻辑）
		DialogSession sessionPO = sessionService.getSessionByIdAndUserId(sessionId, senderId);
		// 2. 若会话不存在，尝试查询发送者为targetId的会话（因会话可能归属targetId）
		if (sessionPO == null) {
			LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(DialogSession::getId, sessionId)
					.eq(DialogSession::getTargetId, senderId)
					.eq(DialogSession::getDeleted, 0);
			sessionPO = sessionMapper.selectOne(queryWrapper);
		}

		// 3. 会话不存在或已删除
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}

		// 4. 发送者不是会话参与者（既不是userId也不是targetId）
		if (!Objects.equals(sessionPO.getUserId(), senderId) && !Objects.equals(sessionPO.getTargetId(), senderId)) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW_SEND);
		}

		return sessionPO;
	}

	/**
	 * 校验消息内容合法性
	 * 业务说明：校验消息内容是否符合业务规范，包括非空、长度、格式等
	 * 业务流程：
	 * 1. 校验内容是否为空或仅包含空白字符
	 * 2. 校验文字消息长度（最大500字）
	 * 3. 校验表情消息格式（需符合[表情名]格式）
	 * 参数校验：内容和消息类型不能为空
	 * 异常处理：不符合规范时抛出业务异常
	 * @param content 消息内容，需要校验的内容
	 * @param contentType 消息类型，标识消息内容类型
	 */
	private void validateMessageContent(String content, DialogMessage.ContentTypeEnum contentType) {
		// 1. 内容非空校验
		if (content == null || content.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CONTENT_EMPTY);
		}

		// 2. 文字消息长度限制（最大500字）
		if (contentType == DialogMessage.ContentTypeEnum.TEXT && content.length() > 500) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CONTENT_TOO_LONG);
		}

		// 3. 表情消息格式校验（需符合[表情名]格式，如[微笑]）
		if (contentType == DialogMessage.ContentTypeEnum.EMOJI) {
			if (!content.matches("^\\[\\w+\\]$")) {
				throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CONTENT_INVALID);
			}
		}
	}

	/**
	 * 构建消息PO对象
	 * 业务说明：根据参数构建消息持久化对象，用于保存到数据库
	 * 业务流程：
	 * 1. 创建消息对象实例
	 * 2. 设置会话ID、发送者ID、接收者ID
	 * 3. 设置内容、类型、状态等属性
	 * 4. 设置时间字段
	 * 5. 返回构建的消息对象
	 * 参数校验：会话ID、发送者ID、接收者ID、内容不能为空
	 * 数据处理：状态默认设为已发送，时间设为当前时间
	 * @param sessionId 会话ID，标识消息所属会话
	 * @param senderId 发送者ID，标识消息发送方
	 * @param receiverId 接收者ID，标识消息接收方
	 * @param content 消息内容
	 * @param contentType 消息类型，标识内容类型
	 * @return 构建的消息PO对象
	 */
	private DialogMessage buildMessagePO(Long sessionId, Long senderId, Long receiverId, String content, DialogMessage.ContentTypeEnum contentType) {
		DialogMessage messagePO = new DialogMessage();
		messagePO.setSessionId(sessionId);
		messagePO.setSenderId(senderId);
		messagePO.setReceiverId(receiverId);
		messagePO.setContent(content);
		messagePO.setContentType(contentType);
		messagePO.setStatus(DialogMessage.MessageStatusEnum.SENT); // 初始状态为已发送
		messagePO.setSendTime(LocalDateTime.now());
		messagePO.setReadTime(null); // 初始未读
		messagePO.setIsRevoked(0); // 初始未撤回
		messagePO.setUpdatedAt(LocalDateTime.now());
		return messagePO;
	}

	/**
	 * 更新会话最后消息信息（MySQL）
	 * 业务说明：更新会话记录中的最后消息信息，包括内容、时间等
	 * 业务流程：
	 * 1. 根据消息类型生成消息预览
	 * 2. 更新会话PO的最后消息相关信息
	 * 3. 保存到MySQL数据库
	 * 4. 同步会话缓存
	 * 参数校验：会话PO和消息PO不能为空
	 * 数据处理：消息预览限制为20字，超过则截断并添加省略号
	 * @param sessionPO 会话PO对象
	 * @param messagePO 消息PO对象
	 * @param content 消息内容
	 * @param contentType 消息类型
	 */
	private void updateSessionLastMsg(DialogSession sessionPO, DialogMessage messagePO, String content, DialogMessage.ContentTypeEnum contentType) {
		// 1. 生成消息预览（文字取前20字，表情显示[表情]）
		String lastMsgContent;
		if (contentType == DialogMessage.ContentTypeEnum.EMOJI) {
			lastMsgContent = "[表情]";
		} else {
			lastMsgContent = content.length() > MSG_PREVIEW_MAX_LEN
					? content.substring(0, MSG_PREVIEW_MAX_LEN) + "..."
					: content;
		}

		// 2. 更新会话PO
		sessionPO.setLastMsgId(messagePO.getId());
		sessionPO.setLastMsgContent(lastMsgContent);
		sessionPO.setLastMsgTime(messagePO.getSendTime());
		sessionPO.setUpdatedTime(LocalDateTime.now());

		// 3. 保存到MySQL
		sessionMapper.updateById(sessionPO);

		// 4. 同步会话缓存（更新后的最后消息信息）
		redisCacheUtil.setSessionCache(sessionPO.getUserId(), sessionPO.getId(), sessionPO, MSG_CACHE_EXPIRE_SEC);
		// 若发送者是targetId，还需同步targetId的会话缓存（因会话可能归属targetId）
		if (Objects.equals(sessionPO.getTargetId(), messagePO.getSenderId())) {
			redisCacheUtil.setSessionCache(sessionPO.getTargetId(), sessionPO.getId(), sessionPO, MSG_CACHE_EXPIRE_SEC);
		}
	}

	/**
	 * 增加接收者未读计数（Redis+MySQL）
	 * 业务说明：增加接收者的未读消息计数，用于消息提醒
	 * 业务流程：
	 * 1. 更新Redis中的未读计数（优先更新缓存）
	 * 2. 更新MySQL中的未读计数（最终一致性）
	 * 参数校验：接收者ID和会话ID不能为空
	 * 数据处理：Redis和MySQL数据保持一致性
	 * @param receiverId 接收者ID，标识消息接收方
	 * @param sessionId 会话ID，标识消息所属会话
	 */
	private void incrementReceiverUnreadCount(Long receiverId, Long sessionId) {
		// 1. Redis未读计数自增（优先更新缓存）
		redisCacheUtil.incrementSessionUnreadCount(receiverId, sessionId);

		// 2. MySQL未读计数自增（最终一致性，可异步更新）
		DialogSession receiverSessionPO = sessionService.getSessionByIdAndUserId(sessionId, receiverId);
		if (receiverSessionPO != null) {
			receiverSessionPO.setUnreadCount(receiverSessionPO.getUnreadCount() + 1);
			receiverSessionPO.setUpdatedTime(LocalDateTime.now());
			sessionMapper.updateById(receiverSessionPO);
		}
	}

	/**
	 * 推送消息给接收者（异步）
	 * 逻辑：在线→WebSocket推送，离线→MQ触发系统通知
	 * 业务说明：将消息推送给接收者，根据在线状态选择推送方式
	 * 业务流程：
	 * 1. 检查接收者是否在线（WebSocket会话是否存在）
	 * 2. 在线则通过WebSocket实时推送
	 * 3. 离线则通过MQ发送系统通知
	 * 4. 更新对方会话未读数
	 * 异常处理：推送失败时更新消息状态为FAILED
	 * @param messagePO 消息PO对象
	 * @param receiverId 接收者ID，标识消息接收方
	 * @param sessionPO 会话PO对象
	 */
	@Async // 异步处理，避免阻塞消息发送主流程
	protected void pushMessageToReceiver(DialogMessage messagePO, Long receiverId, DialogSession sessionPO) {
		try {
			DialogSession targetUserSession = getTargetUserSession(receiverId, sessionPO.getUserId());

			if (targetUserSession.getHidden() == 1) {
				targetUserSession.setHidden(0);
				sessionMapper.updateById(targetUserSession);
			}
			// 1. 转换消息PO为VO（用于推送）
			MessageVO messageVO = convertToMessageVO(messagePO, receiverId, targetUserSession.getId());

			// 2. 检查接收者是否在线（WebSocket会话是否存在）
			if (webSocketHandler.isUserOnline(receiverId)) {
				log.info("在线推送消息给接收者{}，消息ID：{}", receiverId, messagePO.getId());
				// 2.1 在线：WebSocket实时推送
				webSocketHandler.pushPrivateMessage(
						receiverId,
						messageVO,
						MessagePushType.PRIVATE_MSG
				);
			} else {
				log.info("离线推送消息给接收者{}，消息ID：{}", receiverId, messagePO.getId());
				// 2.2 离线：通过MQ发送系统通知（调用notification-service）
				String senderName = webSocketHandler.getSenderName(messagePO.getSenderId());
				dialogEventPublisher.sendOfflineNotification(messageVO, receiverId, senderName);
			}
			// 自动增加对方会话未读数
			targetUserSession.setUnreadCount(targetUserSession.getUnreadCount() + 1);
			sessionMapper.updateById(targetUserSession);
			// 更新redis缓存
			redisCacheUtil.setSessionUnreadCount(receiverId, targetUserSession.getId(), targetUserSession.getUnreadCount());
		} catch (Exception e) {
			// 推送失败：更新消息状态为FAILED，便于后续重发
			updateMessageStatusToFailed(messagePO.getId());
			log.error("推送消息给接收者失败 | messageId: {}, receiverId: {}, senderId: {}, sessionId: {}",
					messagePO.getId(), receiverId, messagePO.getSenderId(), sessionPO.getId(), e);
		}
	}

	/**
	 * 推送已读状态给发送者（WebSocket）
	 * 业务说明：向消息发送者推送消息已读状态，实现消息回执功能
	 * 业务流程：
	 * 1. 检查发送者是否在线
	 * 2. 构建已读状态推送数据
	 * 3. 通过WebSocket推送已读状态
	 * 参数校验：发送者ID和会话ID不能为空
	 * 数据处理：若发送者离线则不推送
	 * @param senderId 发送者ID，标识消息发送方
	 * @param sessionId 会话ID，标识消息所属会话
	 * @param msgIds 已读消息ID列表，为空表示所有消息已读
	 */
	private void pushReadStatusToSender(Long senderId, Long sessionId, List<String> msgIds) {
		// 1. 检查发送者是否在线
		if (!webSocketHandler.isUserOnline(senderId)) {
			return; // 发送者离线，无需推送
		}

		// 2. 构建已读状态推送数据
		JSONObject readData = new JSONObject();
		readData.put("sessionId", sessionId);
		readData.put("msgIds", msgIds.isEmpty() ? "all" : msgIds); // 空列表表示所有消息已读
		readData.put("readTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

		// 3. WebSocket推送已读状态
		webSocketHandler.pushMessage(
				senderId,
				MessagePushType.MSG_READ,
				readData
		);
	}

	/**
	 * 更新消息状态为FAILED（MongoDB）
	 * 业务说明：将消息状态更新为失败，用于标记发送失败的消息
	 * 业务流程：
	 * 1. 构建MongoDB更新对象
	 * 2. 更新指定消息的状态为FAILED
	 * 3. 同步缓存中的消息状态
	 * 参数校验：消息ID不能为空
	 * 数据处理：状态更新为FAILED，更新时间设为当前时间
	 * @param msgId 消息ID，标识需要更新状态的消息
	 */
	private void updateMessageStatusToFailed(String msgId) {
		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.FAILED.getValue())
				.set("updatedAt", LocalDateTime.now());
		mongoTemplate.updateFirst(
				Query.query(Criteria.where("_id").is(msgId)),
				update,
				DialogSession.class
		);

		// 同步缓存（更新后的失败状态）
		DialogMessage messagePO = mongoTemplate.findById(msgId, DialogMessage.class);
		if (messagePO != null) {
			redisCacheUtil.setMessageCache(messagePO, MSG_CACHE_EXPIRE_SEC);
		}
	}


	/**
	 * 将消息PO转换为VO（补充发送者信息、内容预览、时间格式化）
	 * 业务说明：将消息持久化对象转换为视图对象，补充发送者信息、内容预览、格式化时间等
	 * 业务流程：
	 * 1. 复制消息PO的基本属性到VO
	 * 2. 设置消息状态和内容类型值
	 * 3. 补充消息方向（自己发送/对方发送）
	 * 4. 补充发送者信息（头像、昵称）
	 * 5. 补充内容预览
	 * 6. 格式化发送时间
	 * 参数校验：消息PO和当前用户ID不能为空
	 * 数据处理：根据发送者ID判断消息方向，获取发送者信息
	 * @param messagePO 消息PO对象
	 * @param currentUserId 当前用户ID，用于判断消息方向
	 * @return 转换后的消息VO对象
	 */
	private MessageVO convertToMessageVO(DialogMessage messagePO, Long currentUserId, Long sessionId) {
		MessageVO messageVO = new MessageVO();
		BeanUtils.copyProperties(messagePO, messageVO);
		messageVO.setStatus(messagePO.getStatus().getValue());
		messageVO.setContentType(messagePO.getContentType().getValue());

		messageVO.setShowSessionId(sessionId);
		// 1. 补充消息方向（自己发送/对方发送）
		messageVO.setSelfSend(Objects.equals(messagePO.getSenderId(), currentUserId));

		// 2. 补充发送者信息（头像、昵称）
		if (Objects.equals(messagePO.getSenderId(), AI_SENDER_ID)) {
			messageVO.setSenderNickname("AI助手");
			messageVO.setSenderAvatarUrl(""); // AI默认头像
		} else {
			Result<List<UserInfoDes>> userResult = userFeignClient.getUsersByIds(Collections.singleton(messagePO.getSenderId()));
			if (userResult.isSuccess() && userResult.getData() != null && !userResult.getData().isEmpty()) {
				UserInfoDes senderUser = userResult.getData().get(0);
				messageVO.setSenderNickname(senderUser.getNickname());
				messageVO.setSenderAvatarUrl(senderUser.getAvatarUrl());
			} else {
				messageVO.setSenderNickname("未知用户");
				messageVO.setSenderAvatarUrl(""); // 默认头像
			}
		}

		// 3. 补充内容预览（与会话最后消息预览一致）
		if (messagePO.getContentType() == DialogMessage.ContentTypeEnum.EMOJI) {
			messageVO.setContentPreview("[表情]");
		} else {
			String content = messagePO.getContent();
			messageVO.setContentPreview(content.length() > MSG_PREVIEW_MAX_LEN
					? content.substring(0, MSG_PREVIEW_MAX_LEN) + "..."
					: content);
		}

		// 4. 格式化发送时间（如：15:30、昨天 15:30、06-12 15:30）
		messageVO.setSendTimeStr(formatSendTime(messagePO.getSendTime()));

		return messageVO;
	}

	/**
	 * 格式化发送时间（抖音风格）
	 * 业务说明：将日期时间格式化为用户友好的时间显示格式
	 * 业务流程：
	 * 1. 判断发送时间与当前时间的关系
	 * 2. 根据不同时间范围选择不同格式
	 * 3. 返回格式化后的时间字符串
	 * 数据处理：今天显示HH:mm，昨天显示昨天 HH:mm，一个月内显示MM-dd HH:mm，超过一个月显示yyyy-MM-dd HH:mm
	 * 参数校验：发送时间可为空，为空时返回空字符串
	 * @param sendTime 发送时间
	 * @return 格式化后的时间字符串
	 */
	private String formatSendTime(LocalDateTime sendTime) {
		if (sendTime == null) {
			return "";
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minusDays(1);
		LocalDateTime oneMonthAgo = now.minusMonths(1);

		// 今天：HH:mm
		if (sendTime.toLocalDate().isEqual(now.toLocalDate())) {
			return sendTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
		}

		// 昨天：昨天 HH:mm
		if (sendTime.toLocalDate().isEqual(yesterday.toLocalDate())) {
			return "昨天 " + sendTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
		}

		// 一个月内：MM-dd HH:mm
		if (sendTime.isAfter(oneMonthAgo)) {
			return sendTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
		}

		// 超过一个月：yyyy-MM-dd HH:mm
		return sendTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
}