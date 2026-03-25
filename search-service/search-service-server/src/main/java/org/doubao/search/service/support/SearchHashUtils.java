package org.doubao.search.service.support;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public final class SearchHashUtils {

    private SearchHashUtils() {
    }

    public static String md5Hex(String value) {
        if (value == null) {
            return "";
        }
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }
}
