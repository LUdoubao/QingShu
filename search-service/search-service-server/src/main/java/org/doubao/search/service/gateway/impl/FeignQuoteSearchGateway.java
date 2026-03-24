package org.doubao.search.service.gateway.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.feign.QuoteClient;
import org.doubao.search.service.gateway.QuoteSearchGateway;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class FeignQuoteSearchGateway implements QuoteSearchGateway {

    @Resource
    private QuoteClient quoteClient;

    @Override
    public Map<String, String> getSuggestions(QueryContext context) {
        Result<Map<String, String>> response = quoteClient.getSearchSuggestions(context.getRawQuery());
        if (response == null || response.getData() == null) {
            return Collections.emptyMap();
        }
        return response.getData();
    }

    @Override
    public Page<SearchResultDTO> search(QueryContext context) {
        String type = context.getType();
        if (!"quote".equals(type) && !"tag".equals(type)) {
            throw new BusinessException(ErrorCode.SEARCH_TYPE_NOT_SUPPORT);
        }

        Result<Map<String, Object>> response = quoteClient.searchQuotes(
                context.getRawQuery(),
                context.getPage(),
                context.getSize(),
                context.getUserId(),
                type
        );
        Map<String, Object> data = response == null ? null : response.getData();
        if (data == null) {
            return new Page<>(context.getPage(), context.getSize());
        }

        Page<SearchResultDTO> result = new Page<>(context.getPage(), context.getSize());
        result.setTotal(Long.parseLong(String.valueOf(data.get("total"))));
        Object records = data.get("records");
        List<SearchResultDTO> items = JSON.parseArray(JSON.toJSONString(records), SearchResultDTO.class);
        result.setRecords(items);
        return result;
    }
}
