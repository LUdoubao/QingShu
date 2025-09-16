package org.doubao.search.service.es.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.doubao.search.service.es.model.dto.SearchRequestDto;

import java.util.Map;
import java.util.Objects;

public class SearchUtil {

    /**
     * 将筛选条件转换为哈希值
     * 用于缓存键生成或筛选条件的唯一标识
     *
     * @param filter 搜索筛选条件对象
     * @return 筛选条件对应的MD5哈希字符串，空筛选条件返回空字符串
     */
    public static String filterToHash(SearchRequestDto.SearchFilter filter) {
        if (filter == null) {
            return "";
        }

        // 将筛选条件对象转换为Map
        Map<String, Object> filterMap = BeanUtil.beanToMap(filter);
        // 移除值为null的条目，避免空值影响哈希结果
        filterMap.values().removeIf(Objects::isNull);

        // 转换为JSON字符串并计算MD5哈希
        String filterStr = JsonUtil.toJsonString(filterMap);
        // 使用Hutool工具类的MD5哈希方法（替代原MD5工具类）
        return DigestUtil.md5Hex(filterStr);
    }
}
