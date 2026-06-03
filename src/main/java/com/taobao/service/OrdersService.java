package com.taobao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.dto.CartItem;
import com.taobao.dto.OrderVO;
import com.taobao.entity.Orders;
import com.taobao.mapper.OrdersMapper;

import java.util.List;

public interface OrdersService {
    List<Orders> getAllOrders();
    Orders getOrderById(Integer id);

    //通过消费者，商户id拿到订单
    List<Orders> getOrdersByConsumerId(Integer consumerId);
    List<Orders> getOrdersByMerchantId(Integer merchantId);

    boolean addOrder(Orders order);
    boolean updateOrder(Orders order);
    boolean deleteOrder(Integer id);

    //展示所有订单
    List<OrderVO> listMyOrders(int consumerId);

    //分页展示我的订单
    IPage<OrderVO> listMyOrdersPage(int consumerId, int page, int size);

   // prepare to delete
    R<String> confirm(Integer id);

    // 从购物车生成list（未支付的）
    List<Orders> createOrdersFromCart(int consumerId, List<CartItem> items);
    // 支付订单，同时改订单状态
    R<String> payOrders(int consumerId, List<Orders> orders, String password);


    // 商户找自己的评价
    List<OrderVO> listMerchantOrders(int merchantId);


}
