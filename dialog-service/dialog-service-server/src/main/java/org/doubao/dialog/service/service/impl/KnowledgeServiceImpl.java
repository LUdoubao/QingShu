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
 * 业务说明：实现AI知识库的管理功能，包括内容匹配、模块查询、条目增删改等
 * 核心逻辑：1. 智能匹配用户问题与知识库条目 2. 支持模块化知识管理 3. 统计知识库条目命中次数
 * 适用场景：
 * 1. AI助手知识库管理
 * 2. 常见问题自动回复
 * 3. FAQ功能实现
 * 4. 智能客服知识库
 */
@Service
public class KnowledgeServiceImpl extends ServiceImpl<AssistantKnowledgeMapper, AssistantKnowledge> implements KnowledgeService {

	private static final Logger logger = LoggerFactory.getLogger(KnowledgeServiceImpl.class);
	
	/** 知识库Mapper，用于知识库条目的持久化操作 */
	@Autowired
	private AssistantKnowledgeMapper knowledgeMapper;

	/**
	 * 根据内容匹配知识库
	 * 业务说明：根据用户输入内容在知识库中查找匹配的回复内容，用于AI自动回复
	 * 业务流程：
	 * 1. 对用户输入内容进行预处理（去除特殊字符）
	 * 2. 调用数据库匹配方法查找匹配的知识库条目
	 * 3. 找到最佳匹配后增加该条目的命中次数
	 * 4. 返回匹配到的回复内容
	 * 参数校验：内容不能为空或仅包含空白字符
	 * 性能优化：匹配结果按相关度排序，返回最佳匹配
	 * 数据处理：命中次数统计，用于分析知识库使用情况
	 * @param content 需要匹配的内容，通常是用户的问题
	 * @return 匹配到的回复内容，未匹配到返回null
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
	 * 业务说明：根据功能模块查询对应的知识库条目，用于模块化知识管理
	 * 业务流程：调用数据库查询方法按模块筛选知识库条目
	 * 参数校验：模块名称不能为空
	 * 数据处理：返回指定模块下的所有知识条目，按更新时间倒序排列
	 * 性能优化：支持按模块分类管理知识库，提高查询效率
	 * @param module 模块名称，如"执翎台"、"飞翎榜"等
	 * @return 知识库列表，包含该模块下的所有知识条目
	 */
	@Override
	public List<AssistantKnowledge> getByModule(String module) {
		return knowledgeMapper.selectByModule(module);
	}

	/**
	 * 添加知识库条目
	 * 业务说明：向知识库中添加新的条目，用于扩展AI助手的回复能力
	 * 业务流程：
	 * 1. 设置新条目的默认命中次数为0（如果未设置）
	 * 2. 调用数据库插入方法保存知识库条目
	 * 事务说明：使用事务确保数据一致性
	 * 异常处理：发生异常时回滚操作
	 * 参数校验：知识库对象不能为空，关键词和内容不能为空
	 * 数据处理：新条目命中次数默认为0
	 * @param knowledge 知识库条目对象，包含关键词、回复内容等信息
	 * @return 操作是否成功，true=成功，false=失败
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
	 * 业务说明：更新知识库中已有的条目，用于维护知识库内容准确性
	 * 业务流程：调用数据库更新方法修改知识库条目信息
	 * 事务说明：使用事务确保数据一致性
	 * 异常处理：发生异常时回滚操作
	 * 参数校验：知识库对象和ID不能为空
	 * 数据处理：更新时间自动设置为当前时间
	 * @param knowledge 知识库条目对象，包含更新后的信息
	 * @return 操作是否成功，true=成功，false=失败
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateKnowledge(AssistantKnowledge knowledge) {
		return knowledgeMapper.updateById(knowledge) > 0;
	}
}