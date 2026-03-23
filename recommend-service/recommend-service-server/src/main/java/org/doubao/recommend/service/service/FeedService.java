package org.doubao.recommend.service.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.TagVo;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.*;
import org.doubao.recommend.service.mapper.RecommendQuoteMapper;
import org.doubao.recommend.service.strategy.RecallStrategy;
import org.doubao.recommend.service.util.CursorUtil;
import org.doubao.recommend.service.util.SeenFilterUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FeedService {

    @Resource
    private List<RecallStrategy> recallStrategies;
    @Resource
    private RankService rankService;
    @Resource
    private ContentFeatureService contentFeatureService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserProfileService userProfileService;
    @Resource
    private RecommendQuoteMapper recommendQuoteMapper;
    @Resource
    private ExploreInjectService exploreInjectService;
    @Resource
    private QuoteService quoteService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private static final int FALLBACK_SCAN_BATCH_SIZE = 200;
    private static final int FALLBACK_SCAN_MAX_ROUNDS = 6;

    @Value("${feed.default-size:20}")
    private int defaultSize;
    @Value("${feed.max-candidates:300}")
    private int maxCandidates;
    @Value("${feed.seen-ttl-days:7}")
    private int seenTtlDays;
    @Value("${feed.cursor-ttl-hours:24}")
    private int cursorTtlHours;
    @Value("${feed.recall-limit-per-strategy:50}")
    private int recallLimitPerStrategy;

    public FeedResponse getFeed(RecommendRequest request) {
        normalizeRequest(request);
        CursorInfo cursor = loadCursor(request);
        FeedSession previousSession = loadSession(request);
        Set<String> seen = loadSeen(request.getUserIdentity());
        userProfileService.loadProfile(request.getUserIdentity());
        Set<CandidateItem> recalled = ensureEnoughCandidates(request, seen);
        Map<Long, ContentFeature> featureMap = loadFeatures(recalled);
        List<CandidateItem> validCandidates = filterInvalidCandidates(recalled, featureMap);
        List<RecommendItem> ranked = rankService.rank(request, validCandidates, featureMap);
        List<RecommendItem> filtered = filterByCursor(ranked, cursor);
        List<RecommendItem> page = new ArrayList<>(filtered.stream().limit(request.getPageSize()).collect(Collectors.toList()));
        boolean hasMore = filtered.size() > page.size();

        if (page.size() < request.getPageSize()) {
            List<RecommendItem> fallbackItems = loadFallbackItems(request, seen, previousSession, page);
            mergePageItems(page, fallbackItems, request.getPageSize());
            hasMore = hasMore || fallbackItems.size() >= Math.max(0, request.getPageSize() - filtered.size());
        }

        List<RecommendItem> exploreItems = exploreInjectService.buildExploreItems(request, seen, page);
        exploreInjectService.injectExploreItems(page, exploreItems, request.getPageSize());

        FeedResponse response = new FeedResponse();
        response.setItems(page);
        response.setHasMore(hasMore);
        String nextCursor = page.isEmpty() ? null : CursorUtil.buildCursor(page.get(page.size() - 1).getScore(), page.get(page.size() - 1).getContentId());
        response.setNextCursor(nextCursor);
        FeedSession session = saveSession(request, nextCursor, page, previousSession);
        response.setSessionId(session.getSessionId());
        markSeen(request.getUserIdentity(), page);
        saveCursor(request, nextCursor);
        return response;
    }

    private void normalizeRequest(RecommendRequest request) {
        request.setScene(request.getScene() == null || request.getScene().trim().isEmpty() ? "home" : request.getScene());
        int size = request.getPageSize() == null || request.getPageSize() <= 0 ? defaultSize : request.getPageSize();
        request.setPageSize(Math.min(size, defaultSize + 10));
        int requestedLimit = request.getLimit() == null || request.getLimit() <= 0 ? maxCandidates : request.getLimit();
        int minimumLimit = request.getPageSize() * Math.max(5, request.getPage() == null ? 1 : request.getPage());
        request.setLimit(Math.min(maxCandidates, Math.max(requestedLimit, minimumLimit)));
    }

    private CursorInfo loadCursor(RecommendRequest request) {
        if (request.getCursor() != null && !request.getCursor().trim().isEmpty()) {
            return CursorUtil.parseCursor(request.getCursor());
        }
        if (request.getUserIdentity() == null || request.getUserIdentity().trim().isEmpty()) {
            return null;
        }
        String cached = stringRedisTemplate.opsForValue().get(RedisKeys.cursorKey(request.getUserIdentity(), request.getScene()));
        return CursorUtil.parseCursor(cached);
    }

    private Set<String> loadSeen(String userIdentity) {
        if (userIdentity == null || userIdentity.trim().isEmpty()) {
            return new HashSet<>();
        }
        Set<String> seen = stringRedisTemplate.opsForSet().members(RedisKeys.SEEN_PREFIX + userIdentity);
        return seen == null ? new HashSet<>() : seen;
    }

    private Set<CandidateItem> ensureEnoughCandidates(RecommendRequest request, Set<String> seen) {
        int targetCandidateCount = Math.min(maxCandidates, Math.max(request.getLimit(), request.getPageSize() * 5));
        int dynamicRecallLimit = Math.max(recallLimitPerStrategy, Math.min(targetCandidateCount, request.getPageSize() * 3));
        LinkedHashSet<CandidateItem> merged = new LinkedHashSet<>();
        for (RecallStrategy strategy : recallStrategies) {
            if (!strategy.support(request)) {
                continue;
            }
            List<CandidateItem> recalled = strategy.recall(request);
            if (recalled == null || recalled.isEmpty()) {
                continue;
            }
            merged.addAll(recalled.stream().limit(dynamicRecallLimit).collect(Collectors.toList()));
            if (merged.size() >= targetCandidateCount) {
                break;
            }
        }
        Set<CandidateItem> deduplicated = rankService.deduplicate(merged);
        return SeenFilterUtil.filterCandidates(deduplicated, seen);
    }

    private Map<Long, ContentFeature> loadFeatures(Set<CandidateItem> candidates) {
        List<Long> ids = candidates.stream().map(CandidateItem::getContentId).filter(Objects::nonNull).limit(maxCandidates).collect(Collectors.toList());
        return contentFeatureService.getBatchByIds(ids).stream().collect(Collectors.toMap(ContentFeature::getContentId, item -> item, (a, b) -> a));
    }

    private List<CandidateItem> filterInvalidCandidates(Set<CandidateItem> candidates, Map<Long, ContentFeature> featureMap) {
        return candidates.stream().filter(candidate -> featureMap.containsKey(candidate.getContentId())).collect(Collectors.toList());
    }

    public List<RecommendItem> filterByCursor(List<RecommendItem> ranked, CursorInfo cursor) {
        if (cursor == null) {
            return ranked;
        }
        return ranked.stream()
                .filter(item -> isAfterCursor(item, cursor))
                .collect(Collectors.toList());
    }

    private boolean isAfterCursor(RecommendItem item, CursorInfo cursor) {
        if (item == null || item.getScore() == null || item.getContentId() == null || cursor.getScore() == null || cursor.getContentId() == null) {
            return false;
        }
        int scoreCompare = Double.compare(item.getScore(), cursor.getScore());
        if (scoreCompare < 0) {
            return true;
        }
        return scoreCompare == 0 && item.getContentId() < cursor.getContentId();
    }

    private void markSeen(String userIdentity, List<RecommendItem> items) {
        if (userIdentity == null || userIdentity.trim().isEmpty() || items == null || items.isEmpty()) {
            return;
        }
        String key = RedisKeys.SEEN_PREFIX + userIdentity;
        String[] values = items.stream().map(item -> String.valueOf(item.getContentId())).toArray(String[]::new);
        stringRedisTemplate.opsForSet().add(key, values);
        stringRedisTemplate.expire(key, Duration.ofDays(seenTtlDays));
    }

    private void saveCursor(RecommendRequest request, String nextCursor) {
        if (request.getUserIdentity() == null || request.getUserIdentity().trim().isEmpty() || nextCursor == null) {
            return;
        }
        String key = RedisKeys.cursorKey(request.getUserIdentity(), request.getScene());
        stringRedisTemplate.opsForValue().set(key, nextCursor, Duration.ofHours(cursorTtlHours));
    }

    private FeedSession saveSession(RecommendRequest request, String nextCursor, List<RecommendItem> returnedItems, FeedSession session) {
        LocalDateTime now = LocalDateTime.now();
        if (session == null) {
            session = new FeedSession();
            session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
            session.setStartTime(now);
            session.setReturnedCount(0);
        }
        session.setLastRequestTime(now);
        session.setLastCursor(nextCursor);
        int returned = returnedItems == null ? 0 : returnedItems.size();
        session.setReturnedCount((session.getReturnedCount() == null ? 0 : session.getReturnedCount()) + returned);
        session.setFallbackAnchorId(resolveFallbackAnchor(returnedItems, session));
        if (request.getUserIdentity() != null && !request.getUserIdentity().trim().isEmpty()) {
            try {
                redisTemplate.opsForValue().set(RedisKeys.sessionKey(request.getUserIdentity(), request.getScene()), session, Duration.ofHours(cursorTtlHours));
            } catch (Exception ignored) {
            }
        }
        return session;
    }

    private List<RecommendItem> loadFallbackItems(RecommendRequest request, Set<String> seen, FeedSession session, List<RecommendItem> currentPage) {
        int missing = request.getPageSize() - (currentPage == null ? 0 : currentPage.size());
        if (missing <= 0) {
            return Collections.emptyList();
        }
        LinkedHashMap<Long, RecommendItem> collected = new LinkedHashMap<>();
        if (currentPage != null) {
            currentPage.forEach(item -> {
                if (item != null && item.getContentId() != null) {
                    collected.put(item.getContentId(), item);
                }
            });
        }
        Long anchorId = session == null ? null : session.getFallbackAnchorId();
        int batchSize = Math.max(FALLBACK_SCAN_BATCH_SIZE, missing * 4);
        for (int round = 0; round < FALLBACK_SCAN_MAX_ROUNDS && collected.size() < request.getPageSize(); round++) {
            List<ContentFeature> features = recommendQuoteMapper.selectRecentPublishedBeforeId(request.getAuthor(), anchorId, batchSize);
            if (DoubaoUtils.isEmpty(features)) {
                break;
            }
            anchorId = features.stream()
                    .map(ContentFeature::getContentId)
                    .filter(Objects::nonNull)
                    .min(Long::compareTo)
                    .orElse(anchorId);
            List<ContentFeature> freshFeatures = features.stream()
                    .filter(Objects::nonNull)
                    .filter(feature -> feature.getContentId() != null)
                    .filter(feature -> !collected.containsKey(feature.getContentId()))
                    .filter(feature -> seen == null || !seen.contains(String.valueOf(feature.getContentId())))
                    .collect(Collectors.toList());
            if (freshFeatures.isEmpty()) {
                continue;
            }
            Map<Long, ContentFeature> fallbackFeatureMap = freshFeatures.stream()
                    .collect(Collectors.toMap(ContentFeature::getContentId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
            List<CandidateItem> fallbackCandidates = freshFeatures.stream().map(feature -> {
                CandidateItem item = new CandidateItem();
                item.setContentId(feature.getContentId());
                item.setRecallSource("fallback:deep_scan");
                item.setBaseScore(0.35);
                item.setReason("deep_scan_fill");
                return item;
            }).collect(Collectors.toList());
            List<RecommendItem> rankedFallback = rankService.rank(request, fallbackCandidates, fallbackFeatureMap);
            for (RecommendItem item : rankedFallback) {
                if (item == null || item.getContentId() == null || collected.containsKey(item.getContentId())) {
                    continue;
                }
                collected.put(item.getContentId(), item);
                if (collected.size() >= request.getPageSize()) {
                    break;
                }
            }
        }
        if (DoubaoUtils.isEmpty(collected)) {
            return new ArrayList<>();
        }
        if (currentPage == null || currentPage.isEmpty()) {
            ArrayList<RecommendItem> recommendItems = new ArrayList<>(collected.values());
            List<Long> ids = recommendItems.stream().map(RecommendItem::getContentId).collect(Collectors.toList());
            // 批量查询引文信息
            UserLoginVo user = UserContext.getUser();
            List<QuoteVo> quoteVos = quoteService.recommendList(ids, DoubaoUtils.isNotEmpty(user) ? user.getId() : null);
            recommendItems.forEach(feature -> {
                if (DoubaoUtils.isNotEmpty(quoteVos)) {
                    quoteVos.stream().filter(quote -> quote.getId().equals(feature.getContentId())).findFirst().ifPresent(
                            quoteVo -> convert(feature, quoteVo));
                }
            });
            return recommendItems;
        }
        List<RecommendItem> collect = collected.values().stream()
                .filter(item -> currentPage.stream().noneMatch(existing -> existing != null && Objects.equals(existing.getContentId(), item.getContentId())))
                .collect(Collectors.toList());
        List<Long> ids = collect.stream().map(RecommendItem::getContentId).collect(Collectors.toList());
        // 批量查询引文信息
        UserLoginVo user = UserContext.getUser();
        List<QuoteVo> quoteVos = quoteService.recommendList(ids, DoubaoUtils.isNotEmpty(user) ? user.getId() : null);
        collect.forEach(feature -> {
            if (DoubaoUtils.isNotEmpty(quoteVos)) {
                quoteVos.stream().filter(quote -> quote.getId().equals(feature.getContentId())).findFirst().ifPresent(
                        quoteVo -> convert(feature, quoteVo));
            }
        });
        return collect;
    }
    private void convert(RecommendItem feature, QuoteVo quoteVo) {
        feature.setContentId(quoteVo.getId());
        feature.setContent(quoteVo.getContent());
        feature.setTitle(quoteVo.getTitle());
        feature.setAuthor(quoteVo.getAuthor());
        feature.setDynasty(quoteVo.getDynasty());
        feature.setSource(quoteVo.getSource());
        feature.setTagVos(DoubaoUtils.isNotEmpty(quoteVo.getTags())
                ? JSON.parseArray(JSON.toJSONString(quoteVo.getTags()), TagVo.class)
                : null);
        feature.setTopicNameVos(DoubaoUtils.isNotEmpty(quoteVo.getTopic())
                ? Collections.singletonList(quoteVo.getTopic())
                : null);
        feature.setCreatedTime(quoteVo.getCreatedTime());
        feature.setOriginal(quoteVo.getOriginal());
        feature.setUserInfo(quoteVo.getUserInfo());
        feature.setFollow(quoteVo.isFollow());
    }
    private void mergePageItems(List<RecommendItem> page, List<RecommendItem> fallbackItems, int pageSize) {
        if (fallbackItems == null || fallbackItems.isEmpty()) {
            return;
        }
        Set<Long> existingIds = page.stream()
                .filter(Objects::nonNull)
                .map(RecommendItem::getContentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (RecommendItem item : fallbackItems) {
            if (item == null || item.getContentId() == null || existingIds.contains(item.getContentId())) {
                continue;
            }
            page.add(item);
            existingIds.add(item.getContentId());
            if (page.size() >= pageSize) {
                break;
            }
        }
    }

    private Long resolveFallbackAnchor(List<RecommendItem> returnedItems, FeedSession session) {
        Long anchor = returnedItems == null ? null : returnedItems.stream()
                .map(RecommendItem::getContentId)
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(null);
        if (anchor != null) {
            return anchor;
        }
        return session == null ? null : session.getFallbackAnchorId();
    }

    private FeedSession loadSession(RecommendRequest request) {
        if (request.getUserIdentity() == null || request.getUserIdentity().trim().isEmpty()) {
            return null;
        }
        Object session = redisTemplate.opsForValue().get(RedisKeys.sessionKey(request.getUserIdentity(), request.getScene()));
        if (session instanceof FeedSession) {
            return (FeedSession) session;
        }
        return objectMapper.convertValue(session, FeedSession.class);
    }
}
