package org.doubao.share.service.service;

import org.doubao.share.service.dto.ShareVerifyDTO;
import org.doubao.share.service.vo.VerifyResultVO;

import javax.servlet.http.HttpServletRequest;

public interface VerifyService {
	VerifyResultVO verifyShareLink(ShareVerifyDTO dto, HttpServletRequest request);
}