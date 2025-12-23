package org.doubao.quote.service.duplicate.check;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@Service
public class CitationCheckService {

	@Autowired
	private CitationRepository citationRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(CitationCheckService.class);
	// 检查新引文是否重复
	public DecisionEngine.DuplicationResult checkCitation(String content, String author, String source, boolean isOriginal) {
		// 预处理新引文
		CitationDocument newDoc = preprocessCitation(content, author, source, isOriginal);
		LOGGER.info("----------------New Citation: {}", JSON.toJSONString(newDoc));

		// 获取候选集
		List<CitationDocument> candidates = getCandidateCitations(newDoc);
		LOGGER.info("----------------Candidates: {}", JSON.toJSONString(candidates));

		// 执行详细比较
		DecisionEngine.DuplicationResult duplicationResult = DecisionEngine.checkDuplication(newDoc, candidates, isOriginal);
		LOGGER.info("----------------Duplication Result: {}", JSON.toJSONString(duplicationResult));

		if (duplicationResult.getStatus() == DecisionEngine.DuplicationStatus.UNIQUE) {
			// 不重复，保存新引文
			LOGGER.info("----------------不重复，保存新引文: {}", JSON.toJSONString(newDoc));
			addCitation(content, author, source, isOriginal);
		}
		return  duplicationResult;
	}

	// 添加新引文到数据库
	public void addCitation(String content, String author, String source, boolean isOriginal) {
		CitationDocument doc = preprocessCitation(content, author, source, isOriginal);
		LOGGER.info("----------------Preprocessed Citation: {}", JSON.toJSONString(doc));
		citationRepository.save(doc);
	}

	// 批量导入引文
	public void importCitations(List<CitationImport> citations) {
		List<CitationDocument> documents = new ArrayList<>();
		for (CitationImport citation : citations) {
			documents.add(preprocessCitation(
					citation.getContent(),
					citation.getAuthor(),
					citation.getSource(),
					citation.isOriginal()
			));
		}
		citationRepository.saveAll(documents);
	}

	// 预处理引文
	private CitationDocument preprocessCitation(String content, String author, String source, boolean isOriginal) {
		LOGGER.info("----------------Original Content: {}", content);
		String normalizedContent = TextUtils.normalizeText(content);
		LOGGER.info("----------------Normalized Content: {}", normalizedContent);


		LOGGER.info("----------------Original Author: {}", author);
		String normalizedAuthor = TextUtils.normalizeAuthor(author);
		LOGGER.info("----------------Normalized Author: {}", normalizedAuthor);

		LOGGER.info("----------------Original Source: {}", source);
		String normalizedSource = TextUtils.normalizeText(source);
		LOGGER.info("----------------Normalized Source: {}", normalizedSource);

		long[] simHash = TextUtils.computeSimHash(normalizedContent);
		LOGGER.info("----------------SimHash: {}", simHash);

		Set<String> trigrams = TextUtils.extractNGrams(normalizedContent, 3);
		LOGGER.info("----------------Trigrams: {}", JSON.toJSONString(trigrams));

		return new CitationDocument(
				normalizedContent,
				normalizedAuthor,
				normalizedSource,
				simHash,
				trigrams,
				isOriginal
		);
	}

	// 获取候选引文
	private List<CitationDocument> getCandidateCitations(CitationDocument newDoc) {
		List<CitationDocument> byContentSimHash = citationRepository.findByContentSimHash(newDoc.getContentSimHash());
		LOGGER.info("----------------Candidates by ContentSimHash: {}", JSON.toJSONString(byContentSimHash));
		// 使用SimHash查找
		Set<CitationDocument> candidates = new HashSet<>(byContentSimHash);
		List<CitationDocument> byAuthor = citationRepository.findByAuthor(newDoc.getAuthor());
		LOGGER.info("----------------Candidates by Author: {}", JSON.toJSONString(byAuthor));
		// 使用作者查找
		candidates.addAll(byAuthor);

		// 使用n-gram查找
		if (!newDoc.getTrigrams().isEmpty()) {
			List<CitationDocument> byTrigramsIn = citationRepository.findByTrigramsIn(newDoc.getTrigrams());
			LOGGER.info("----------------Candidates by Trigrams: {}", JSON.toJSONString(byTrigramsIn));
			candidates.addAll(byTrigramsIn);
		}

		return new ArrayList<>(candidates);
	}

	// 导入请求对象
	public static class CitationImport {
		private String content;
		private String author;
		private String source;
		private boolean isOriginal;

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public boolean isOriginal() {
			return isOriginal;
		}

		public void setOriginal(boolean original) {
			isOriginal = original;
		}
	}
}
