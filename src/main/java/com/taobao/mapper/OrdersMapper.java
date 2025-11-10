package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Orders;

import java.util.List;

public interface OrdersMapper extends BaseMapper<Orders> {
    List<Orders> selectOrdersByConsumerId(Integer consuer_id);
    List<Orders> selectOrdersByMerchantId(Integer merchant_id);
}
