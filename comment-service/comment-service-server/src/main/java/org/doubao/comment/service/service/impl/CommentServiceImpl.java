package org.doubao.comment.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.math.RandomUtils;
import org.doubao.comment.service.dto.*;
import org.doubao.comment.service.entity.Comment;
import org.doubao.comment.service.feign.LikeClient;
import org.doubao.comment.service.feign.UserClient;
import org.doubao.comment.service.mapper.CommentMapper;
import org.doubao.comment.service.messaging.CommentEventPublisher;
import org.doubao.comment.service.service.CommentService;
import org.doubao.comment.service.vo.CommentCountVo;
import org.doubao.comment.service.vo.CommentVO;
import org.doubao.comment.service.vo.ReplyVO;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.ConvertUtil;
import org.doubao.mall.common.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 * <p>
 * 实现评论服务接口，提供评论创建、查询、点赞、回复、状态管理等核心业务功能
 * 集成了缓存、消息队列、远程调用等机制，确保高并发场景下的性能和可靠性
 */
@Service
@Transactional
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
	private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
	@Autowired
	private CommentMapper commentMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Resource
	private CommentEventPublisher commentEventPublisher;

	@Autowired
	private UserClient userClient;

	@Autowired
	private LikeClient likeClient;

	/**
	 * 创建评论
	 * <p>
	 * 执行评论创建的完整业务流程，包括敏感词检测、字数校验、频率限制、评论对象构建、
	 * 数据库保存、通知发送和缓存清理等步骤
	 * <p>
	 * 业务流程：
	 * 1. 内容校验（字数检查，当前未启用敏感词检测）
	 * 2. 用户频率限制（10秒内最多3次评论）
	 * 3. 评论对象构建（区分主评论和回复评论）
	 * 4. 评论数据持久化
	 * 5. 发送评论通知
	 * 6. 清理相关缓存
	 * 
	 * @param dto 评论数据传输对象，包含评论内容、关联文章ID、父评论ID等信息
	 * @return 创建的评论唯一标识符，格式为"C-"前缀加时间戳和随机数（主评论）或
	 *         "R-"前缀加时间戳和随机数（回复评论）
	 */
	@Override
	public String createComment(CommentDTO dto) {
		// 1: 敏感词检测（当前未启用）
		// int riskScore = aiClient.checkContent(dto.getContent());
		// if (riskScore >= 90) {
		// 	throw new BusinessException("内容包含敏感词", ErrorCode.COMMENT_HAS_NOT_ALLOWED);
		// }

		// 2: 字数校验（评论内容必须在500字符之间）
		int contentLength = dto.getContent().length();
		if (contentLength > 500) {
			throw new BusinessException(ErrorCode.COMMENT_NOT_IN_RANGE);
		}

		// 3: 频率限制（每个用户10秒内最多评论3次）
		long userId = UserContext.getUser().getId();
		String rateKey = "comment_rate:" + userId;
		Long count = redisTemplate.opsForValue().increment(rateKey, 1);
		if (count != null && count == 1) {
			redisTemplate.expire(rateKey, 10, TimeUnit.SECONDS);
		}
		if (count != null && count > 3) {
			throw new BusinessException(ErrorCode.COMMENT_LIMIT_REACHED);
		}

		// 4: 构建评论对象
		Comment comment = new Comment();
		comment.setContent(dto.getContent());
		comment.setPostId(dto.getPostId());
		comment.setUserId(userId);
		comment.setParentId(dto.getParentId());

		// 4.1: 设置根评论ID（区分主评论和回复评论）
		if (dto.isReply()) {
			// 回复评论使用 "R-" 前缀
			comment.setCommentId("R-" + System.currentTimeMillis() + RandomUtils.nextInt(1000));
			comment.setRootId(getRootCommentId(dto.getParentId()));
			comment.setRepliedUserId(dto.getRepliedUserId());
		} else {
			// 主评论使用 "C-" 前缀
			comment.setCommentId("C-" + System.currentTimeMillis() + RandomUtils.nextInt(1000));
			comment.setRootId(comment.getCommentId());
		}

		// 4.2: 初始化评论统计数据
		comment.setLikeCount(0L);
		comment.setReplyCount(0L);
		comment.setStatus(0); // 0-正常，1-折叠，2-删除
		comment.setIsTop(0);  // 0-未置顶，1-已置顶

		// 5: 保存评论到数据库
		this.save(comment);

		// 6: 发送评论通知给相关用户
		Long repliedUserId = 0L;
		String originalComment = "";
		if (dto.isReply()) {
			// 查询被回复评论所属用户，用于发送通知
			Comment parentComment = this.getById(comment.getParentId());
			repliedUserId = parentComment.getUserId();
			originalComment = parentComment.getContent();
		}

		// 6.1: 获取操作用户信息（昵称和头像）
		String operatorUserName = Constants.DEFAULT_USER_NAME;
		String operatorUserAvatar = "";
		List<UserInfoDes> userInfos = userClient.getUsersByIds(Collections.singleton(userId)).getData();
		if (!CollectionUtils.isEmpty(userInfos)) {
			UserInfoDes userInfo = userInfos.get(0);
			operatorUserName = userInfo.getNickname();
			operatorUserAvatar = userInfo.getAvatarUrl();
		}
		
		// 6.2: 异步发送评论通知
		commentEventPublisher.pushCommentNotification(repliedUserId, !dto.isReply(), comment.getPostId(), comment.getContent(), originalComment,
				userId, operatorUserName, operatorUserAvatar);
		
		// 7: 删除相关缓存，确保数据一致性
		String cacheKey = "comments:post:" + dto.getPostId()+":*";
		Set<String> keys = redisTemplate.keys(cacheKey);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}

		return comment.getCommentId();
	}

	/**
	 * 计算评论热度分数
	 * <p>
	 * 基于点赞数和回复数计算评论的基础热度分数，使用对数函数防止热度值增长过快
	 * 目前未启用作者权重（预留扩展功能）
	 * <p>
	 * 热度计算公式：log10(点赞数 * 2 + 回复数 + 0.1) + (作者权重 ? 5 : 0)
	 * 使用对数函数确保热度值增长相对平缓，避免早期高热度内容持续占据热门位置
	 *
	 * @param comment 评论实体对象，包含点赞数、回复数等计算所需字段
	 * @return 评论热度分数，基于点赞数和回复数的对数计算结果
	 */
	@Override
	public double calculateHotScore(Comment comment) {
		long likeCount = comment.getLikeCount() == null ? 0L : comment.getLikeCount();
		long replyCount = comment.getReplyCount() == null ? 0L : comment.getReplyCount();

		// 基础热度计算：使用对数函数防止热度值增长过快
		// 点赞权重为2，回复权重为1，加0.1避免对数为负无穷
		double baseScore = Math.log10(likeCount * 2 + replyCount + 0.1);

		// 作者权重（当前未启用，预留功能）
		// boolean isAuthorReplied = quoteClient.isAuthor(
		// 		comment.getPostId(),
		// 		comment.getUserId()
		// );
		boolean isAuthorReplied = false;

		// 如果是作者回复，额外增加5分权重
		return baseScore + (isAuthorReplied ? 5 : 0);
	}

	@Override
	public void handleLike(String commentId, String action) {

	}

	@Override
	public String replyComment(String commentId, ReplyDTO dto) {
		return "";
	}

	@Override
	public void handleTop(String commentId, String action) {

	}

	@Override
	public void deleteComment(String commentId) {

	}

	/**
	 * 根据评论ID获取回复列表
	 * <p>
	 * 分页获取指定评论的回复列表，包括用户信息和点赞状态的查询与设置
	 * <p>
	 * 业务流程：
	 * 1. 参数校验（页码和每页大小）
	 * 2. 数据库查询回复列表
	 * 3. 批量获取用户信息
	 * 4. 设置回复的点赞状态
	 * 5. 构建回复视图对象列表
	 * 
	 * @param commentId 评论唯一标识符
	 * @param page 页码，从1开始
	 * @param size 每页大小，限制最大为20条
	 * @return 包含回复视图对象的分页结果
	 */
	@Override
	public Page<ReplyVO> getRepliesByCommentId(String commentId, Integer page, Integer size) {
		// 1: 校验参数（页码最小为1，每页大小限制在1-20之间）
		if (page < 1) {
			page = 1;
		}
		if (size < 1 || size > 20) { // 限制最大每页20条
			size = 5;
		}

		// 2: 计算分页参数（MyBatis-Plus分页从1开始）
		Page<Comment> queryPage = new Page<>(page, size);

		// 3: 数据库查询（查询指定评论的子回复）
		IPage<Comment> replyPage = commentMapper.selectRepliesByCommentId(
				queryPage,
				commentId // 主评论ID（作为rootId）
		);
		
		// 3.1: 收集所有需要查询的用户ID（评论用户和被回复用户）
		Set<Long> userIds = replyPage.getRecords().stream().map(Comment::getUserId).collect(Collectors.toSet());
		userIds.addAll(replyPage.getRecords().stream().map(Comment::getRepliedUserId).collect(Collectors.toSet()));
		List<UserInfoDes> userInfos = userClient.getUsersByIds(userIds).getData();
		
		// 3.2: 收集回复ID，用于批量查询点赞状态
		List<String> replyIds =  new ArrayList<>();
		Long userId = UserContext.getUser().getId();

		// 4: 转换为回复视图对象，补充用户信息
		List<ReplyVO> replyVOList = replyPage.getRecords().stream()
				.map(comment -> {
					ReplyVO replyVO = new ReplyVO();
					BeanUtils.copyProperties(comment, replyVO);
					replyIds.add(comment.getCommentId());
					// 设置回复ID（使用评论表的ID）
					replyVO.setReplyId(comment.getCommentId());

					replyVO.setLikeCount(comment.getLikeCount());
					// 4.1: 补充用户信息（通过用户服务获取）
					Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> Objects.equals(comment.getUserId(), userInfo.getId())).findFirst();
					if (first.isPresent()) {
						UserInfoDes userInfo = first.get();
						replyVO.setUser(userInfo);
					}

					// 4.2: 处理被回复者信息
					userInfos.stream()
							.filter(userInfo -> Objects.equals(comment.getRepliedUserId(), userInfo.getId()))
							.findFirst()
							.ifPresent(userInfo -> replyVO.setRepliedNickname(userInfo.getNickname()));
					return replyVO;
				})
				.collect(Collectors.toList());

		// 5: 设置是否点赞（批量查询点赞状态）
		if (!replyIds.isEmpty()) {
			BatchLikeStatusRequest likeQueryDto = new BatchLikeStatusRequest();
			likeQueryDto.setEntities(replyIds.stream().map(replyId -> {
				BatchLikeStatusRequest.EntityRequest request = new BatchLikeStatusRequest.EntityRequest();
				request.setEntityId(replyId);
				request.setEntityType(1); // 1表示评论类型
				return request;
			}).collect(Collectors.toList()));
			// 不查询点赞数量，只查询点赞状态
			likeQueryDto.setQueryCount(false);
			likeQueryDto.setUserId(userId);
			BatchLikeStatusResponse batchLikeStatusResponse = likeClient.batchGetLikeStatus(likeQueryDto).getData();
			List<BatchLikeStatusResponse.LikeStatusResult> results = batchLikeStatusResponse.getResults();
			if (results != null && !results.isEmpty()) {
				replyVOList.forEach(replyVO -> {
					String replyId = replyVO.getReplyId();
					results.stream()
							.filter(result -> Objects.equals(result.getEntityId(), replyId))
							.findFirst()
							.ifPresent(result -> {
								replyVO.setLiked(result.getLiked());
							});
				});
			}
		}


		// 6: 构建分页结果
		Page<ReplyVO> replyVOPage = new Page<>(replyPage.getCurrent(), replyPage.getSize(),replyPage.getTotal());
		replyVOPage.setRecords(replyVOList);

		return replyVOPage;
	}

	/**
	 * 切换评论点赞状态
	 * <p>
	 * 执行评论点赞或取消点赞操作，通过调用点赞服务完成实际的点赞逻辑，
	 * 并更新评论表中的点赞数冗余字段，同时清理相关缓存
	 * <p>
	 * 业务流程：
	 * 1. 验证评论是否存在
	 * 2. 构造点赞服务请求参数
	 * 3. 调用点赞服务接口
	 * 4. 更新评论表中的点赞数冗余字段
	 * 5. 清理相关缓存
	 *
	 * @param commentId 评论唯一标识符
	 * @return 点赞操作的响应结果，包含操作是否成功、操作类型和当前点赞总数
	 */
	@Override
	public ToggleLikeResponse toggleLike(String commentId) {
		Long operatorUserId = Long.valueOf(UserContext.getUser().getId());
		// 1: 查询评论信息（获取评论作者ID和内容）
		Comment comment = commentMapper.selectById(commentId);
		if (comment == null) {
			throw new BusinessException((ErrorCode.COMMENT_NOT_FOUND));
		}

		// 2: 删除缓存，确保数据一致性
		String cacheKey = "comments:post:" + comment.getPostId()+":*";
		Set<String> keys = redisTemplate.keys(cacheKey);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
		// 3: 构造调用点赞服务的请求参数
		CommentLikeRequest likeRequest = new CommentLikeRequest();
		likeRequest.setOperatorUserId(operatorUserId);  // 点赞操作的用户
		likeRequest.setEntityType(1);  // 实体类型：1表示评论（与点赞服务约定）
		likeRequest.setEntityId(commentId);  // 评论ID
		likeRequest.setUserId(comment.getUserId());  // 被点赞的评论作者ID
		likeRequest.setContent(comment.getContent());  // 评论内容（用于通知）

		// 4: 调用点赞服务的接口
		Result<ToggleLikeResponse> feignResult = likeClient.toggleLike(likeRequest);
		if (!feignResult.isSuccess()) {
			throw new BusinessException((ErrorCode.COMMENT_LIKE_ERROR));
		}

		// 5: 更新评论表中的点赞数（冗余字段，提高查询效率）
		ToggleLikeResponse response = feignResult.getData();
		comment.setLikeCount(response.getCurrentCount());
		commentMapper.updateById(comment);

		return response;
	}

	/**
	 * 更新评论状态
	 * <p>
	 * 根据请求参数更新评论的状态信息，如折叠、删除等状态
	 * 当状态为折叠时，会更新评论内容为"该评论已被折叠"
	 * 操作完成后会清理相关缓存，确保数据一致性
	 * <p>
	 * 支持的状态值：
	 * 0 - 正常
	 * 1 - 折叠
	 * 2 - 删除
	 * <p>
	 * 业务流程：
	 * 1. 解析请求参数
	 * 2. 查询评论记录
	 * 3. 更新评论状态
	 * 4. 特殊处理（折叠状态更新内容）
	 * 5. 清理相关缓存
	 *
	 * @param request 包含评论ID和状态的请求参数映射
	 */
	@Override
	public void updateStatus(Map<String, String> request) {
		// 1: 解析请求参数
		String commentId = request.get("commentId");
		int status = Integer.parseInt(request.get("status"));
		
		// 2: 查询评论记录
		LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(Comment::getCommentId, commentId);
		Comment comment = this.getOne(queryWrapper);
		if (comment == null) {
			return;
		}
		
		// 3: 特殊处理（如果状态为折叠，更新评论内容）
		if (status == 1) {
			comment.setContent("该评论已被折叠");
		}
		
		// 4: 更新评论状态
		comment.setStatus(status);
		this.updateById(comment);

		// 5: 清除相关缓存，确保数据一致性
		String cacheKey = "comments:post:" + comment.getPostId() +":*";
		Set<String> keys = redisTemplate.keys(cacheKey);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}

	@Override
	public Map<Long, Long> batchCounts(List<Long> contentIds) {
		if (CollectionUtils.isEmpty(contentIds)) {
			return Collections.emptyMap();
		}
		Map<Long, Long> result = new HashMap<>(contentIds.size());
		List<String> quoteIds = contentIds.stream().map(Object::toString).collect(Collectors.toList());
		List<CommentCountVo> commentCountVos = commentMapper.countCommentsByPostIds(quoteIds);
		for (CommentCountVo commentCountVo : commentCountVos) {
			result.put(Long.valueOf(commentCountVo.getPostId()), commentCountVo.getCommentCount());
		}
		return result;
	}

	/**
	 * 批量统计每日评论数
	 * <p>
	 * 根据参数统计指定日期范围内每日的评论总数
	 * 用于数据统计和报表展示功能
	 * <p>
	 * 业务流程：
	 * 1. 参数解析和校验
	 * 2. 转换参数格式
	 * 3. 调用数据库查询
	 * 4. 处理查询结果
	 * 5. 返回统计结果
	 *
	 * @param params 统计参数，包含引文ID和日期列表
	 * @return 按日期分组的评论数量统计结果，键为日期，值为当日评论总数
	 */
	@Override
	public Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params) {
		// 1: 参数校验（检查参数是否为空）
		if (params == null || params.isEmpty()) {
			log.error("批量查询每日评论数参数为空");
			return Collections.emptyMap();
		}

		// 2: 解析参数（获取文章ID列表和日期列表）
		List<Long> contentIds = ConvertUtil.safeConvertToListOfLong(params.get("quoteIds"));
		List<LocalDate> dates = ConvertUtil.safeConvertToListOfLocalDate(params.get("dates"));


		// 参数校验：文章ID和日期列表不可为空
		if (CollectionUtils.isEmpty(contentIds) || CollectionUtils.isEmpty(dates)) {
			log.error("批量查询每日评论数参数不完整：contentIds={}, dates={}", contentIds, dates);
			return Collections.emptyMap();
		}

		// 3: 转换文章ID为字符串（因为Comment中postId是String类型）
		List<String> postIds = contentIds.stream()
				.map(String::valueOf)
				.collect(Collectors.toList());

		// 4: 调用Mapper查询指定日期和文章的有效评论数总和（按日期分组）
		List<Map<String, Object>> dailyCounts = commentMapper.selectDailyCommentCounts(postIds, dates);

		// 5: 处理查询结果并返回
		// 5.1 初始化结果映射（确保所有请求日期都有返回值）
		Map<LocalDate, Long> resultMap = new HashMap<>(dates.size());
		for (LocalDate date : dates) {
			resultMap.put(date, 0L);
		}

		// 5.2 填充查询到的实际计数（覆盖初始值）
		for (Map<String, Object> countMap : dailyCounts) {
			// 从查询结果中提取日期和计数（数据库字段与Java类型映射）
			LocalDate statDate = ConvertUtil.safeParseLocalDate(countMap.get("stat_date"));
			Long totalCount = ConvertUtil.safeParseLong(countMap.get("total_count"));


			// 仅更新输入日期列表中存在的日期
			if (statDate != null && resultMap.containsKey(statDate)) {
				resultMap.put(statDate, totalCount);
			}
		}

		log.info("批量查询每日评论数完成：日期范围={}至{}, 文章数量={}, 结果={}",
				dates.get(0), dates.get(dates.size() - 1), contentIds.size(), resultMap);
		return resultMap;
	}

	/**
	 * 获取评论列表
	 * <p>
	 * 根据文章ID分页获取评论列表，支持按热度或时间排序
	 * 包含缓存机制、用户信息查询、回复列表补充、点赞状态设置等功能
	 * <p>
	 * 业务流程：
	 * 1. 尝试从缓存获取（减少数据库压力）
	 * 2. 缓存未命中则查询数据库
	 * 3. 补充用户信息（通过用户服务）
	 * 4. 获取前2条回复（减少嵌套查询）
	 * 5. 设置点赞状态（批量查询）
	 * 6. 按热度排序（如需要）
	 * 7. 存入缓存（提高后续访问速度）
	 * 
	 * @param postId 关联文章ID
	 * @param page 分页参数
	 * @param sortType 排序类型，"hot"表示按热度排序，"time"表示按时间排序
	 * @return 评论视图对象的分页结果
	 */
	@Override
	public IPage<CommentVO> getCommentList(String postId, Page<Comment> page, String sortType) {
		Long userId = UserContext.getUser().getId();

		// 1: 尝试从缓存获取（热点数据）
		// 缓存键格式：comments:post:{postId}:{sortType}:{userId}:{page}:{size}
		String cacheKey = "comments:post:" + postId + ":" + sortType + ":"  + userId + ":" + page.getCurrent() + ":" + page.getSize();
		IPage<CommentVO> cachedPage = (IPage<CommentVO>) redisTemplate.opsForValue().get(cacheKey);
		if (cachedPage != null) {
			return cachedPage;
		}

		// 2: 缓存未命中，从数据库查询
		// 2.1: 查询一级评论（rootId = commentId，即主评论）
		LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<Comment>()
				.eq(Comment::getPostId, postId)
				.isNull(Comment::getParentId) // 一级评论无父ID
				.in(Comment::getStatus, 0,1); // 查询正常和折叠状态的评论（排除删除状态）

		// 2.2: 排序处理
		if ("hot".equals(sortType)) {
			// 按热度排序（数据库层面先按基础热度排序，内存中计算最终热度）
			// query.orderByDesc("like_count * 2 + reply_count");
		} else {
			// 按时间排序（最新在前）
			query.orderByDesc(Comment::getCreatedTime);
		}

		// 2.3: 分页查询一级评论
		IPage<Comment> commentPage = commentMapper.selectPage(page, query);

		// 3: 转换为VO（包含用户信息、点赞状态、回复列表）
		// 3.1: 收集所有需要查询的用户ID
		Set<Long> userIds = commentPage.getRecords().stream().map(Comment::getUserId).collect((Collectors.toSet()));
		userIds.addAll(commentPage.getRecords().stream().map(Comment::getRepliedUserId).filter(Objects::nonNull).collect(Collectors.toSet()));
		List<UserInfoDes> userInfos = userClient.getUsersByIds(userIds).getData();

		// 3.2: 收集评论ID，用于批量查询点赞状态
		List<String> commentIds = commentPage.getRecords().stream().map(Comment::getCommentId).collect(Collectors.toList());
		
		// 3.3: 转换评论实体为视图对象
		IPage<CommentVO> resultPage = commentPage.convert(comment -> {
			CommentVO vo = new CommentVO();
			BeanUtils.copyProperties(comment, vo);

			// 3.3.1: 补充用户信息（远程调用用户服务）
			Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> Objects.equals(comment.getUserId(), userInfo.getId())).findFirst();
			if (first.isPresent()) {
				UserInfoDes userInfo = first.get();
				vo.setUser(userInfo);
			}

			// 3.3.2: 补充前2条回复（嵌套查询）
			List<Comment> replies = commentMapper.selectReplies(
					comment.getCommentId(), 2); // 只查前2条
			List<ReplyVO> voList = convertReplies(replies);
			if (voList != null && !voList.isEmpty()) {
				List<String> collect = voList.stream().map(ReplyVO::getReplyId).collect(Collectors.toList());
				commentIds.addAll(collect);
			}
			vo.setReplyList(voList);

			// 3.3.3: 统计回复数（查询该评论下的所有回复数量）
			LambdaQueryWrapper<Comment> replyQuery = new LambdaQueryWrapper<Comment>()
					.eq(Comment::getPostId, postId)
					.eq(Comment::getRootId, comment.getCommentId()) // 查询该评论树下的所有回复
					.isNotNull(Comment::getParentId) // 回复评论有父ID
					.in(Comment::getStatus, 0,1); // 正常和折叠状态
			int count = this.count(replyQuery);
			vo.setReplyCount((long) count);

			// 3.3.4: 计算热度值（仅在热度排序时计算，避免不必要的计算）
			if ("hot".equals(sortType)) {
				vo.setHotScore(calculateHotScore(comment));
			}

			// 3.3.5: 标记置顶状态（从Redis获取置顶评论ID）
			String topCommentId = (String) redisTemplate.opsForValue().get("post:top_comment:" + postId);
			vo.setIsTop(Objects.equals(comment.getCommentId(), topCommentId) ? 1 : 0);

			return vo;
		});

		// 4: 设置是否点赞（批量查询点赞状态）
		if (!commentIds.isEmpty()) {
			BatchLikeStatusRequest likeQueryDto = new BatchLikeStatusRequest();
			likeQueryDto.setEntities(commentIds.stream().map(commentId -> {
				BatchLikeStatusRequest.EntityRequest request = new BatchLikeStatusRequest.EntityRequest();
				request.setEntityId(commentId);
				request.setEntityType(1); // 1表示评论类型
				return request;
			}).collect(Collectors.toList()));
			// 不查询点赞数量，只查询点赞状态
			likeQueryDto.setQueryCount(false);
			likeQueryDto.setUserId(Long.valueOf(userId));
			BatchLikeStatusResponse batchLikeStatusResponse = likeClient.batchGetLikeStatus(likeQueryDto).getData();
			List<BatchLikeStatusResponse.LikeStatusResult> results = batchLikeStatusResponse.getResults();
			if (results != null && !results.isEmpty()) {
				resultPage.getRecords().forEach(commentVO -> {
					String commentId = commentVO.getCommentId();
					results.stream()
							.filter(result -> Objects.equals(result.getEntityId(), commentId))
							.findFirst()
							.ifPresent(result -> {
								commentVO.setLike(result.getLiked());
							});
					// 同时设置回复的点赞状态
					List<ReplyVO> replyList = commentVO.getReplyList();
					replyList.forEach(replyVO -> {
						String replyId = replyVO.getReplyId();
						results.stream()
								.filter(result -> Objects.equals(result.getEntityId(), replyId))
								.findFirst()
								.ifPresent(result -> {
									replyVO.setLiked(result.getLiked());
								});
					});
				});
			}
		}


		// 5: 按热度二次排序（内存中精确计算，仅在热度排序时执行）
		if ("hot".equals(sortType)) {
			List<CommentVO> sortedComments = resultPage.getRecords().stream()
					.sorted(Comparator.comparingDouble(CommentVO::getHotScore).reversed())
					.collect(Collectors.toList());
			resultPage.setRecords(sortedComments);
		}

		// 6: 存入缓存（10分钟过期，平衡数据一致性和性能）
		redisTemplate.opsForValue().set(cacheKey, resultPage, 10, TimeUnit.MINUTES);

		return resultPage;
	}

	/**
	 * 转换回复列表为VO
	 * <p>
	 * 将评论实体列表转换为回复视图对象列表，补充用户信息和被回复者昵称
	 * <p>
	 * 业务流程：
	 * 1. 收集所有需要查询的用户ID
	 * 2. 批量获取用户信息
	 * 3. 转换评论实体为回复视图对象
	 * 4. 设置用户信息和被回复者昵称
	 * 
	 * @param replies 评论实体列表
	 * @return 回复视图对象列表
	 */
	private List<ReplyVO> convertReplies(List<Comment> replies) {
		// 收集所有需要查询的用户ID（评论用户和被回复用户）
		Set<Long> userIds = replies.stream().map(Comment::getUserId).collect(Collectors.toSet());
		userIds.addAll(replies.stream().map(Comment::getRepliedUserId).filter(Objects::nonNull).collect(Collectors.toSet()));
		List<UserInfoDes> userInfos = userClient.getUsersByIds(userIds).getData();
		
		// 转换评论实体为回复视图对象
		return replies.stream().map(reply -> {
			ReplyVO replyVO = new ReplyVO();
			BeanUtils.copyProperties(reply, replyVO);
			replyVO.setReplyId(reply.getCommentId()); // 设置回复ID
			replyVO.setLikeCount(reply.getLikeCount()); // 设置点赞数
			replyVO.setAuthorTop(reply.getIsTop() == 1); // 设置是否作者置顶
			// 设置评论用户信息
			Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> Objects.equals(reply.getUserId(), userInfo.getId())).findFirst();
			if (first.isPresent()) {
				UserInfoDes userInfo = first.get();
				replyVO.setUser(userInfo);
			}
			// 设置被回复用户昵称
			Optional<UserInfoDes> replyUser = userInfos.stream().filter(userInfo -> Objects.equals(reply.getRepliedUserId(), userInfo.getId())).findFirst();
			if (replyUser.isPresent()) {
				UserInfoDes userInfo = replyUser.get();
				replyVO.setRepliedNickname(userInfo.getNickname());
			}
			return replyVO;
		}).collect(Collectors.toList());
	}


	/**
	 * 获取根评论ID
	 * <p>
	 * 递归查找评论树的根节点ID，用于确定评论所属的主评论
	 * 对于主评论，返回自身ID；对于回复，返回其根评论ID
	 * <p>
	 * 递归逻辑：
	 * 1. 查询当前评论
	 * 2. 如果没有父ID，则为根评论，返回当前ID
	 * 3. 如果有父ID，则递归查询父评论的根ID
	 * 
	 * @param commentId 评论ID，可能是回复或主评论
	 * @return 根评论ID，即评论树的根节点ID
	 */
	private String getRootCommentId(String commentId) {
		Comment parent = commentMapper.selectById(commentId);
		return parent.getParentId() == null ?
				commentId :
				getRootCommentId(parent.getParentId());
	}
}