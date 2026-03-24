package org.doubao.search.service.index;

import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.entity.SearchTermIndexDO;
import org.doubao.search.service.support.QueryPreprocessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class SearchTermBuilder {

    private static final int MAX_TOTAL_TERMS = 15;
    private static final int MAX_AUTHOR_TERMS = 3;
    private static final int MAX_TITLE_TERMS = 6;
    private static final int MAX_CONTENT_TERMS = 6;
    private static final int AUTHOR_MIN_LENGTH = 2;
    private static final int TITLE_MIN_LENGTH = 2;
    private static final int CONTENT_MIN_LENGTH = 3;

    @Resource
    private QueryPreprocessor queryPreprocessor;

    public List<SearchTermIndexDO> build(SearchDocIndexDO doc) {
        List<SearchTermIndexDO> terms = new ArrayList<SearchTermIndexDO>(MAX_TOTAL_TERMS);
        addAuthorTerms(terms, doc);
        addTitleTerms(terms, doc);
        addContentTerms(terms, doc);
        if (terms.size() > MAX_TOTAL_TERMS) {
            return new ArrayList<SearchTermIndexDO>(terms.subList(0, MAX_TOTAL_TERMS));
        }
        return terms;
    }

    private void addAuthorTerms(List<SearchTermIndexDO> target, SearchDocIndexDO doc) {
        addTerms(target, doc, collectAuthorTerms(doc.getAuthorName()), "AUTHOR", "author", 8, MAX_AUTHOR_TERMS);
        addTerms(target, doc, collectAuthorPrefixTerms(doc.getAuthorName()), "PREFIX", "author", 5,
                MAX_TOTAL_TERMS - target.size());
    }

    private void addTitleTerms(List<SearchTermIndexDO> target, SearchDocIndexDO doc) {
        int remaining = Math.min(MAX_TITLE_TERMS, MAX_TOTAL_TERMS - target.size());
        addTerms(target, doc, collectNgramTerms(doc.getTitle(), TITLE_MIN_LENGTH, 3, MAX_TITLE_TERMS),
                "WORD", "title", 10, remaining);
        remaining = Math.min(MAX_TITLE_TERMS, MAX_TOTAL_TERMS - target.size());
        addTerms(target, doc, collectPrefixTerms(doc.getTitle(), TITLE_MIN_LENGTH, 2),
                "PREFIX", "title", 4, remaining);
    }

    private void addContentTerms(List<SearchTermIndexDO> target, SearchDocIndexDO doc) {
        int remaining = Math.min(MAX_CONTENT_TERMS, MAX_TOTAL_TERMS - target.size());
        addTerms(target, doc, collectNgramTerms(doc.getContent(), CONTENT_MIN_LENGTH, 3, MAX_CONTENT_TERMS),
                "WORD", "content", 3, remaining);
    }

    private void addTerms(List<SearchTermIndexDO> target, SearchDocIndexDO doc, List<String> values,
                          String termType, String sourceField, int weight, int limit) {
        if (limit <= 0) {
            return;
        }
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (contains(target, sourceField, termType, value)) {
                continue;
            }
            target.add(buildTerm(doc, value, termType, sourceField, weight));
            if (target.size() >= MAX_TOTAL_TERMS || countBySource(target, sourceField) >= limit) {
                return;
            }
        }
    }

    private int countBySource(List<SearchTermIndexDO> target, String sourceField) {
        int count = 0;
        for (SearchTermIndexDO term : target) {
            if (sourceField.equals(term.getSourceField())) {
                count++;
            }
        }
        return count;
    }

    private boolean contains(List<SearchTermIndexDO> target, String sourceField, String termType, String value) {
        for (SearchTermIndexDO item : target) {
            if (sourceField.equals(item.getSourceField())
                    && termType.equals(item.getTermType())
                    && value.equals(item.getTermNormalized())) {
                return true;
            }
        }
        return false;
    }

    private List<String> collectAuthorTerms(String authorName) {
        List<String> terms = new ArrayList<String>();
        String normalized = queryPreprocessor.normalize(authorName);
        if (normalized.length() >= AUTHOR_MIN_LENGTH) {
            terms.add(normalized);
        }
        return terms;
    }

    private List<String> collectAuthorPrefixTerms(String authorName) {
        return collectPrefixTerms(authorName, AUTHOR_MIN_LENGTH, 2);
    }

    private List<String> collectPrefixTerms(String text, int minLength, int limit) {
        List<String> terms = new ArrayList<String>();
        String normalized = queryPreprocessor.normalize(text);
        if (normalized.length() < minLength) {
            return terms;
        }
        int max = Math.min(normalized.length(), minLength + limit - 1);
        for (int size = minLength; size <= max; size++) {
            terms.add(normalized.substring(0, size));
        }
        return terms;
    }

    private List<String> collectNgramTerms(String text, int minLength, int ngramLength, int limit) {
        Set<String> terms = new LinkedHashSet<String>();
        String normalized = queryPreprocessor.normalize(text);
        if (normalized.isEmpty()) {
            return new ArrayList<String>();
        }
        for (String fragment : normalized.split("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]+")) {
            String token = fragment == null ? "" : fragment.trim();
            if (token.length() < minLength) {
                continue;
            }
            addWholeToken(terms, token, minLength, limit);
            addSlidingTerms(terms, token, Math.max(minLength, ngramLength), limit);
            if (terms.size() >= limit) {
                break;
            }
        }
        return new ArrayList<String>(terms);
    }

    private void addWholeToken(Set<String> terms, String token, int minLength, int limit) {
        if (token.length() >= minLength) {
            terms.add(token);
        }
        trimToLimit(terms, limit);
    }

    private void addSlidingTerms(Set<String> terms, String token, int size, int limit) {
        if (token.length() < size) {
            return;
        }
        for (int i = 0; i <= token.length() - size; i++) {
            terms.add(token.substring(i, i + size));
            if (terms.size() >= limit) {
                return;
            }
        }
    }

    private void trimToLimit(Set<String> terms, int limit) {
        if (terms.size() <= limit) {
            return;
        }
        List<String> snapshot = new ArrayList<String>(terms);
        terms.clear();
        terms.addAll(snapshot.subList(0, limit));
    }

    private SearchTermIndexDO buildTerm(SearchDocIndexDO doc, String term, String termType, String sourceField, int weight) {
        SearchTermIndexDO item = new SearchTermIndexDO();
        item.setBizType(doc.getBizType());
        item.setBizId(doc.getBizId());
        item.setTerm(term);
        item.setTermNormalized(term);
        item.setTermType(termType);
        item.setSourceField(sourceField);
        item.setWeight(weight);
        return item;
    }
}
