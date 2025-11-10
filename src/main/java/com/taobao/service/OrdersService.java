package com.taobao.service;

import com.taobao.common.R;
import com.taobao.entity.Orders;
import com.taobao.mapper.OrdersMapper;

import java.util.List;

public interface OrdersService {
    List<Orders> getAllOrders();
    Orders getOrderById(Integer id);

    List<Orders> getOrdersByConsumerId(Integer consumerId);
    List<Orders> getOrdersByMerchantId(Integer merchantId);
    boolean addOrder(Orders order);
    boolean updateOrder(Orders order);
    boolean deleteOrder(Integer id);

    R<String> payOrder(Integer id);
    R<String> confirm(Integer id);
}
