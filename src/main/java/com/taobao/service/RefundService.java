package com.taobao.service;

import com.taobao.common.R;
import com.taobao.entity.Refund;

public interface RefundService {

    // 消费者提交退换货申请
    R<String> applyRefund(int consumerId, int orderId, int type, String reason);

    // 查询某订单的状态(返回整个对象方便前端用一些数据 可供双方使用)
    Refund getByOrderId(int orderId);


}
