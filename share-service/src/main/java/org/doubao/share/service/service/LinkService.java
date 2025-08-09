package org.doubao.share.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.share.service.dto.ShareLinkCreateDTO;
import org.doubao.share.service.entity.ShareLink;
import org.doubao.share.service.vo.ShareLinkVO;

public interface LinkService extends IService<ShareLink> {
	ShareLinkVO createShareLink(ShareLinkCreateDTO dto);
	void invalidateLinksByQuoteId(String quoteId);
	ShareLink getShareLinkByUrl(String shareUrl);
	ShareLinkVO updateShareLink(Long linkId, ShareLinkCreateDTO dto);

	ShareLink getShareLinkByQuoteId(Long quoteId);
}
