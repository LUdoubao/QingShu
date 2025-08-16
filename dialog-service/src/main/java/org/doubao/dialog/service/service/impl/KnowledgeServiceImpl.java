package org.doubao.dialog.service.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.dialog.service.entity.AssistantKnowledge;
import org.doubao.dialog.service.mapper.AssistantKnowledgeMapper;
import org.doubao.dialog.service.service.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 知识库服务实现类
 */
@Service
public class KnowledgeServiceImpl extends ServiceImpl<AssistantKnowledgeMapper, AssistantKnowledge> implements KnowledgeService {

	private static final Logger logger = LoggerFactory.getLogger(KnowledgeServiceImpl.class);
	@Autowired
	private AssistantKnowledgeMapper knowledgeMapper;

	/**
	 * 根据内容匹配知识库
	 */
	@Override
	public String matchKnowledge(String content) {
		if (content == null || content.trim().isEmpty()) {
			return null;
		}

		// 简单预处理，去除特殊字符
		String processedContent = content.replaceAll("[^a-zA-Z0-9一-龥]", " ");

		// 查找匹配的知识库条目
		List<AssistantKnowledge> matchedList = knowledgeMapper.matchByKeyword(processedContent);

		if (matchedList == null || matchedList.isEmpty()) {
			return null;
		}

		// 找到最佳匹配（这里简单取第一个，实际可以根据匹配度排序）
		AssistantKnowledge bestMatch = matchedList.get(0);

		// 增加命中次数
		knowledgeMapper.incrementHitCount(bestMatch.getId());

		return bestMatch.getContent();
	}

	/**
	 * 根据模块查询知识库
	 */
	@Override
	public List<AssistantKnowledge> getByModule(String module) {
		return knowledgeMapper.selectByModule(module);
	}

	/**
	 * 添加知识库条目
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean addKnowledge(AssistantKnowledge knowledge) {
		if (knowledge.getHitCount() == null) {
			knowledge.setHitCount(0);
		}
		return knowledgeMapper.insert(knowledge) > 0;
	}

	/**
	 * 更新知识库条目
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateKnowledge(AssistantKnowledge knowledge) {
		return knowledgeMapper.updateById(knowledge) > 0;
	}
}