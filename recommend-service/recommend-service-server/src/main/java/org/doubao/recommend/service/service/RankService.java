package org.doubao.recommend.service.service;

import org.doubao.recommend.service.domain.*;
import org.doubao.recommend.service.util.ScoreUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankService {

    @Resource
    private UserProfileService userProfileService;

    public Set<CandidateItem> deduplicate(Collection<CandidateItem> candidates) {
        Map<Long, CandidateItem> merged = new HashMap<>();
        for (CandidateItem c : candidates) {
            if (c == null || c.getContentId() == null) {
                continue;
            }
            CandidateItem old = merged.get(c.getContentId());
            if (old == null) {
                merged.put(c.getContentId(), c);
                continue;
            }
            old.setBaseScore(Math.max(defaultScore(old.getBaseScore()), defaultScore(c.getBaseScore())));
            old.setRecallSource(mergeText(old.getRecallSource(), c.getRecallSource()));
            old.setReason(mergeText(old.getReason(), c.getReason()));
        }
        return new LinkedHashSet<>(merged.values());
    }

    public List<RecommendItem> rank(RecommendRequest request, Collection<CandidateItem> candidates, Map<Long, ContentFeature> featureMap) {
        UserProfile profile = userProfileService.loadProfile(request.getUserIdentity());
        Map<String, Integer> authorFrequency = new HashMap<>();
        List<RecommendItem> result = new ArrayList<>();
        for (CandidateItem candidate : candidates) {
            ContentFeature feature = featureMap.get(candidate.getContentId());
            if (feature == null) {
                continue;
            }
            double interest = userProfileService.interestScore(profile, feature);
            double hot = feature.getHotScore() == null ? 0.0 : feature.getHotScore();
            double quality = feature.getQualityScore() == null ? 0.0 : feature.getQualityScore();
            double freshness = feature.getFreshnessScore() == null ? 0.0 : feature.getFreshnessScore();
            double explore = shouldExplore(candidate) ? 0.03 : 0.0;
            double diversityPenalty = authorPenalty(feature.getAuthor(), authorFrequency);
            double finalScore = 0.35 * interest + 0.25 * quality + 0.20 * hot + 0.10 * freshness
                    + 0.07 * defaultScore(candidate.getBaseScore()) + explore - diversityPenalty;
            finalScore += 0.02 * ScoreUtils.safeLog1p(feature.getContent() == null ? 0 : feature.getContent().length());
            RecommendItem item = buildItem(candidate, feature, hot, quality, freshness, finalScore);
            result.add(item);
            if (feature.getAuthor() != null) {
                authorFrequency.merge(feature.getAuthor(), 1, Integer::sum);
            }
        }
        result.sort(Comparator.comparing(RecommendItem::getScore, Comparator.nullsLast(Double::compareTo)).reversed()
                .thenComparing(RecommendItem::getContentId, Comparator.nullsLast(Long::compareTo)).reversed());
        return result;
    }

    private RecommendItem buildItem(CandidateItem candidate, ContentFeature feature, double hot, double quality, double freshness, double finalScore) {
        RecommendItem item = new RecommendItem();
        item.setContentId(feature.getContentId());
        item.setTitle(feature.getTitle());
        item.setContent(feature.getContent());
        item.setAuthor(feature.getAuthor());
        item.setSource(feature.getSource());
        item.setDynasty(feature.getDynasty());
        item.setPoetryCategory(feature.getPoetryCategory());
        item.setAuthorId(feature.getAuthorId());
        item.setTagVos(feature.getTagVos());
        item.setTopicNameVos(feature.getTopicNameVos());
        item.setScore(finalScore);
        item.setHotScore(hot);
        item.setQualityScore(quality);
        item.setFreshnessScore(freshness);
        item.setRecallSource(candidate.getRecallSource());
        item.setCreatedTime(feature.getCreatedTime());
        item.setOriginal(feature.getOriginal());
        item.setUserInfo(feature.getUserInfo());
        item.setFollow(feature.isFollow());
        return item;
    }

    private double authorPenalty(String author, Map<String, Integer> authorFrequency) {
        if (author == null) {
            return 0.0;
        }
        int count = authorFrequency.getOrDefault(author, 0);
        return count >= 2 ? 0.05 * (count - 1) : 0.0;
    }

    private boolean shouldExplore(CandidateItem candidate) {
        return candidate.getRecallSource() != null && (candidate.getRecallSource().contains("cold") || candidate.getRecallSource().contains("new"));
    }

    private double defaultScore(Double score) {
        return score == null ? 0.0 : score;
    }

    private String mergeText(String left, String right) {
        return Arrays.stream(new String[]{left, right})
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }
}
