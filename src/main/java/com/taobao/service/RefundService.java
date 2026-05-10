package com.taobao.service;

import com.taobao.common.R;
import com.taobao.entity.Orders;
import com.taobao.entity.Refund;

import java.util.List;

public interface RefundService {

    // 消费者提交退换货申请
    R<String> applyRefund(int consumerId, int orderId, int type, String reason);

    // 查询某订单的状态(返回整个对象方便前端用一些数据 可供双方使用)
    Refund getByOrderId(int orderId);

    //判断某订单是否有退换货条件（未签收 未评价 还未退换货）
    String canApplyRefund(int consumerId, int orderId);


    //用于展示售后页面可退换货的订单
    List<Orders> getAvailableOrders(int consumerId);
}
