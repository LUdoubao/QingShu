package org.doubao.share.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.share.service.entity.ShareLink;

import java.util.List;

public interface ShareLinkMapper extends BaseMapper<ShareLink> {
	ShareLink selectByShareUrl(@Param("shareUrl") String shareUrl);
	List<ShareLink> selectByQuoteId(@Param("quoteId") String quoteId);
}
