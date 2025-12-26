package org.doubao.fanout.service.controller;

import org.doubao.fanout.service.mapper.FanoutFailRecordMapper;
import org.doubao.fanout.service.model.FanoutFailRecord;
import org.doubao.fanout.service.service.FailMessageService;
import org.doubao.fanout.service.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/fanout")
public class FanoutController {

	@Resource
	private FailMessageService failMessageService;
	@Resource
	private FanoutFailRecordMapper fanoutFailRecordMapper;
	@Resource
	private RuleService ruleService;

	/**
	 * 手动重试失败消息
	 */
	@PostMapping("/retry/{recordId}")
	public ResponseEntity<Void> retryFailedMessage(@PathVariable Long recordId) {
		try {
			failMessageService.manualRetry(recordId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * 批量重试失败消息
	 */
	@PostMapping("/retry/batch")
	public ResponseEntity<Void> batchRetryFailedMessages(@RequestBody List<Long> recordIds) {
		try {
			for (Long id : recordIds) {
				failMessageService.manualRetry(id);
			}
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * 查询失败消息记录
	 */
	@GetMapping("/fail-records")
	public ResponseEntity<List<FanoutFailRecord>> getFailRecords(
			@RequestParam(required = false) String eventId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		try {
			List<FanoutFailRecord> records;
			if (eventId != null && !eventId.isEmpty()) {
				records = fanoutFailRecordMapper.findByEventId(eventId);
			} else {
				// 简单分页实现，实际项目中应使用Pageable
				records = fanoutFailRecordMapper.findAll();
			}
			return ResponseEntity.ok(records);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * 重新加载分发规则
	 */
	@PostMapping("/rules/reload")
	public ResponseEntity<Void> reloadRules() {
		try {
			ruleService.loadRules();
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
