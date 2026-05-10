package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.dto.OrderVO;
import com.taobao.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@SuppressWarnings("all")
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


    //清除1min内未支付订单
    @Delete("DELETE FROM orders WHERE status = 1 AND create_time < DATE_SUB(NOW(), INTERVAL 1 MINUTE)")
    int deleteExpiredUnpaidOrders();

    //清除3个月内已签收的订单
    @Delete("DELETE FROM orders WHERE status = 3 AND create_time < DATE_SUB(NOW(), INTERVAL 3 MONTH)")
    int deleteExpiredReceivedOrders();

    // 超过三个月的订单
    @Select("SELECT * FROM orders WHERE consumer_id = #{consumerId} AND status = 3 " +
            "AND create_time > DATE_SUB(NOW(), INTERVAL 3 MONTH)")
    List<Orders> selectReceivedRecentByConsumerId(@Param("consumerId") int consumerId);

}