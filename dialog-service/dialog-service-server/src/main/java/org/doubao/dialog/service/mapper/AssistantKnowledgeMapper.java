package org.doubao.dialog.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.dialog.service.entity.AssistantKnowledge;

import java.util.List;

/**
 * 知识库Mapper接口
 */
@Mapper
public interface AssistantKnowledgeMapper extends BaseMapper<AssistantKnowledge> {

	/**
	 * 根据关键词匹配知识库
	 */
	List<AssistantKnowledge> matchByKeyword(@Param("keyword") String keyword);

	/**
	 * 根据模块查询知识库
	 */
	List<AssistantKnowledge> selectByModule(@Param("module") String module);

	/**
	 * 增加命中次数
	 */
	int incrementHitCount(@Param("id") Long id);
}
