package org.doubao.dialog.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.dialog.service.entity.AssistantKnowledge;

import java.util.List;

/**
 * 知识库服务接口
 */
public interface KnowledgeService extends IService<AssistantKnowledge> {

	/**
	 * 根据内容匹配知识库
	 * @param content 匹配内容
	 * @return 匹配到的回复内容，未匹配到返回null
	 */
	String matchKnowledge(String content);

	/**
	 * 根据模块查询知识库
	 * @param module 模块名称
	 * @return 知识库列表
	 */
	List<AssistantKnowledge> getByModule(String module);

	/**
	 * 添加知识库条目
	 * @param knowledge 知识库条目
	 * @return 是否成功
	 */
	boolean addKnowledge(AssistantKnowledge knowledge);

	/**
	 * 更新知识库条目
	 * @param knowledge 知识库条目
	 * @return 是否成功
	 */
	boolean updateKnowledge(AssistantKnowledge knowledge);
}