package com.taobao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.dto.OrderVO;
import com.taobao.dto.RefundListVO;
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


    //用于展示售后页面可退换货的订单（含商户名，分页）
    IPage<OrderVO> getAvailableOrders(int consumerId, int page, int size);

    //展示所有退换货订单
    List<Refund> listByConsumerId(int consumerId);

    //消费者查自己的退换货详情（含商品名、商户名、金额等，分页）
    IPage<RefundListVO> listConsumerRefundsDetail(int consumerId, int page, int size);

    //商户查看自己店铺下的退换货申请（分页）
    IPage<RefundListVO> listMerchantRefunds(int merchantId, Integer status, int page, int size);

    //商家处理退换货申请
    R<String> auditRefund(int merchantId, int refundId, String action);
}
