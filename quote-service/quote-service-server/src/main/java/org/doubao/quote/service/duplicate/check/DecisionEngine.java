package org.doubao.quote.service.duplicate.check;

import org.doubao.quote.service.entity.CitationDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DecisionEngine {

	// 原创内容相似度阈值
	private static final double ORIGINAL_THRESHOLD = 0.15;
	// 非原创内容相似度阈值
	private static final double NON_ORIGINAL_THRESHOLD = 0.008;
	// 元数据相似度权重
	private static final double METADATA_WEIGHT = 0.1;

	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionEngine.class);

	public static DuplicationResult checkDuplication(
			CitationDocument newCitation,
			List<CitationDocument> existingCitations,
			boolean isOriginal
	) {
		double threshold = isOriginal ? ORIGINAL_THRESHOLD : NON_ORIGINAL_THRESHOLD;
		LOGGER.info("------------------Checking citation duplication for citation: {}", newCitation);
		List<SimilarCitation> similarCitations = new ArrayList<>();

		for (CitationDocument existing : existingCitations) {
			// 跳过自身比较
			if (existing.getId().equals(newCitation.getId())) continue;

			double similarity = calculateOverallSimilarity(newCitation, existing);
			LOGGER.info("Similarity between citation: {} and citation: {} is: {}", newCitation, existing, similarity);
			if (similarity >= threshold) {
				similarCitations.add(new SimilarCitation(existing, similarity));
				if (similarCitations.size() >= 2) break;
			}
		}

		// 排序相似引文（按相似度降序）
		similarCitations.sort(Comparator.comparing(SimilarCitation::getSimilarity).reversed());

		return new DuplicationResult(
				similarCitations.isEmpty() ? DuplicationStatus.UNIQUE : DuplicationStatus.DUPLICATE,
				similarCitations
		);
	}

	private static double calculateOverallSimilarity(CitationDocument citation1, CitationDocument citation2) {
		// 计算内容相似度
		double contentSim = SimilarityCalculator.simHashSimilarity(
				citation1.getContentSimHash(),
				citation2.getContentSimHash()
		);
		LOGGER.info("-----------------Content similarity between citation: {} and citation: {} is: {}", citation1.toString(), citation2.toString(), contentSim);

		// 计算元数据相似度
		double authorSim = SimilarityCalculator.editDistanceSimilarity(
				citation1.getAuthor(),
				citation2.getAuthor()
		);
		LOGGER.info("----------------Author similarity between citation: {} and citation: {} is: {}", citation1, citation2, authorSim);

		double sourceSim = SimilarityCalculator.editDistanceSimilarity(
				citation1.getSource(),
				citation2.getSource()
		);
		LOGGER.info("----------------Source similarity between citation: {} and citation: {} is: {}", citation1, citation2, sourceSim);

		double metadataSim = (authorSim + sourceSim) / 400.0;

		LOGGER.info("--------------Metadata similarity between citation: {} and citation: {} is: {}", citation1, citation2, metadataSim);
		// 加权综合相似度
		return (contentSim * 0.1 * (1 - METADATA_WEIGHT)) + (metadataSim * METADATA_WEIGHT);
	}

	public enum DuplicationStatus {
		UNIQUE, DUPLICATE
	}

	public static class DuplicationResult {
		private final DuplicationStatus status;
		private final List<SimilarCitation> similarCitations;

		public DuplicationResult(DuplicationStatus status, List<SimilarCitation> similarCitations) {
			this.status = status;
			this.similarCitations = similarCitations;
		}

		public DuplicationStatus getStatus() { return status; }
		public List<SimilarCitation> getSimilarCitations() { return similarCitations; }
	}

	public static class SimilarCitation {
		private final CitationDocument citation;
		private final double similarity;

		public SimilarCitation(CitationDocument citation, double similarity) {
			this.citation = citation;
			this.similarity = similarity;
		}

		public CitationDocument getCitation() { return citation; }
		public double getSimilarity() { return similarity; }
	}
}
