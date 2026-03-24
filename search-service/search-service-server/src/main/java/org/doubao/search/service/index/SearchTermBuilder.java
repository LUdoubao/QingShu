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

    private static final int MAX_CONTENT_FRAGMENT_LENGTH = 12;
    private static final int MAX_TITLE_FRAGMENT_LENGTH = 16;
    private static final int MAX_TOKENS_PER_FIELD = 24;
    private static final int MIN_TOKEN_LENGTH = 2;

    @Resource
    private QueryPreprocessor queryPreprocessor;

    public List<SearchTermIndexDO> build(SearchDocIndexDO doc) {
        List<SearchTermIndexDO> terms = new ArrayList<>();
        addTerms(terms, doc, doc.getTitle(), "WORD", "title", 10);
        addTerms(terms, doc, doc.getContent(), "WORD", "content", 3);
        addTerms(terms, doc, doc.getAuthorName(), "AUTHOR", "author", 8);
        // addTerms(terms, doc, doc.getCategoryName(), "CATEGORY", "category", 7);
        addTerms(terms, doc, doc.getTagNamesText(), "TAG", "tag", 6);
        addPrefixTerms(terms, doc, doc.getAuthorName(), "PREFIX", "author", 5);
        // addPrefixTerms(terms, doc, doc.getCategoryName(), "PREFIX", "category", 4);
        addPrefixTerms(terms, doc, doc.getTagNamesText(), "PREFIX", "tag", 4);
        addPrefixTerms(terms, doc, doc.getTitle(), "PREFIX", "title", 4);
        return terms;
    }

    private void addTerms(List<SearchTermIndexDO> target, SearchDocIndexDO doc, String text, String termType,
                          String sourceField, int weight) {
        Set<String> normalizedTerms = tokenize(text, sourceField);
        for (String value : normalizedTerms) {
            target.add(buildTerm(doc, value, termType, sourceField, weight));
        }
    }

    private void addPrefixTerms(List<SearchTermIndexDO> target, SearchDocIndexDO doc, String text, String termType,
                                String sourceField, int weight) {
        String normalized = queryPreprocessor.normalize(text);
        if (normalized.isEmpty()) {
            return;
        }
        int max = Math.min(normalized.length(), 10);
        for (int i = 1; i <= max; i++) {
            target.add(buildTerm(doc, normalized.substring(0, i), termType, sourceField, weight));
        }
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

    private Set<String> tokenize(String text, String sourceField) {
        Set<String> tokens = new LinkedHashSet<String>();
        String normalized = queryPreprocessor.normalize(text);
        if (normalized.isEmpty()) {
            return tokens;
        }
        for (String fragment : normalized.split("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]+")) {
            collectFragmentTokens(tokens, fragment, sourceField);
            if (tokens.size() >= MAX_TOKENS_PER_FIELD) {
                break;
            }
        }
        return tokens;
    }

    private void collectFragmentTokens(Set<String> tokens, String fragment, String sourceField) {
        if (fragment == null) {
            return;
        }
        String normalizedFragment = fragment.trim();
        if (normalizedFragment.length() < MIN_TOKEN_LENGTH) {
            return;
        }
        if (shouldKeepWholeFragment(normalizedFragment, sourceField)) {
            tokens.add(normalizedFragment);
        }
        if (shouldGenerateBigrams(sourceField)) {
            addBigrams(tokens, normalizedFragment);
        }
    }

    private boolean shouldKeepWholeFragment(String fragment, String sourceField) {
        if ("content".equals(sourceField)) {
            return fragment.length() <= MAX_CONTENT_FRAGMENT_LENGTH;
        }
        if ("title".equals(sourceField)) {
            return fragment.length() <= MAX_TITLE_FRAGMENT_LENGTH;
        }
        return true;
    }

    private boolean shouldGenerateBigrams(String sourceField) {
        return "content".equals(sourceField) || "title".equals(sourceField);
    }

    private void addBigrams(Set<String> tokens, String fragment) {
        if (fragment.length() <= MIN_TOKEN_LENGTH) {
            return;
        }
        for (int i = 0; i < fragment.length() - 1; i++) {
            tokens.add(fragment.substring(i, i + 2));
            if (tokens.size() >= MAX_TOKENS_PER_FIELD) {
                return;
            }
        }
    }
}
