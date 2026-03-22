package org.doubao.recommend.service.service;

import org.doubao.recommend.service.domain.*;
import org.doubao.recommend.service.util.ScoreUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankService {

    private static final double INTEREST_WEIGHT = 0.40;
    private static final double QUALITY_WEIGHT = 0.22;
    private static final double HOT_WEIGHT = 0.18;
    private static final double FRESHNESS_WEIGHT = 0.12;
    private static final double RECALL_WEIGHT = 0.08;
    private static final double TEXT_LENGTH_WEIGHT = 0.02;
    private static final double EXPLORE_BONUS = 0.05;
    private static final double TAG_DIVERSITY_BONUS = 0.015;
    private static final double AUTHOR_PENALTY_STEP = 0.02;
    private static final double AUTHOR_PENALTY_MAX = 0.08;

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
            double interest = normalize(userProfileService.interestScore(profile, feature));
            double hot = normalize(feature.getHotScore());
            double quality = normalize(feature.getQualityScore());
            double freshness = normalize(feature.getFreshnessScore());
            double baseScore = normalize(candidate.getBaseScore());
            double explore = shouldExplore(candidate) ? EXPLORE_BONUS : 0.0;
            double tagDiversityBonus = tagDiversityBonus(profile, feature, candidate);
            double diversityPenalty = authorPenalty(feature.getAuthor(), authorFrequency);
            double finalScore = INTEREST_WEIGHT * interest
                    + QUALITY_WEIGHT * quality
                    + HOT_WEIGHT * hot
                    + FRESHNESS_WEIGHT * freshness
                    + RECALL_WEIGHT * baseScore
                    + explore
                    + tagDiversityBonus
                    - diversityPenalty;
            finalScore += TEXT_LENGTH_WEIGHT * ScoreUtils.safeLog1p(feature.getContent() == null ? 0 : feature.getContent().length());
            RecommendItem item = buildItem(candidate, feature, hot, quality, freshness, finalScore);
            result.add(item);
            if (feature.getAuthor() != null) {
                authorFrequency.merge(feature.getAuthor(), 1, Integer::sum);
            }
        }
        result.sort(Comparator.comparing(RecommendItem::getScore, Comparator.nullsLast(Double::compareTo)).reversed());
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
        if (count < 2) {
            return 0.0;
        }
        return Math.min(AUTHOR_PENALTY_MAX, AUTHOR_PENALTY_STEP * (count - 1));
    }

    private boolean shouldExplore(CandidateItem candidate) {
        if (candidate.getReason() != null && candidate.getReason().contains("explore")) {
            return true;
        }
        return candidate.getRecallSource() != null && (candidate.getRecallSource().contains("cold") || candidate.getRecallSource().contains("new"));
    }

    private double tagDiversityBonus(UserProfile profile, ContentFeature feature, CandidateItem candidate) {
        if (profile == null || feature == null || candidate == null || candidate.getReason() == null || !candidate.getReason().contains("explore")) {
            return 0.0;
        }
        List<String> topTags = UserProfile.topEntries(profile.getTagWeights(), 3).stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        boolean unfamiliar = feature.tagList().stream().anyMatch(tag -> !topTags.contains(tag));
        return unfamiliar ? TAG_DIVERSITY_BONUS : 0.0;
    }

    private double defaultScore(Double score) {
        return score == null ? 0.0 : score;
    }

    private double normalize(Double score) {
        return Math.max(0.0, defaultScore(score));
    }

    private String mergeText(String left, String right) {
        return Arrays.stream(new String[]{left, right})
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }
}
