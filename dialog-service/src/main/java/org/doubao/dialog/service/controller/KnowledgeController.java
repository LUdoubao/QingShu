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
 * 知识库控制器
 */
@RestController
@RequestMapping("/dialog/knowledge")
@Api(tags = "知识库管理接口")
public class KnowledgeController {

	@Autowired
	private KnowledgeService knowledgeService;

	/**
	 * 获取所有知识库条目
	 */
	@GetMapping
	@ApiOperation("获取所有知识库条目")
	public ResponseEntity<List<AssistantKnowledge>> getAllKnowledge() {
		return ResponseEntity.ok(knowledgeService.list());
	}

	/**
	 * 根据模块获取知识库条目
	 */
	@GetMapping("/module/{module}")
	@ApiOperation("根据模块获取知识库条目")
	public ResponseEntity<List<AssistantKnowledge>> getKnowledgeByModule(@PathVariable String module) {
		return ResponseEntity.ok(knowledgeService.getByModule(module));
	}

	/**
	 * 添加知识库条目
	 */
	@PostMapping
	@ApiOperation("添加知识库条目")
	public ResponseEntity<Boolean> addKnowledge(@RequestBody AssistantKnowledge knowledge) {
		return ResponseEntity.ok(knowledgeService.addKnowledge(knowledge));
	}

	/**
	 * 更新知识库条目
	 */
	@PutMapping
	@ApiOperation("更新知识库条目")
	public ResponseEntity<Boolean> updateKnowledge(@RequestBody AssistantKnowledge knowledge) {
		return ResponseEntity.ok(knowledgeService.updateKnowledge(knowledge));
	}

	/**
	 * 删除知识库条目
	 */
	@DeleteMapping("/{id}")
	@ApiOperation("删除知识库条目")
	public ResponseEntity<Boolean> deleteKnowledge(@PathVariable Long id) {
		return ResponseEntity.ok(knowledgeService.removeById(id));
	}
}