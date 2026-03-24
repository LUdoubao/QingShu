package org.doubao.search.service.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.assembler.SearchPageAssembler;
import org.doubao.search.service.converter.SearchDocConverter;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.entity.SearchCandidateDO;
import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.gateway.QuoteSearchGateway;
import org.doubao.search.service.mapper.SearchDocIndexMapper;
import org.doubao.search.service.mapper.SearchTermIndexMapper;
import org.doubao.search.service.rank.impl.RuleBasedRankService;
import org.doubao.search.service.recall.impl.MultiRouteRecallService;
import org.doubao.search.service.recall.strategy.ExactMatchRecallStrategy;
import org.doubao.search.service.recall.strategy.FallbackLikeRecallStrategy;
import org.doubao.search.service.recall.strategy.PrefixRecallStrategy;
import org.doubao.search.service.recall.strategy.RemoteQuoteRecallStrategy;
import org.doubao.search.service.recall.strategy.TermRecallStrategy;
import org.doubao.search.service.search.impl.DefaultSearchExecutionService;
import org.doubao.search.service.stats.SearchStatsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DefaultSearchExecutionServiceIntegrationTest.Config.class)
public class DefaultSearchExecutionServiceIntegrationTest {

    @Autowired
    private DefaultSearchExecutionService searchExecutionService;

    @MockBean
    private SearchDocIndexMapper searchDocIndexMapper;

    @MockBean
    private SearchTermIndexMapper searchTermIndexMapper;

    @MockBean
    private QuoteSearchGateway quoteSearchGateway;

    @MockBean
    private SearchStatsService searchStatsService;

    @Test
    public void shouldPreferLocalIndexRecallBeforeRemoteFallback() {
        SearchDocIndexDO localDoc = new SearchDocIndexDO();
        localDoc.setBizType("quote");
        localDoc.setBizId(11L);
        localDoc.setContent("苏轼词句");
        localDoc.setAuthorName("苏轼");
        localDoc.setCategoryName("诗词");
        localDoc.setSource("定风波");
        localDoc.setIsOriginal(1);
        localDoc.setStatus(1);
        localDoc.setIsDeleted(0);

        SearchCandidateDO candidate = new SearchCandidateDO();
        candidate.setBizId(11L);
        candidate.setRecallScore(30D);

        Mockito.when(searchDocIndexMapper.selectExactMatches(Mockito.eq("quote"), Mockito.eq("苏轼"), Mockito.anyInt()))
                .thenReturn(Collections.<SearchDocIndexDO>emptyList());
        Mockito.when(searchTermIndexMapper.selectByPrefix(Mockito.eq("quote"), Mockito.eq("苏轼"), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(candidate));
        Mockito.when(searchTermIndexMapper.selectByTerms(Mockito.eq("quote"), Mockito.anyList(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(candidate));
        Mockito.when(searchDocIndexMapper.selectByBizIds("quote", Collections.singletonList(11L)))
                .thenReturn(Collections.singletonList(localDoc));
        Mockito.when(searchDocIndexMapper.selectLikeMatches(Mockito.eq("quote"), Mockito.eq("苏轼"), Mockito.anyInt()))
                .thenReturn(Collections.<SearchDocIndexDO>emptyList());

        QueryContext context = new QueryContext("苏轼", "苏轼", "quote", 1, 10, 1L, Arrays.asList("苏轼"));
        Page<SearchResultDTO> page = searchExecutionService.search(context);

        Assertions.assertEquals(1L, page.getTotal());
        Assertions.assertEquals(1, page.getRecords().size());
        Assertions.assertEquals(Long.valueOf(11L), page.getRecords().get(0).getId());
        Mockito.verify(quoteSearchGateway, Mockito.never()).search(Mockito.any(QueryContext.class));
        Mockito.verify(searchStatsService).recordSearch(context, 1L);
    }

    @TestConfiguration
    static class Config {
        @Bean
        public SearchDocConverter searchDocConverter() {
            return new SearchDocConverter();
        }

        @Bean
        public ExactMatchRecallStrategy exactMatchRecallStrategy() {
            return new ExactMatchRecallStrategy();
        }

        @Bean
        public PrefixRecallStrategy prefixRecallStrategy() {
            return new PrefixRecallStrategy();
        }

        @Bean
        public TermRecallStrategy termRecallStrategy() {
            return new TermRecallStrategy();
        }

        @Bean
        public FallbackLikeRecallStrategy fallbackLikeRecallStrategy() {
            return new FallbackLikeRecallStrategy();
        }

        @Bean
        public RemoteQuoteRecallStrategy remoteQuoteRecallStrategy() {
            return new RemoteQuoteRecallStrategy();
        }

        @Bean
        public MultiRouteRecallService multiRouteRecallService() {
            return new MultiRouteRecallService();
        }

        @Bean
        public RuleBasedRankService ruleBasedRankService() {
            return new RuleBasedRankService();
        }

        @Bean
        public SearchPageAssembler searchPageAssembler() {
            return new SearchPageAssembler();
        }

        @Bean
        public DefaultSearchExecutionService defaultSearchExecutionService() {
            return new DefaultSearchExecutionService();
        }
    }
}
