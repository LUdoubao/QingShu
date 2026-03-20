package org.doubao.recommend.service.util;

import org.doubao.recommend.service.domain.CandidateItem;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class SeenFilterUtil {

    private SeenFilterUtil() {
    }

    public static Set<CandidateItem> filterCandidates(Collection<CandidateItem> candidates, Set<String> seen) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptySet();
        }
        if (seen == null || seen.isEmpty()) {
            return candidates.stream().collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return candidates.stream()
                .filter(candidate -> candidate != null && candidate.getContentId() != null)
                .filter(candidate -> !seen.contains(String.valueOf(candidate.getContentId())))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
