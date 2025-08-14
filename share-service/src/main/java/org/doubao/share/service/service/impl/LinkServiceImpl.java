package org.doubao.share.service.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.share.service.dto.ShareLinkCreateDTO;
import org.doubao.share.service.entity.ShareLink;
import org.doubao.share.service.feign.QuoteServiceFeign;
import org.doubao.share.service.mapper.ShareLinkMapper;
import org.doubao.share.service.service.LinkService;
import org.doubao.mall.common.util.EncryptionUtil;
import org.doubao.share.service.vo.ShareLinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class LinkServiceImpl extends ServiceImpl<ShareLinkMapper, ShareLink> implements LinkService {
	@Value("${share.aes.key:doubao}")
	private String aesKey;
	@Autowired
	private ShareLinkMapper shareLinkMapper;

	@Autowired
	private QuoteServiceFeign quoteServiceFeign;

	@Autowired
	private EncryptionUtil encryptionUtil;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${share.base.url}")
	private String BASE_URL;
	private static final String REDIS_KEY_PREFIX = "share:link:";

	@Override
	@Transactional
	public ShareLinkVO createShareLink(ShareLinkCreateDTO dto) {
		// 1. 验证文案是否存在及类型是否匹配
		Map<String, String> request = new HashMap<>();
		request.put("quoteId", dto.getQuoteId());
		boolean exists = quoteServiceFeign.checkQuoteExists(request);
		if (!exists) {
			throw new IllegalArgumentException("文案不存在");
		}

		// 2. 构建分享链接
		String shareUrl = buildShareUrl(dto);

		// 3. 处理密码加密
		String encryptPassword = null;
		if ("PASSWORD".equals(dto.getAccessControl())) {
			try {
				encryptPassword = EncryptionUtil.encrypt(dto.getPassword(), aesKey);
			} catch (Exception e) {
				throw new RuntimeException("密码加密失败", e);
			}
		}

		// 4. 处理授权用户
		String authorizedUsersJson = null;
		if ("SPECIFIED".equals(dto.getAccessControl()) && dto.getAuthorizedUserIds() != null) {
			try {
				authorizedUsersJson = objectMapper.writeValueAsString(dto.getAuthorizedUserIds());
			} catch (JsonProcessingException e) {
				throw new RuntimeException("授权用户列表序列化失败", e);
			}
		}

		invalidateLinksByQuoteId(dto.getQuoteId());

		// 5. 保存到数据库
		ShareLink shareLink = new ShareLink();
		shareLink.setQuoteId(dto.getQuoteId());
		shareLink.setShareType("ORIGINAL".equals(dto.getShareType()) ? 1 : 0);
		shareLink.setAccessControl(getAccessControlCode(dto.getAccessControl()));
		shareLink.setEncryptPassword(encryptPassword);
		shareLink.setAuthorizedUsers(authorizedUsersJson);
		shareLink.setShareUrl(shareUrl);
		shareLink.setExpireTime(dto.getExpireTime());
		shareLink.setStatus(1); // 有效

		this.save(shareLink);

		// 6. 缓存到Redis
		cacheShareLink(shareLink);

		// 7. 构建返回结果
		ShareLinkVO vo = new ShareLinkVO();
		vo.setShareLink(BASE_URL + shareUrl);
		vo.setExpireTime(dto.getExpireTime());

		return vo;
	}

	@Override
	@Transactional
	public void invalidateLinksByQuoteId(String quoteId) {
		List<ShareLink> links = shareLinkMapper.selectByQuoteId(quoteId);
		for (ShareLink link : links) {
			link.setStatus(0); // 标记为无效
		}
		cleanCache(links, quoteId);
		this.updateBatchById(links);
	}

	private void  cleanCache(List<ShareLink> links, String quoteId) {
		for (ShareLink link : links) {
			redisTemplate.delete(REDIS_KEY_PREFIX + link.getShareUrl());
		}
		redisTemplate.delete(REDIS_KEY_PREFIX + quoteId);
	}

	@Override
	public ShareLink getShareLinkByUrl(String shareUrl) {
		// 先从缓存获取
		String cacheKey = REDIS_KEY_PREFIX + shareUrl;
		ShareLink shareLink = (ShareLink) redisTemplate.opsForValue().get(cacheKey);

		// 缓存不存在则从数据库获取
		if (shareLink == null) {
			shareLink = shareLinkMapper.selectByShareUrl(shareUrl);

			// 如果数据库存在，则缓存
			if (shareLink != null) {
				cacheShareLink(shareLink);
			}
		}

		return shareLink;
	}

	@Override
	@Transactional
	public ShareLinkVO updateShareLink(Long linkId, ShareLinkCreateDTO dto) {
		ShareLink existingLink = shareLinkMapper.selectById(linkId);
		if (existingLink == null) {
			throw new IllegalArgumentException("分享链接不存在");
		}

		// 处理密码加密
		String encryptPassword = existingLink.getEncryptPassword();
		if ("PASSWORD".equals(dto.getAccessControl())) {
			try {
				encryptPassword = EncryptionUtil.encrypt(dto.getPassword(), aesKey);
			} catch (Exception e) {
				throw new RuntimeException("密码加密失败", e);
			}
		}

		// 处理授权用户
		String authorizedUsersJson = existingLink.getAuthorizedUsers();
		if ("SPECIFIED".equals(dto.getAccessControl()) && dto.getAuthorizedUserIds() != null) {
			try {
				authorizedUsersJson = objectMapper.writeValueAsString(dto.getAuthorizedUserIds());
			} catch (JsonProcessingException e) {
				throw new RuntimeException("授权用户列表序列化失败", e);
			}
		}
		redisTemplate.delete(REDIS_KEY_PREFIX + existingLink.getShareUrl());

		// 更新数据库记录
		existingLink.setAccessControl(getAccessControlCode(dto.getAccessControl()));
		existingLink.setEncryptPassword(encryptPassword);
		existingLink.setAuthorizedUsers(authorizedUsersJson);
		existingLink.setExpireTime(dto.getExpireTime());

		this.updateById(existingLink);

		cacheShareLink(existingLink);

		// 构建返回结果
		ShareLinkVO vo = new ShareLinkVO();
		vo.setShareLink(BASE_URL + existingLink.getShareUrl());
		vo.setExpireTime(dto.getExpireTime());

		return vo;
	}

	@Override
	public ShareLink getShareLinkByQuoteId(Long quoteId) {
		String cacheKey = REDIS_KEY_PREFIX + quoteId;
		ShareLink shareLink = (ShareLink) redisTemplate.opsForValue().get(cacheKey);
		if (shareLink == null) {
			LambdaQueryWrapper<ShareLink> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(ShareLink::getQuoteId, quoteId)
					.eq(ShareLink::getStatus, 1);
			shareLink =  this.getOne(queryWrapper);
			cacheShareLink(shareLink);
		}
		if (shareLink != null) {
			shareLink.setShareUrl((BASE_URL + shareLink.getShareUrl()));
		}
		return shareLink;
	}

	private String buildShareUrl(ShareLinkCreateDTO dto) {
		StringBuilder urlBuilder = new StringBuilder();

		if ("ORIGINAL".equals(dto.getShareType())) {
			urlBuilder.append("original/").append(dto.getQuoteId());
		} else {
			urlBuilder.append("non-original/").append(dto.getQuoteId());
		}
		// 生成16位UUID
		String uuid = UUID.randomUUID().toString().replace("-", "");
		urlBuilder.append("/").append(uuid);
		return urlBuilder.toString();
	}

	private int getAccessControlCode(String accessControl) {
		switch (accessControl) {
			case "PUBLIC": return 1;
			case "PASSWORD": return 2;
			case "SPECIFIED": return 3;
			default: throw new IllegalArgumentException("无效的访问控制类型");
		}
	}

	private void cacheShareLink(ShareLink shareLink) {
		if (shareLink == null) {
			return;
		}
		String cacheKey = REDIS_KEY_PREFIX + shareLink.getShareUrl();

		// 设置缓存过期时间
		if (shareLink.getExpireTime() != null) {
			long expireSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), shareLink.getExpireTime());
			if (expireSeconds > 0) {
				redisTemplate.opsForValue().set(cacheKey, shareLink, expireSeconds, TimeUnit.SECONDS);
				redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + shareLink.getQuoteId(), shareLink,  expireSeconds, TimeUnit.SECONDS);
				return;
			}
		}

		// 永久链接缓存24小时
		redisTemplate.opsForValue().set(cacheKey, shareLink, 24, TimeUnit.HOURS);
		redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + shareLink.getQuoteId(), shareLink, 24, TimeUnit.HOURS);

	}
}
