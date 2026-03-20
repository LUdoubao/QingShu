package org.doubao.recommend.service.common;

public final class RedisKeys {
    private RedisKeys() {}

    public static final String HOT_HOME_ZSET = "rec:hot:home";
    public static final String HOT_DETAIL_PREFIX = "rec:hot:detail:";
    public static final String PROFILE_PREFIX = "rec:profile:";
    public static final String FEATURE_PREFIX = "rec:feature:";
    public static final String SIMILAR_PREFIX = "rec:similar:";
    public static final String SEEN_PREFIX = "rec:seen:";
    public static final String CURSOR_PREFIX = "rec:cursor:";
    public static final String SESSION_PREFIX = "rec:session:";
    public static final String PRECOMPUTED_HOME_PREFIX = "rec:pre:home:";
    public static final String USER_ACTIVE_ZSET = "rec:active:users";
    public static final String RESULT_CACHE_PREFIX = "rec:result:";

    public static String cursorKey(String userIdentity, String scene) {
        return CURSOR_PREFIX + userIdentity + ":" + scene;
    }

    public static String sessionKey(String userIdentity, String scene) {
        return SESSION_PREFIX + userIdentity + ":" + scene;
    }
}
