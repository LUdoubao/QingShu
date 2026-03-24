package org.doubao.search.service.rank.impl;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.rank.SearchRankService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RuleBasedRankService implements SearchRankService {

    @Override
    public List<RecallDoc> rank(QueryContext context, List<RecallDoc> docs) {
        List<RecallDoc> ranked = new ArrayList<>(docs);
        for (RecallDoc doc : ranked) {
            doc.setScore(score(context, doc));
        }
        ranked.sort(Comparator.comparingDouble(RecallDoc::getScore).reversed());
        return ranked;
    }

    private double score(QueryContext context, RecallDoc doc) {
        double score = 0D;
        String normalizedQuery = context.getNormalizedQuery();
        String content = normalize(doc.getDocument().getContent());
        String author = normalize(doc.getDocument().getAuthor());
        String source = normalize(doc.getDocument().getSource());
        String category = normalize(doc.getDocument().getCategoryName());

        if (author.equals(normalizedQuery)) {
            score += 80;
        } else if (author.contains(normalizedQuery)) {
            score += 50;
        }
        if (category.equals(normalizedQuery)) {
            score += 60;
        }
        if (source.contains(normalizedQuery)) {
            score += 20;
        }
        if (content.contains(normalizedQuery)) {
            score += 40;
        }
        if (content.startsWith(normalizedQuery)) {
            score += 15;
        }
        if (doc.getDocument().getOriginal() == 1) {
            score += 5;
        }
        return score;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
