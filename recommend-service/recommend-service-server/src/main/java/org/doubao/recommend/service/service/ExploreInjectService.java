package org.doubao.recommend.service.service;

import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.domain.RecommendItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.domain.UserProfile;
import org.doubao.recommend.service.mapper.RecommendQuoteMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExploreInjectService {

    private static final int EXPLORE_SCAN_LIMIT = 40;

    @Resource
    private UserProfileService userProfileService;
    @Resource
    private RecommendQuoteMapper recommendQuoteMapper;
    @Resource
    private RankService rankService;
    @Resource
    private ContentFeatureService contentFeatureService;

    public List<RecommendItem> buildExploreItems(RecommendRequest request, Set<String> seen, Collection<RecommendItem> currentPage) {
        UserProfile profile = userProfileService.loadProfile(request.getUserIdentity());
        if (profile == null) {
            return Collections.emptyList();
        }
        int quota = resolveQuota(request.getPageSize());
        if (quota <= 0) {
            return Collections.emptyList();
        }

        Set<Long> excludedIds = new HashSet<>();
        if (currentPage != null) {
            currentPage.stream().filter(Objects::nonNull).map(RecommendItem::getContentId).filter(Objects::nonNull).forEach(excludedIds::add);
        }

        LinkedHashMap<Long, ContentFeature> featureMap = new LinkedHashMap<>();
        appendCandidates(featureMap, recommendQuoteMapper.selectExploreByExcludedTags(new ArrayList<>(profile.getTagWeights().keySet()), EXPLORE_SCAN_LIMIT), seen, excludedIds);
        appendCandidates(featureMap, recommendQuoteMapper.selectExploreByExcludedAuthors(new ArrayList<>(profile.getAuthorWeights().keySet()), EXPLORE_SCAN_LIMIT), seen, excludedIds);
        if (featureMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ContentFeature> enrichedFeatureMap = contentFeatureService.getBatchByIds(new ArrayList<>(featureMap.keySet())).stream()
                .collect(Collectors.toMap(ContentFeature::getContentId, feature -> feature, (left, right) -> left, LinkedHashMap::new));
        if (enrichedFeatureMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<CandidateItem> candidates = enrichedFeatureMap.values().stream().map(feature -> {
            CandidateItem item = new CandidateItem();
            item.setContentId(feature.getContentId());
            item.setRecallSource("explore:fresh_pool");
            item.setBaseScore(0.72);
            item.setReason(resolveReason(profile, feature));
            return item;
        }).collect(Collectors.toList());

        return rankService.rank(request, candidates, enrichedFeatureMap).stream().limit(quota).collect(Collectors.toList());
    }

    public void injectExploreItems(List<RecommendItem> page, List<RecommendItem> exploreItems, int pageSize) {
        if (page == null || exploreItems == null || exploreItems.isEmpty()) {
            return;
        }
        int insertions = Math.min(exploreItems.size(), Math.max(1, Math.min(2, pageSize)));
        int spacing = Math.max(2, pageSize / (insertions + 1));
        for (int i = 0; i < insertions; i++) {
            RecommendItem item = exploreItems.get(i);
            page.removeIf(existing -> existing != null && Objects.equals(existing.getContentId(), item.getContentId()));
            int index = Math.min(page.size(), spacing * (i + 1) - 1);
            page.add(index, item);
        }
        while (page.size() > pageSize) {
            page.remove(page.size() - 1);
        }
    }

    private void appendCandidates(Map<Long, ContentFeature> target, List<ContentFeature> source, Set<String> seen, Set<Long> excludedIds) {
        if (source == null) {
            return;
        }
        for (ContentFeature feature : source) {
            if (feature == null || feature.getContentId() == null) {
                continue;
            }
            if (excludedIds.contains(feature.getContentId())) {
                continue;
            }
            if (seen != null && seen.contains(String.valueOf(feature.getContentId()))) {
                continue;
            }
            target.putIfAbsent(feature.getContentId(), feature);
        }
    }

    private int resolveQuota(Integer pageSize) {
        int size = pageSize == null || pageSize <= 0 ? 20 : pageSize;
        return size >= 12 ? 2 : 1;
    }

    private String resolveReason(UserProfile profile, ContentFeature feature) {
        boolean newAuthor = feature.getAuthor() != null && !feature.getAuthor().isEmpty()
                && !profile.getAuthorWeights().containsKey(feature.getAuthor());
        boolean newTag = feature.tagList().stream().anyMatch(tag -> !profile.getTagWeights().containsKey(tag));
        if (newAuthor && newTag) {
            return "explore_new_author_tag";
        }
        if (newAuthor) {
            return "explore_new_author";
        }
        return "explore_new_tag";
    }
}
