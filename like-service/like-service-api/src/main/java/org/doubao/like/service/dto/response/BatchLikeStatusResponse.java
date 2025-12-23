package org.doubao.like.service.dto.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 批量点赞状态查询响应DTO
 * <p>
 * 包含多个实体的点赞状态和计数信息
 */
@ApiModel(description = "批量点赞状态响应结果")
public class BatchLikeStatusResponse {

	/**
	 * 查询结果列表
	 * <p>
	 * 按请求顺序返回每个实体的：
	 * - 点赞状态（是否已点赞）
	 * - 当前点赞总数
	 */
	@ApiModelProperty(
			value = "查询结果列表",
			required = true,
			notes = "按请求顺序返回每个实体的点赞状态和计数"
	)
	private List<LikeStatusResult> results;

	public List<LikeStatusResult> getResults() {
		return results;
	}

	public void setResults(List<LikeStatusResult> results) {
		this.results = results;
	}

	/**
	 * 单个实体的点赞状态结果
	 */
	@ApiModel(description = "单个实体的点赞状态详情")
	public static class LikeStatusResult {

		/**
		 * 实体类型
		 * <p>
		 */
		@ApiModelProperty(
				value = "实体类型",
				required = true
		)
		private int entityType;

		/**
		 * 实体ID
		 * <p>
		 * 对应文案ID或评论ID
		 */
		@ApiModelProperty(
				value = "实体ID",
				required = true,
				example = "789"
		)
		private String entityId;

		/**
		 * 是否已点赞
		 * <p>
		 * true: 当前用户已点赞该实体
		 * false: 当前用户未点赞
		 */
		@ApiModelProperty(
				value = "是否已点赞",
				required = true,
				example = "true",
				notes = "表示当前用户是否已点赞该实体"
		)
		private Boolean liked;

		/**
		 * 点赞总数
		 * <p>
		 * 该实体获得的总点赞数（包含所有用户的点赞）
		 */
		@ApiModelProperty(
				value = "点赞总数",
				required = true,
				example = "42",
				notes = "该实体获得的总点赞数"
		)
		private Integer count;

		public int getEntityType() {
			return entityType;
		}

		public void setEntityType(int entityType) {
			this.entityType = entityType;
		}

		public String getEntityId() {
			return entityId;
		}

		public void setEntityId(String entityId) {
			this.entityId = entityId;
		}

		public Boolean getLiked() {
			return liked;
		}

		public void setLiked(Boolean liked) {
			this.liked = liked;
		}

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}
	}

	/**
	 * 快速构建响应对象
	 *
	 * @param results 结果列表
	 * @return 构建好的响应对象
	 */
	public static BatchLikeStatusResponse of(List<LikeStatusResult> results) {
		BatchLikeStatusResponse response = new BatchLikeStatusResponse();
		response.setResults(results);
		return response;
	}

	/**
	 * 构建单个实体的结果对象
	 *
	 * @param entityType 实体类型
	 * @param entityId 实体ID
	 * @param isLiked 是否点赞
	 * @param count 点赞总数
	 * @return 单个结果对象
	 */
	public static LikeStatusResult buildResult(
			int entityType,
			String entityId,
			Boolean isLiked,
			Integer count
	) {
		LikeStatusResult result = new LikeStatusResult();
		result.setEntityType(entityType);
		result.setEntityId(entityId);
		result.setLiked(isLiked);
		result.setCount(count);
		return result;
	}
}