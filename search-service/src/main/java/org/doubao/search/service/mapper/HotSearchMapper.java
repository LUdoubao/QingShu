package org.doubao.search.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.model.entity.HotSearch;
import org.doubao.search.service.model.vo.HotSearchVO;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HotSearchMapper extends BaseMapper<HotSearch> {
    List<HotSearch> selectByKeywordLike(@Param("keyword") String keyword);
    
    int incrementSearchCount(@Param("keyword") String keyword, @Param("increment") int increment);
    
    List<HotSearchVO> selectHotSearchesByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit);
}
