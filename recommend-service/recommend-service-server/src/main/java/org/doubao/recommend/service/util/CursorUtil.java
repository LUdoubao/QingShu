package org.doubao.recommend.service.util;

import org.doubao.recommend.service.domain.CursorInfo;

import java.math.BigDecimal;

public final class CursorUtil {

    private CursorUtil() {
    }

    public static CursorInfo parseCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) {
            return null;
        }
        int idx = cursor.indexOf('_');
        if (idx <= 0 || idx >= cursor.length() - 1) {
            throw new IllegalArgumentException("invalid cursor format");
        }
        Double score = new BigDecimal(cursor.substring(0, idx)).doubleValue();
        Long contentId = Long.valueOf(cursor.substring(idx + 1));
        return new CursorInfo(score, contentId);
    }

    public static String buildCursor(Double score, Long contentId) {
        if (score == null || contentId == null) {
            return null;
        }
        return BigDecimal.valueOf(score).stripTrailingZeros().toPlainString() + "_" + contentId;
    }
}
