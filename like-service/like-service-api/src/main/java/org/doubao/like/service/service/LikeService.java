package org.doubao.like.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.like.service.dto.request.BatchLikeStatusRequest;
import org.doubao.like.service.dto.request.ToggleLikeRequest;
import org.doubao.like.service.dto.response.BatchLikeStatusResponse;
import org.doubao.like.service.dto.response.HotContentResponse;
import org.doubao.like.service.dto.response.LikeQuoteVo;
import org.doubao.like.service.dto.response.ToggleLikeResponse;
import org.doubao.like.service.entity.LikeRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LikeService extends IService<LikeRecord> {

	ToggleLikeResponse toggleLike(ToggleLikeRequest request);

	BatchLikeStatusResponse batchGetLikeStatus(BatchLikeStatusRequest request);

	HotContentResponse getHotContents(String type, int page, int limit, Integer windowHours);

	Page<LikeQuoteVo> likeList(Long userId, int page, int size);

	Map<Long, Long> batchCounts(List<Long> contentIds);

	Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params);
}
