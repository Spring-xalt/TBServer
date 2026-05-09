package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.dto.OrderVO;
import com.taobao.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT * FROM orders WHERE consumer_id = #{consumerId}")
    List<Orders> selectOrdersByConsumerId(Integer consumerId);

    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId}")
    List<Orders> selectOrdersByMerchantId(Integer merchantId);


    //关联查询
    @Select("SELECT o.*, m.merchant_name FROM orders o " +
            "JOIN merchant m ON o.merchant_id = m.id " +
            "WHERE o.consumer_id = #{consumerId} " +
            "ORDER BY o.create_time DESC")
    List<OrderVO> selectOrdersWithMerchant(@Param("consumerId") int consumerId);
}