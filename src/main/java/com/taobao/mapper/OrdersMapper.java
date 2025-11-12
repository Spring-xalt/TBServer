package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Orders;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT * FROM orders WHERE consumer_id = #{consumerId}")
    List<Orders> selectOrdersByConsumerId(Integer consumerId);

    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId}")
    List<Orders> selectOrdersByMerchantId(Integer merchantId);
}