package org.doubao.like.service.dto.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 热门文案响应DTO
 * <p>
 * 返回按点赞数排序的热门文案列表
 */
@ApiModel(description = "热门文案响应数据")
public class HotContentResponse {

	/**
	 * 热门文案列表
	 * <p>
	 * 按点赞数从高到低排序，包含：
	 * - 文案ID
	 * - 当前点赞总数
	 */
	@ApiModelProperty(
			value = "热门文案列表",
			required = true,
			notes = "按点赞数降序排列的文案数据"
	)
	private List<HotContentItem> hotContents;

	public List<HotContentItem> getHotContents() {
		return hotContents;
	}

	public void setHotContents(List<HotContentItem> hotContents) {
		this.hotContents = hotContents;
	}

	/**
	 * 单个热门文案条目
	 */
	@ApiModel(description = "热门文案条目详情")
	public static class HotContentItem {

		/**
		 * 文案ID
		 * <p>
		 * 文案的唯一标识
		 */
		@ApiModelProperty(
				value = "文案ID",
				required = true,
				example = "789",
				notes = "热门文案的唯一标识"
		)
		private Long contentId;

		/**
		 * 点赞总数
		 * <p>
		 * 该文案获得的总点赞数
		 */
		@ApiModelProperty(
				value = "点赞总数",
				required = true,
				example = "420",
				notes = "该文案当前的总点赞数"
		)
		private Integer count;

		/**
		 * 排名变化
		 * <p>
		 * 相比上次排名的变化：
		 * - 正数表示上升
		 * - 负数表示下降
		 * - 0表示持平
		 * - null表示新上榜
		 */
		@ApiModelProperty(
				value = "排名变化",
				example = "2",
				notes = "相比上次排名的变化（正数=上升，负数=下降，0=持平，null=新上榜）"
		)
		private Integer rankChange;

		public Long getContentId() {
			return contentId;
		}

		public void setContentId(Long contentId) {
			this.contentId = contentId;
		}

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public Integer getRankChange() {
			return rankChange;
		}

		public void setRankChange(Integer rankChange) {
			this.rankChange = rankChange;
		}
	}


	/**
	 * 快速构建响应对象
	 *
	 * @param items 热门文案条目列表
	 * @return 构建好的响应对象
	 */
	public static HotContentResponse of(List<HotContentItem> items) {
		HotContentResponse response = new HotContentResponse();
		response.setHotContents(items);
		return response;
	}

	/**
	 * 构建单个热门文案条目
	 *
	 * @param contentId 文案ID
	 * @param count 点赞总数
	 * @param rankChange 排名变化
	 * @return 构建好的条目对象
	 */
	public static HotContentItem buildItem(
			Long contentId,
			Integer count,
			Integer rankChange
	) {
		HotContentItem item = new HotContentItem();
		item.setContentId(contentId);
		item.setCount(count);
		item.setRankChange(rankChange);
		return item;
	}

	/**
	 * 构建简单条目（无排名变化）
	 *
	 * @param contentId 文案ID
	 * @param count 点赞总数
	 * @return 构建好的条目对象
	 */
	public static HotContentItem buildSimpleItem(Long contentId, Integer count) {
		return buildItem(contentId, count, null);
	}
}