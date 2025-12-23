package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.quote.service.entity.QuoteTag;
import org.doubao.quote.service.mapper.QuoteTagMapper;
import org.doubao.quote.service.service.QuoteTagService;
import org.springframework.stereotype.Service;

@Service
public class QuoteTagServiceImpl extends ServiceImpl<QuoteTagMapper, QuoteTag> implements QuoteTagService {
}
