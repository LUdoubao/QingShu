package org.doubao.user.server.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.report.entity.ReportCategory;

import java.util.List;

/**
 * 举报分类Mapper接口
 */
@Mapper
public interface ReportCategoryMapper extends BaseMapper<ReportCategory> {

    /**
     * 查询所有启用的分类
     */
    List<ReportCategory> selectAllEnabled();
    /**
     * 根据父ID查询子分类
     */
    List<ReportCategory> selectByParentId(@Param("parentId") Long parentId);

}
