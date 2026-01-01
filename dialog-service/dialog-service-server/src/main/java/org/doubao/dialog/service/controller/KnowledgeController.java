package org.doubao.dialog.service.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.dialog.service.entity.AssistantKnowledge;
import org.doubao.dialog.service.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 知识库管理控制器
 * 适用场景：
 * 1. AI助手知识库管理
 * 2. 常见问题自动回复配置
 * 3. FAQ功能内容管理
 * 业务说明：处理AI知识库的增删改查操作，为AI助手提供智能回复能力
 */
@RestController
@RequestMapping("/dialog/knowledge")
@Api(tags = "知识库管理接口")
public class KnowledgeController {

	@Autowired
	private KnowledgeService knowledgeService;

	/**
	 * 获取所有知识库条目
	 * 业务说明：获取系统中所有的知识库条目，用于知识库管理界面展示
	 * @return 知识库条目列表，包含关键词、回复内容等信息
	 * 接口路径：GET /dialog/knowledge
	 * 访问权限：需管理员认证
	 * 业务流程：查询所有知识库条目→返回完整列表
	 */
	@GetMapping
	@ApiOperation("获取所有知识库条目")
	public ResponseEntity<List<AssistantKnowledge>> getAllKnowledge() {
		return ResponseEntity.ok(knowledgeService.list());
	}

	/**
	 * 根据模块获取知识库条目
	 * 业务说明：根据功能模块获取对应的知识库条目，用于模块化知识管理
	 * @param module 模块名称，如"执翎台"、"飞翎榜"等
	 * @return 指定模块的知识库条目列表
	 * 接口路径：GET /dialog/knowledge/module/{module}
	 * 访问权限：需管理员认证
	 * 业务流程：参数校验→按模块查询知识库→返回结果
	 */
	@GetMapping("/module/{module}")
	@ApiOperation("根据模块获取知识库条目")
	public ResponseEntity<List<AssistantKnowledge>> getKnowledgeByModule(@PathVariable String module) {
		return ResponseEntity.ok(knowledgeService.getByModule(module));
	}

	/**
	 * 添加知识库条目
	 * 业务说明：向知识库中添加新的条目，用于扩展AI助手的回复能力
	 * @param knowledge 知识库条目对象，包含关键词、回复内容、模块等信息
	 * @return 操作是否成功，true=成功，false=失败
	 * 接口路径：POST /dialog/knowledge
	 * 访问权限：需管理员认证
	 * 业务流程：参数校验→添加知识库条目→返回操作结果
	 */
	@PostMapping
	@ApiOperation("添加知识库条目")
	public ResponseEntity<Boolean> addKnowledge(@RequestBody AssistantKnowledge knowledge) {
		return ResponseEntity.ok(knowledgeService.addKnowledge(knowledge));
	}

	/**
	 * 更新知识库条目
	 * 业务说明：更新知识库中已有的条目，用于维护知识库内容准确性
	 * @param knowledge 知识库条目对象，包含更新后的信息
	 * @return 操作是否成功，true=成功，false=失败
	 * 接口路径：PUT /dialog/knowledge
	 * 访问权限：需管理员认证
	 * 业务流程：参数校验→更新知识库条目→返回操作结果
	 */
	@PutMapping
	@ApiOperation("更新知识库条目")
	public ResponseEntity<Boolean> updateKnowledge(@RequestBody AssistantKnowledge knowledge) {
		return ResponseEntity.ok(knowledgeService.updateKnowledge(knowledge));
	}

	/**
	 * 删除知识库条目
	 * 业务说明：从知识库中删除指定条目，用于移除过时或错误内容
	 * @param id 知识库条目ID，标识需要删除的条目
	 * @return 操作是否成功，true=成功，false=失败
	 * 接口路径：DELETE /dialog/knowledge/{id}
	 * 访问权限：需管理员认证
	 * 业务流程：参数校验→删除知识库条目→返回操作结果
	 */
	@DeleteMapping("/{id}")
	@ApiOperation("删除知识库条目")
	public ResponseEntity<Boolean> deleteKnowledge(@PathVariable Long id) {
		return ResponseEntity.ok(knowledgeService.removeById(id));
	}
}