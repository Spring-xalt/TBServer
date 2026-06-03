package com.taobao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.dto.ReviewVO;
import com.taobao.entity.Orders;
import com.taobao.entity.Review;

import java.util.List;

public interface ReviewService {

    // 提交评价
    R<String> submitReview(int consumerId, int orderId, int score, String content);

    // 查询消费者所有评价
    List<Review> getConsumerReviews(int consumerId);

    // 查询消费者可评价的订单列表（已签收+3个月内+未评价）
    List<Orders> getReviewableOrders(int consumerId);

    //根据消费者id拿（分页）
    IPage<ReviewVO> getConsumerReviewsDetail(int consumerId, int page, int size);



    //根据商户id拿所有细节评价（分页）
    IPage<ReviewVO> getMerchantReviewsDetail(int merchantId, int page, int size);

    //根据商品id拿评价详情
    List<ReviewVO> getProductReviews(int productId);

}
