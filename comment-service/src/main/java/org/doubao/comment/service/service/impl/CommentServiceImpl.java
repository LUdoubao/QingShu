package org.doubao.comment.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.math.RandomUtils;
import org.doubao.comment.service.dto.CommentDTO;
import org.doubao.comment.service.dto.ReplyDTO;
import org.doubao.comment.service.entity.Comment;
import org.doubao.comment.service.feign.UserClient;
import org.doubao.comment.service.mapper.CommentMapper;
import org.doubao.comment.service.service.CommentService;
import org.doubao.comment.service.vo.CommentVO;
import org.doubao.comment.service.vo.ReplyVO;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	// @Autowired
	// private AiClient aiClient;

	// @Autowired
	// private QuoteClient quoteClient;
	//
	@Autowired
	private UserClient userClient;
	//
	// @Autowired
	// private LikeClient likeClient;

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
		Integer userId = Integer.valueOf(UserContext.getUser().getId());
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

		// 6. 异步处理
		// rabbitTemplate.convertAndSend("comment.queue", comment);
		// 7、删除缓存
		String cacheKey = "comments:post:" + dto.getPostId()+":*";
		Set<String> keys = redisTemplate.keys(cacheKey);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}

		return comment.getCommentId();
	}

	@Override
	public List<CommentVO> getHotComments(String postId, int limit, int offset) {
		String cacheKey = "hot_comments:" + postId;

		// 尝试从缓存获取
		List<CommentVO> cachedComments = (List<CommentVO>) redisTemplate.opsForValue().get(cacheKey);
		if (cachedComments != null) {
			return cachedComments.subList(offset, Math.min(offset + limit, cachedComments.size()));
		}

		// 缓存未命中，从数据库查询
		List<Comment> comments = commentMapper.selectByPostId(postId);

		// 计算热度并排序
		List<CommentVO> result = comments.stream()
				.map(this::convertToVO)
				.sorted(Comparator.comparingDouble(CommentVO::getHotScore).reversed())
				.collect(Collectors.toList());

		// 存入缓存，设置10分钟过期
		redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);

		return result.subList(offset, Math.min(offset + limit, result.size()));
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
		List<UserInfo> userInfos = userClient.getUsersByIds(userIds).getData();

		// 4. 转换为VO对象
		List<ReplyVO> replyVOList = replyPage.getRecords().stream()
				.map(comment -> {
					ReplyVO replyVO = new ReplyVO();
					BeanUtils.copyProperties(comment, replyVO);

					// 设置回复ID（使用评论表的ID）
					replyVO.setReplyId(comment.getCommentId());

					// 5. 补充用户信息（通过用户服务获取）
					Optional<UserInfo> first = userInfos.stream().filter(userInfo -> Objects.equals(comment.getUserId(), Long.valueOf(userInfo.getId()))).findFirst();
					if (first.isPresent()) {
						UserInfo userInfo = first.get();
						replyVO.setUser(userInfo);
					}

					// 6. 处理被回复者信息
					userInfos.stream()
							.filter(userInfo -> Objects.equals(comment.getRepliedUserId(), Long.valueOf(userInfo.getId())))
							.findFirst()
							.ifPresent(userInfo -> replyVO.setRepliedNickname(userInfo.getNickname()));
					return replyVO;
				})
				.collect(Collectors.toList());

		// 7. 封装分页结果

		Page<ReplyVO> replyVOPage = new Page<>(replyPage.getCurrent(), replyPage.getSize(),replyPage.getTotal());
		replyVOPage.setRecords(replyVOList);

		return replyVOPage;
	}

	/**
	 * 获取评论列表（支持分页、排序）
	 */
	@Override
	public IPage<CommentVO> getCommentList(String postId, Page<Comment> page, String sortType) {
		// 1. 尝试从缓存获取（热点数据）
		String cacheKey = "comments:post:" + postId + ":" + sortType + ":" + page.getCurrent() + ":" + page.getSize();
		IPage<CommentVO> cachedPage = (IPage<CommentVO>) redisTemplate.opsForValue().get(cacheKey);
		if (cachedPage != null) {
			return cachedPage;
		}

		// 2. 缓存未命中，从数据库查询
		// 2.1 查询一级评论（rootId = commentId）
		LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<Comment>()
				.eq(Comment::getPostId, postId)
				.isNull(Comment::getParentId) // 一级评论无父ID
				.eq(Comment::getStatus, 0); // 只查正常状态（未折叠/删除）

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
		List<UserInfo> userInfos = userClient.getUsersByIds(userIds).getData();

		IPage<CommentVO> resultPage = commentPage.convert(comment -> {
			CommentVO vo = new CommentVO();
			BeanUtils.copyProperties(comment, vo);

			// 3.1 补充用户信息（远程调用用户服务）
			Optional<UserInfo> first = userInfos.stream().filter(userInfo -> Objects.equals(comment.getUserId(), Long.valueOf(userInfo.getId()))).findFirst();
			if (first.isPresent()) {
				UserInfo userInfo = first.get();
				vo.setUser(userInfo);
			}

			// 3.2 补充点赞状态（当前登录用户是否点赞）
			Long currentUserId = Long.valueOf(UserContext.getUser().getId());
			// vo.setLike(likeClient.isLiked(comment.getCommentId(), currentUserId));
			vo.setLike(false);

			// 3.3 补充前2条回复（嵌套查询）
			List<Comment> replies = commentMapper.selectReplies(
					comment.getCommentId(), 2); // 只查前2条
			vo.setReplyList(convertReplies(replies));

			// 3.3.1 统计回复数
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
		List<UserInfo> userInfos = userClient.getUsersByIds(userIds).getData();
		return replies.stream().map(reply -> {
			ReplyVO replyVO = new ReplyVO();
			BeanUtils.copyProperties(reply, replyVO);
			replyVO.setReplyId(reply.getCommentId());
			replyVO.setAuthorTop(reply.getIsTop() == 1);
			Optional<UserInfo> first = userInfos.stream().filter(userInfo -> Objects.equals(reply.getUserId(), Long.valueOf(userInfo.getId()))).findFirst();
			if (first.isPresent()) {
				UserInfo userInfo = first.get();
				replyVO.setUser(userInfo);
			}
			Optional<UserInfo> replyUser = userInfos.stream().filter(userInfo -> Objects.equals(reply.getRepliedUserId(), Long.valueOf(userInfo.getId()))).findFirst();
			if (replyUser.isPresent()) {
				UserInfo userInfo = replyUser.get();
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

	private CommentVO convertToVO(Comment comment) {
		CommentVO vo = new CommentVO();
		BeanUtils.copyProperties(comment, vo);
		vo.setHotScore(calculateHotScore(comment));
		vo.setUser(UserContext.getUser());
		return vo;
	}

}