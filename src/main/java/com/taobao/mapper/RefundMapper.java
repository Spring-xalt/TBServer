package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Refund;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RefundMapper extends BaseMapper<Refund> {

    // 判断该订单是否申请过了
    @Select("SELECT * FROM refund WHERE order_id = #{orderId}")
    Refund selectByOrderId(@Param("orderId") int orderId);
}


