package org.doubao.recommend.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.*;
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

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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
        Set<String> seen = loadSeen(request.getUserIdentity());
        userProfileService.loadProfile(request.getUserIdentity());
        Set<CandidateItem> recalled = ensureEnoughCandidates(request, seen);
        Map<Long, ContentFeature> featureMap = loadFeatures(recalled);
        List<CandidateItem> validCandidates = filterInvalidCandidates(recalled, featureMap);
        List<RecommendItem> ranked = rankService.rank(request, validCandidates, featureMap);
        List<RecommendItem> filtered = filterByCursor(ranked, cursor);
        List<RecommendItem> paged = filtered.stream().limit(request.getPageSize()).collect(Collectors.toList());

        FeedResponse response = new FeedResponse();
        response.setItems(paged);
        response.setHasMore(filtered.size() > paged.size());
        String nextCursor = paged.isEmpty() ? null : CursorUtil.buildCursor(paged.get(paged.size() - 1).getScore(), paged.get(paged.size() - 1).getContentId());
        response.setNextCursor(nextCursor);
        FeedSession session = saveSession(request, nextCursor, paged.size());
        response.setSessionId(session.getSessionId());
        markSeen(request.getUserIdentity(), paged);
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

    private FeedSession saveSession(RecommendRequest request, String nextCursor, int returned) {
        FeedSession session = loadSession(request);
        LocalDateTime now = LocalDateTime.now();
        if (session == null) {
            session = new FeedSession();
            session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
            session.setStartTime(now);
            session.setReturnedCount(0);
        }
        session.setLastRequestTime(now);
        session.setLastCursor(nextCursor);
        session.setReturnedCount((session.getReturnedCount() == null ? 0 : session.getReturnedCount()) + returned);
        if (request.getUserIdentity() != null && !request.getUserIdentity().trim().isEmpty()) {
            try {
                redisTemplate.opsForValue().set(RedisKeys.sessionKey(request.getUserIdentity(), request.getScene()), session, Duration.ofHours(cursorTtlHours));
            } catch (Exception ignored) {
            }
        }
        return session;
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
