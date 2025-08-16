package org.doubao.dialog.service.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.dialog.service.dto.*;
import org.doubao.dialog.service.entity.AssistantDialog;
import org.doubao.dialog.service.entity.AssistantMessage;
import org.doubao.dialog.service.enums.DialogStatusEnum;
import org.doubao.dialog.service.enums.SenderTypeEnum;
import org.doubao.dialog.service.feign.AIServiceClient;
import org.doubao.dialog.service.mapper.AssistantDialogMapper;
import org.doubao.dialog.service.mapper.AssistantMessageMapper;
import org.doubao.dialog.service.service.KnowledgeService;
import org.doubao.dialog.service.service.MessageService;
import org.doubao.dialog.service.service.WebSocketService;
import org.doubao.dialog.service.vo.DialogQuery;
import org.doubao.dialog.service.vo.DialogVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl extends ServiceImpl<AssistantMessageMapper, AssistantMessage> implements MessageService {

	private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
	@Autowired
	private AssistantDialogMapper dialogMapper;

	@Autowired
	private AssistantMessageMapper messageMapper;

	@Autowired
	private KnowledgeService knowledgeService;

	@Autowired
	private AIServiceClient aiServiceClient;

	@Autowired
	private WebSocketService webSocketService;

	@Value("${ai.model:gpt-3.5-turbo}")
	private String aiModel;

	@Value("${ai.max-tokens:200}")
	private Integer maxTokens;

	@Value("${ai.temperature:0.7}")
	private Double temperature;

	/**
	 * 处理用户消息并生成回复
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public MessageDTO handleUserMessage(MessageRequest request) {
		log.info("处理用户消息: {}", request);

		// 1. 获取或创建对话
		Long dialogId = request.getDialogId();
		if (dialogId == null) {
			dialogId = createNewDialog(request.getUserId(), request.getContent());
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
				replyContent = callAiService(dialogId, request.getContent());
			}
		}

		// 5. 保存回复消息
		saveMessage(dialogId, null, replyContent, SenderTypeEnum.AI.getCode());

		// 6. 推送回复给用户（WebSocket）
		MessageDTO replyMessage = buildReplyMessage(dialogId, replyContent, isAdmin, SenderTypeEnum.AI.getCode());
		webSocketService.pushToUser(request.getUserId(), replyMessage);

		// 7. 返回结果
		return replyMessage;
	}

	/**
	 * 获取用户的对话历史列表
	 */
	@Override
	public IPage<DialogVO> getUserDialogHistory(Long userId, int pageNum, int pageSize) {
		Page<DialogVO> page = new Page<>(pageNum, pageSize);
		return dialogMapper.selectByUserId(page, userId);
	}

	/**
	 * 获取对话详情
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
	 */
	@Override
	public IPage<DialogVO> getAdminDialogList(DialogQuery query, int pageNum, int pageSize) {
		Page<DialogVO> page = new Page<>(pageNum, pageSize);
		return dialogMapper.selectByAdminQuery(page, query);
	}

	/**
	 * 更新对话状态
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
	 */
	private Long createNewDialog(Long userId, String content) {
		// 根据消息内容生成标题
		String title = aiServiceClient.generateTitle(content);
		AssistantDialog dialog = new AssistantDialog();
		dialog.setUserId(userId);
		dialog.setTitle(title);
		dialog.setStatus(DialogStatusEnum.ACTIVE.getCode());
		dialog.setCreatedTime(LocalDateTime.now());
		dialog.setUpdatedTime(LocalDateTime.now());
		dialogMapper.insert(dialog);
		return dialog.getId();
	}

	/**
	 * 保存消息
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
	 */
	private MessageDTO buildReplyMessage(Long dialogId, String content, boolean isAdmin, int senderType) {
		return new MessageDTO(
				dialogId,
				content,
				isAdmin,
				LocalDateTime.now(),
				senderType
		);
	}

	/**
	 * 判断是否需要人工接管
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
				if (lastMessages.get(i).getSenderType() != SenderTypeEnum.USER.getCode()) {  // 非用户消息
					isNeed = false;
					break;
				}
				if (lastMessages.get(i+1).getSenderType() != SenderTypeEnum.AI.getCode()) {  // 非AI消息
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
	 */
	private String callAiService(Long dialogId, String currentContent) {
		try {
			// 构建对话上下文
			List<ChatMessage> messages = buildAIChatContext(dialogId, currentContent);

			// 调用AI服务
			AIRequest request = new AIRequest();
			request.setMessages(messages);
			request.setModel(aiModel);
			request.setMaxTokens(maxTokens);
			request.setTemperature(temperature);

			AIResponse response = aiServiceClient.generateReply(request);

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
	 */
	private List<ChatMessage> buildAIChatContext(Long dialogId, String currentContent) {
		List<AssistantMessage> history = messageMapper.selectLastNByDialogId(dialogId, 6);  // 取最近6条
		List<ChatMessage> context = new ArrayList<>();

		// 添加系统提示
		context.add(new ChatMessage(
				"system",
				"你是引文网站的智能助手，仅回答与网站功能相关的问题（如创建引文、添加标签等）。" +
						"网站功能包括：翎枢阁（首页）、墨渊境（引文列表）、采翎编（引文管理）、执翎台（新建引文）等。" +
						"若问题与网站功能无关，回复：'抱歉，我只能回答与引文网站相关的问题哦~'"
		));

		// 倒序添加历史消息（最早的在前）
		for (int i = history.size() - 1; i >= 0; i--) {
			AssistantMessage msg = history.get(i);
			String role = msg.getSenderType() == SenderTypeEnum.USER.getCode()
					? "user"
					: "assistant";
			context.add(new ChatMessage(role, msg.getContent()));
		}

		// 添加当前消息
		context.add(new ChatMessage("user", currentContent));
		return context;
	}
}