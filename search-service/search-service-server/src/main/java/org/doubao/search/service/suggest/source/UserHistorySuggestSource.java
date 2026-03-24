package org.doubao.search.service.suggest.source;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.entity.UserSearchHistoryDO;
import org.doubao.search.service.mapper.UserSearchHistoryMapper;
import org.doubao.search.service.suggest.SuggestSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class UserHistorySuggestSource implements SuggestSource {

    private static final int LIMIT = 5;

    @Resource
    private UserSearchHistoryMapper userSearchHistoryMapper;

    @Override
    public List<String> load(QueryContext context, Map<String, String> rawSuggestionData) {
        if (context.getUserId() == null || context.getNormalizedQuery().isEmpty()) {
            return Collections.emptyList();
        }
        List<UserSearchHistoryDO> historyList = userSearchHistoryMapper.selectUserHistory(context.getUserId(), LIMIT);
        List<String> matched = new ArrayList<>();
        for (UserSearchHistoryDO item : historyList) {
            if (item.getQueryTextNormalized() != null
                    && item.getQueryTextNormalized().startsWith(context.getNormalizedQuery())) {
                matched.add(item.getQueryText());
            }
        }
        return matched;
    }
}
