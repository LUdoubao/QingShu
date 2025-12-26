package org.doubao.dialog.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.doubao.dialog.service.entity.DialogSession;

/**
 * 会话表Mapper接口
 * 基于MyBatis-Plus BaseMapper，无需手动编写SQL（复杂查询需在XML中扩展）
 */
public interface DialogSessionMapper extends BaseMapper<DialogSession> {

    // 如需扩展复杂查询（如关联查询），可在此处定义方法并在XML中实现
    // 示例：查询用户所有未读会话数量
    // Integer countUnreadSessions(Long userId);
}
  