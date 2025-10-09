package org.doubao.user.server.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.doubao.user.server.report.entity.ReportCategory;

import java.util.List;

/**
 * 举报分类Mapper接口
 */
public interface ReportCategoryMapper extends BaseMapper<ReportCategory> {

    /**
     * 查询所有启用的分类
     */
    List<ReportCategory> selectAllEnabled();
}
