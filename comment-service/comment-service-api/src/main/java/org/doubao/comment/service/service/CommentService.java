package org.doubao.comment.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.comment.service.dto.CommentDTO;
import org.doubao.comment.service.dto.ReplyDTO;
import org.doubao.comment.service.dto.ToggleLikeResponse;
import org.doubao.comment.service.entity.Comment;
import org.doubao.comment.service.vo.CommentVO;
import org.doubao.comment.service.vo.ReplyVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 * <p>
 * 提供评论创建、查询、点赞、回复、状态管理等核心业务功能
 */
public interface CommentService extends IService<Comment> {
	/**
	 * 创建评论
	 * <p>
	 * 执行评论创建的完整业务流程，包括敏感词检测、字数校验、频率限制等
	 * 该方法会处理主评论和回复评论的创建逻辑，并发送相应的通知
	 * 
	 * @param dto 评论数据传输对象，包含评论内容、关联文章ID、父评论ID等信息
	 * @return 创建的评论唯一标识符，格式为"C-"前缀加时间戳和随机数（主评论）或
	 *         "R-"前缀加时间戳和随机数（回复评论）
	 */
	String createComment(CommentDTO dto);

	/**
	 * 获取评论列表
	 * <p>
	 * 根据文章ID分页获取评论列表，支持按热度或时间排序
	 * 包含缓存机制、用户信息查询、回复列表补充、点赞状态设置等功能
	 * 
	 * @param postId 关联文章ID
	 * @param page 分页参数
	 * @param sortType 排序类型，"hot"表示按热度排序，"time"表示按时间排序
	 * @return 评论视图对象的分页结果
	 */
	IPage<CommentVO> getCommentList(String postId, Page<Comment> page, String sortType);

	/**
	 * 计算评论热度分数
	 * <p>
	 * 基于点赞数、回复数和作者权重等因素计算评论的热度分数
	 * 使用对数函数防止热度值增长过快，确保排序的合理性
	 * 
	 * @param comment 评论实体
	 * @return 评论热度分数
	 */
	double calculateHotScore(Comment comment);

	/**
	 * 处理评论点赞
	 * <p>
	 * 此方法当前为空实现，实际点赞逻辑通过toggleLike方法实现
	 * 
	 * @param commentId 评论唯一标识符
	 * @param action 操作类型，"LIKE"表示点赞，"CANCEL"表示取消点赞
	 */
	void handleLike(String commentId, String action);

	/**
	 * 回复评论
	 * <p>
	 * 此方法当前为空实现，回复逻辑通过createComment方法实现
	 * 
	 * @param commentId 被回复的评论唯一标识符
	 * @param dto 回复数据传输对象
	 * @return 创建的回复唯一标识符
	 */
	String replyComment(String commentId, ReplyDTO dto);

	/**
	 * 处理评论置顶
	 * <p>
	 * 此方法当前为空实现，置顶逻辑可能在其他方法中实现
	 * 
	 * @param commentId 评论唯一标识符
	 * @param action 操作类型，"TOP"表示置顶，"CANCEL"表示取消置顶
	 */
	void handleTop(String commentId, String action);

	/**
	 * 删除评论
	 * <p>
	 * 逻辑删除指定评论，支持软删除功能
	 * 此方法当前为空实现，实际删除逻辑可能在其他方法中实现
	 * 
	 * @param commentId 评论唯一标识符
	 */
	void deleteComment(String commentId);

	/**
	 * 根据评论ID获取回复列表
	 * <p>
	 * 分页获取指定评论的回复列表，包括用户信息和点赞状态的查询与设置
	 * 
	 * @param commentId 评论唯一标识符
	 * @param page 页码
	 * @param size 每页大小
	 * @return 回复视图对象的分页结果
	 */
	Page<ReplyVO> getRepliesByCommentId(String commentId, Integer page, Integer size);

	/**
	 * 切换评论点赞状态
	 * <p>
	 * 执行评论点赞/取消点赞操作，并返回操作结果
	 * 通过调用点赞服务完成实际的点赞逻辑，并更新评论表中的点赞数冗余字段
	 * 
	 * @param commentId 评论唯一标识符
	 * @return 点赞操作的响应结果
	 */
	ToggleLikeResponse toggleLike(String commentId);

	/**
	 * 更新评论状态
	 * <p>
	 * 根据请求参数更新评论的状态信息，如折叠、删除等状态
	 * 操作完成后会清理相关缓存，确保数据一致性
	 * 
	 * @param request 包含评论ID和状态的请求参数映射
	 */
	void updateStatus(Map<String, String> request);

	/**
	 * 批量获取评论计数
	 * <p>
	 * 根据内容ID列表批量获取对应的评论数量
	 * 用于批量展示文章评论数的场景
	 * 
	 * @param contentIds 内容ID列表
	 * @return 内容ID到评论数量的映射
	 */
	Map<Long, Long> batchCounts(List<Long> contentIds);

	/**
	 * 批量统计每日评论数
	 * <p>
	 * 根据参数统计指定日期范围内每日的评论总数
	 * 用于数据统计和报表展示功能
	 * 
	 * @param params 统计参数，包含引文ID和日期列表
	 * @return 按日期分组的评论数量统计结果
	 */
	Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params);
}
