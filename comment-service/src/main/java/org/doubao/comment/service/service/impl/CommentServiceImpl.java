package org.doubao.comment.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.doubao.comment.service.vo.CommentVO;
import org.doubao.comment.service.vo.ReplyVO;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
	@Autowired
	private CommentMapper commentMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Resource
	private CommentEventPublisher commentEventPublisher;
	// @Autowired
	// private AiClient aiClient;

	// @Autowired
	// private QuoteClient quoteClient;
	//
	@Autowired
	private UserClient userClient;

	@Autowired
	private LikeClient likeClient;

	@Override
	public String createComment(CommentDTO dto) {
		// 1. 敏感词检测
		// int riskScore = aiClient.checkContent(dto.getContent());
		// if (riskScore >= 90) {
		// 	throw new BusinessException("内容包含敏感词", ErrorCode.COMMENT_HAS_NOT_ALLOWED);
		// }

		// 2. 字数校验
		int contentLength = dto.getContent().length();
		if (contentLength < 10 || contentLength > 500) {
			throw new BusinessException(ErrorCode.COMMENT_NOT_IN_RANGE);
		}

		// 3. 频率限制
		long userId = UserContext.getUser().getId();
		String rateKey = "comment_rate:" + userId;
		Long count = redisTemplate.opsForValue().increment(rateKey, 1);
		if (count != null && count == 1) {
			redisTemplate.expire(rateKey, 10, TimeUnit.SECONDS);
		}
		if (count != null && count > 3) {
			throw new BusinessException(ErrorCode.COMMENT_LIMIT_REACHED);
		}

		// 4. 构建评论对象
		Comment comment = new Comment();
		comment.setContent(dto.getContent());
		comment.setPostId(dto.getPostId());
		comment.setUserId(Long.valueOf(userId));
		comment.setParentId(dto.getParentId());

		// 设置根评论ID
		if (dto.isReply()) {
			comment.setCommentId("R-" + System.currentTimeMillis() + RandomUtils.nextInt(1000));
			comment.setRootId(getRootCommentId(dto.getParentId()));
			comment.setRepliedUserId(dto.getRepliedUserId());
		} else {
			comment.setCommentId("C-" + System.currentTimeMillis() + RandomUtils.nextInt(1000));
			comment.setRootId(comment.getCommentId());
		}

		comment.setLikeCount(0L);
		comment.setReplyCount(0L);
		comment.setStatus(0);
		comment.setIsTop(0);

		// 5. 保存评论
		this.save(comment);

		// 发送评论通知
		Long repliedUserId = 0L;
		String originalComment = "";
		if (dto.isReply()) {
			// 查询被回复评论所属用户
			Comment parentComment = this.getById(comment.getParentId());
			repliedUserId = parentComment.getUserId();
			originalComment = parentComment.getContent();
		}

		String operatorUserName = Constants.DEFAULT_USER_NAME;
		String operatorUserAvatar = "";
		List<UserInfoDes> userInfos = userClient.getUsersByIds(Collections.singleton(userId)).getData();
		if (!CollectionUtils.isEmpty(userInfos)) {
			UserInfoDes userInfo = userInfos.get(0);
			operatorUserName = userInfo.getNickname();
			operatorUserAvatar = userInfo.getAvatarUrl();
		}
		commentEventPublisher.pushCommentNotification(repliedUserId, !dto.isReply(), comment.getPostId(), comment.getContent(), originalComment,
				userId, operatorUserName, operatorUserAvatar);
		// 7、删除缓存
		String cacheKey = "comments:post:" + dto.getPostId()+":*";
		Set<String> keys = redisTemplate.keys(cacheKey);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}

		return comment.getCommentId();
	}

	@Override
	public double calculateHotScore(Comment comment) {
		long likeCount = comment.getLikeCount() == null ? 0L : comment.getLikeCount();
		long replyCount = comment.getReplyCount() == null ? 0L : comment.getReplyCount();

		// 基础热度计算
		double baseScore = Math.log10(likeCount * 2 + replyCount + 0.1);

		// 作者权重
		// boolean isAuthorReplied = quoteClient.isAuthor(
		// 		comment.getPostId(),
		// 		comment.getUserId()
		// );
		boolean isAuthorReplied = false;

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

	@Override
	public Page<ReplyVO> getRepliesByCommentId(String commentId, Integer page, Integer size) {
		// 1. 校验参数
		if (page < 1) {
			page = 1;
		}
		if (size < 1 || size > 20) { // 限制最大每页20条
			size = 5;
		}

		// 2. 计算分页参数（MyBatis-Plus分页从1开始）
		Page<Comment> queryPage = new Page<>(page, size);

		// 3. 数据库查询（查询指定评论的子回复）
		IPage<Comment> replyPage = commentMapper.selectRepliesByCommentId(
				queryPage,
				commentId // 主评论ID（作为rootId）
		);
		Set<Long> userIds = replyPage.getRecords().stream().map(Comment::getUserId).collect(Collectors.toSet());
		userIds.addAll(replyPage.getRecords().stream().map(Comment::getRepliedUserId).collect(Collectors.toSet()));
		List<UserInfoDes> userInfos = userClient.getUsersByIds(userIds).getData();
		List<String> replyIds =  new ArrayList<>();
		Long userId = UserContext.getUser().getId();

		List<ReplyVO> replyVOList = replyPage.getRecords().stream()
				.map(comment -> {
					ReplyVO replyVO = new ReplyVO();
					BeanUtils.copyProperties(comment, replyVO);
					replyIds.add(comment.getCommentId());
					// 设置回复ID（使用评论表的ID）
					replyVO.setReplyId(comment.getCommentId());

					replyVO.setLikeCount(comment.getLikeCount());
					// 5. 补充用户信息（通过用户服务获取）
					Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> Objects.equals(comment.getUserId(), userInfo.getId())).findFirst();
					if (first.isPresent()) {
						UserInfoDes userInfo = first.get();
						replyVO.setUser(userInfo);
					}

					// 6. 处理被回复者信息
					userInfos.stream()
							.filter(userInfo -> Objects.equals(comment.getRepliedUserId(), userInfo.getId()))
							.findFirst()
							.ifPresent(userInfo -> replyVO.setRepliedNickname(userInfo.getNickname()));
					return replyVO;
				})
				.collect(Collectors.toList());

		// 3.1 设置是否点赞
		if (!replyIds.isEmpty()) {
			BatchLikeStatusRequest likeQueryDto = new BatchLikeStatusRequest();
			likeQueryDto.setEntities(replyIds.stream().map(replyId -> {
				BatchLikeStatusRequest.EntityRequest request = new BatchLikeStatusRequest.EntityRequest();
				request.setEntityId(replyId);
				request.setEntityType(1);
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


		Page<ReplyVO> replyVOPage = new Page<>(replyPage.getCurrent(), replyPage.getSize(),replyPage.getTotal());
		replyVOPage.setRecords(replyVOList);

		return replyVOPage;
	}

	@Override
	public ToggleLikeResponse toggleLike(String commentId) {
		Long operatorUserId = Long.valueOf(UserContext.getUser().getId());
		// 1. 查询评论信息（获取评论作者ID和内容）
		Comment comment = commentMapper.selectById(commentId);
		if (comment == null) {
			throw new BusinessException((ErrorCode.COMMENT_NOT_FOUND));
		}

		// 删除缓存
		String cacheKey = "comments:post:" + comment.getPostId()+":*";
		Set<String> keys = redisTemplate.keys(cacheKey);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
		// 2. 构造调用点赞服务的请求参数
		CommentLikeRequest likeRequest = new CommentLikeRequest();
		likeRequest.setOperatorUserId(operatorUserId);  // 点赞操作的用户
		likeRequest.setEntityType(1);  // 实体类型：1表示评论（与点赞服务约定）
		likeRequest.setEntityId(commentId);  // 评论ID（转为Long，根据实际ID类型调整）
		likeRequest.setUserId(comment.getUserId());  // 被点赞的评论作者ID
		likeRequest.setContent(comment.getContent());  // 评论内容（用于通知）

		// 3. 调用点赞服务的接口
		Result<ToggleLikeResponse> feignResult = likeClient.toggleLike(likeRequest);
		if (!feignResult.isSuccess()) {
			throw new BusinessException((ErrorCode.COMMENT_LIKE_ERROR));
		}

		// 4. 更新评论表中的点赞数（冗余字段，提高查询效率）
		ToggleLikeResponse response = feignResult.getData();
		comment.setLikeCount(response.getCurrentCount());
		commentMapper.updateById(comment);

		return response;
	}

	@Override
	public void updateStatus(Map<String, String> request) {
		String commentId = request.get("commentId");
		int status = Integer.parseInt(request.get("status"));
		LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(Comment::getCommentId, commentId);
		Comment comment = this.getOne(queryWrapper);
		if (comment == null) {
			return;
		}
		if (status == 1) {
			comment.setContent("该评论已被折叠");
		}
		comment.setStatus(status);
		this.updateById(comment);

		// 清除缓存
		String cacheKey = "comments:post:" + comment.getPostId() +":*";
		Set<String> keys = redisTemplate.keys(cacheKey);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}

	/**
	 * 获取评论列表（支持分页、排序）
	 */
	@Override
	public IPage<CommentVO> getCommentList(String postId, Page<Comment> page, String sortType) {
		Long userId = UserContext.getUser().getId();

		// 1. 尝试从缓存获取（热点数据）
		String cacheKey = "comments:post:" + postId + ":" + sortType + ":"  + userId + ":" + page.getCurrent() + ":" + page.getSize();
		IPage<CommentVO> cachedPage = (IPage<CommentVO>) redisTemplate.opsForValue().get(cacheKey);
		if (cachedPage != null) {
			return cachedPage;
		}

		// 2. 缓存未命中，从数据库查询
		// 2.1 查询一级评论（rootId = commentId）
		LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<Comment>()
				.eq(Comment::getPostId, postId)
				.isNull(Comment::getParentId) // 一级评论无父ID
				.in(Comment::getStatus, 0,1); // （折叠/正常）

		// 2.2 排序处理
		if ("hot".equals(sortType)) {
			// 按热度排序（数据库层面先按基础热度排序，内存中计算最终热度）
			// query.orderByDesc("like_count * 2 + reply_count");
		} else {
			// 按时间排序（最新在前）
			query.orderByDesc(Comment::getCreatedTime);
		}

		// 2.3 分页查询一级评论
		IPage<Comment> commentPage = commentMapper.selectPage(page, query);

		// 3. 转换为VO（包含用户信息、点赞状态、回复列表）
		Set<Long> userIds = commentPage.getRecords().stream().map(Comment::getUserId).collect((Collectors.toSet()));
		userIds.addAll(commentPage.getRecords().stream().map(Comment::getRepliedUserId).filter(Objects::nonNull).collect(Collectors.toSet()));
		List<UserInfoDes> userInfos = userClient.getUsersByIds(userIds).getData();

		List<String> commentIds = commentPage.getRecords().stream().map(Comment::getCommentId).collect(Collectors.toList());
		IPage<CommentVO> resultPage = commentPage.convert(comment -> {
			CommentVO vo = new CommentVO();
			BeanUtils.copyProperties(comment, vo);

			// 3.1 补充用户信息（远程调用用户服务）
			Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> Objects.equals(comment.getUserId(), userInfo.getId())).findFirst();
			if (first.isPresent()) {
				UserInfoDes userInfo = first.get();
				vo.setUser(userInfo);
			}

			// 3.2 补充前2条回复（嵌套查询）
			List<Comment> replies = commentMapper.selectReplies(
					comment.getCommentId(), 2); // 只查前2条
			List<ReplyVO> voList = convertReplies(replies);
			if (voList != null && !voList.isEmpty()) {
				List<String> collect = voList.stream().map(ReplyVO::getReplyId).collect(Collectors.toList());
				commentIds.addAll(collect);
			}
			vo.setReplyList(voList);

			// 3.3 统计回复数
			LambdaQueryWrapper<Comment> replyQuery = new LambdaQueryWrapper<Comment>()
					.eq(Comment::getPostId, postId)
					.eq(Comment::getParentId, comment.getCommentId())
					.eq(Comment::getStatus, 0);
			int count = this.count(replyQuery);
			vo.setReplyCount((long) count);

			// 3.4 计算热度值（仅hot排序时）
			if ("hot".equals(sortType)) {
				vo.setHotScore(calculateHotScore(comment));
			}

			// 3.5 标记置顶状态（从Redis获取置顶评论ID）
			String topCommentId = (String) redisTemplate.opsForValue().get("post:top_comment:" + postId);
			vo.setIsTop(Objects.equals(comment.getCommentId(), topCommentId) ? 1 : 0);

			return vo;
		});

		// 3.1 设置是否点赞
		if (!commentIds.isEmpty()) {
			BatchLikeStatusRequest likeQueryDto = new BatchLikeStatusRequest();
			likeQueryDto.setEntities(commentIds.stream().map(commentId -> {
				BatchLikeStatusRequest.EntityRequest request = new BatchLikeStatusRequest.EntityRequest();
				request.setEntityId(commentId);
				request.setEntityType(1);
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


		// 4. 按热度二次排序（内存中精确计算）
		if ("hot".equals(sortType)) {
			List<CommentVO> sortedComments = resultPage.getRecords().stream()
					.sorted(Comparator.comparingDouble(CommentVO::getHotScore).reversed())
					.collect(Collectors.toList());
			resultPage.setRecords(sortedComments);
		}

		// 5. 存入缓存（10分钟过期）
		redisTemplate.opsForValue().set(cacheKey, resultPage, 10, TimeUnit.MINUTES);

		return resultPage;
	}

	/**
	 * 转换回复列表为VO
	 */
	private List<ReplyVO> convertReplies(List<Comment> replies) {
		Set<Long> userIds = replies.stream().map(Comment::getUserId).collect(Collectors.toSet());
		userIds.addAll(replies.stream().map(Comment::getRepliedUserId).filter(Objects::nonNull).collect(Collectors.toSet()));
		List<UserInfoDes> userInfos = userClient.getUsersByIds(userIds).getData();
		return replies.stream().map(reply -> {
			ReplyVO replyVO = new ReplyVO();
			BeanUtils.copyProperties(reply, replyVO);
			replyVO.setReplyId(reply.getCommentId());
			replyVO.setLikeCount(reply.getLikeCount());
			replyVO.setAuthorTop(reply.getIsTop() == 1);
			Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> Objects.equals(reply.getUserId(), userInfo.getId())).findFirst();
			if (first.isPresent()) {
				UserInfoDes userInfo = first.get();
				replyVO.setUser(userInfo);
			}
			Optional<UserInfoDes> replyUser = userInfos.stream().filter(userInfo -> Objects.equals(reply.getRepliedUserId(), userInfo.getId())).findFirst();
			if (replyUser.isPresent()) {
				UserInfoDes userInfo = replyUser.get();
				replyVO.setRepliedNickname(userInfo.getNickname());
			}
			return replyVO;
		}).collect(Collectors.toList());
	}


	private String getRootCommentId(String commentId) {
		Comment parent = commentMapper.selectById(commentId);
		return parent.getParentId() == null ?
				commentId :
				getRootCommentId(parent.getParentId());
	}
}