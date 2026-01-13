package org.doubao.dialog.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.dialog.service.entity.AssistantKnowledge;

import java.util.List;

/**
 * 知识库服务接口
 * 业务说明：定义AI知识库管理功能的接口，包括内容匹配、模块查询、条目增删改等
 * 适用场景：AI助手知识库管理、常见问题自动回复、FAQ功能实现
 */
public interface KnowledgeService extends IService<AssistantKnowledge> {

	/**
	 * 根据内容匹配知识库
	 * 业务说明：根据用户输入内容在知识库中查找匹配的回复内容
	 * @param content 需要匹配的内容，通常是用户的问题
	 * @return 匹配到的回复内容，未匹配到返回null
	 */
	String matchKnowledge(String content);

	/**
	 * 根据模块查询知识库
	 * 业务说明：根据功能模块查询对应的知识库条目
	 * @param module 模块名称，如"执翎台"、"飞翎榜"等
	 * @return 知识库列表，包含该模块下的所有知识条目
	 */
	List<AssistantKnowledge> getByModule(String module);

	/**
	 * 添加知识库条目
	 * 业务说明：向知识库中添加新的条目
	 * @param knowledge 知识库条目对象，包含关键词、回复内容等信息
	 * @return 操作是否成功，true=成功，false=失败
	 */
	boolean addKnowledge(AssistantKnowledge knowledge);

	/**
	 * 更新知识库条目
	 * 业务说明：更新知识库中已有的条目
	 * @param knowledge 知识库条目对象，包含更新后的信息
	 * @return 操作是否成功，true=成功，false=失败
	 */
	boolean updateKnowledge(AssistantKnowledge knowledge);
}