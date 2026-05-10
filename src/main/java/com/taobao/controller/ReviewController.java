package com.taobao.controller;

import com.taobao.common.R;
import com.taobao.dto.SubmitReviewDto;
import com.taobao.entity.Orders;
import com.taobao.entity.Review;
import com.taobao.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody SubmitReviewDto dto, HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        return reviewService.submitReview(
                consumerId,
                dto.getOrderId(),
                dto.getScore(),
                dto.getContent() != null ? dto.getContent() : ""
        );
    }

    // 查询可评价的订单列表
    @GetMapping("/reviewable")
    public R<List<Orders>> reviewableOrders(HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<Orders> list = reviewService.getReviewableOrders(consumerId);
        return R.success(list);
    }

    // 查询消费者本人的所有评价
    @GetMapping("/my")
    public R<List<Review>> myReviews(HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<Review> list = reviewService.getConsumerReviews(consumerId);
        return R.success(list);
    }
}
