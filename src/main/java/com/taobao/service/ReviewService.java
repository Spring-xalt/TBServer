package com.taobao.service;

import com.taobao.common.R;
import com.taobao.entity.Orders;
import com.taobao.entity.Review;

import java.util.List;

public interface ReviewService {

    // 提交评价
    R<String> submitReview(int consumerId, int orderId, int score, String content);

    // 查询消费者所有评价（个人中心用）
    List<Review> getConsumerReviews(int consumerId);

    // 查询商户所有评价（商户中心用）
    List<Review> getMerchantReviews(int merchantId);

    // 查询消费者可评价的订单列表（已签收+3个月内+未评价）
    List<Orders> getReviewableOrders(int consumerId);
}
