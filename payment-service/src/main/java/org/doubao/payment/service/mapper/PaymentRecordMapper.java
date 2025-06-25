package org.doubao.payment.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.payment.service.entity.PaymentRecord;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}