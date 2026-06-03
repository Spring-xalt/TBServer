package com.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.dto.ReviewVO;
import com.taobao.dto.SubmitReviewDto;
import com.taobao.entity.Orders;
import com.taobao.entity.Review;
import com.taobao.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 查询消费者本人的所有评价,返回code和成功信息的
    @GetMapping("/my")
    public R<List<Review>> myReviews(HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<Review> list = reviewService.getConsumerReviews(consumerId);
        return R.success(list);
    }

    //用于消费者评价中心的 返回每一条详细信息的
    @GetMapping("/myDetail")
    public R<Map<String, Object>> myReviewDetails(
            HttpSession session,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) return R.error(401, "请先登录");
        IPage<ReviewVO> result = reviewService.getConsumerReviewsDetail(consumerId, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("reviews", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pages", result.getPages());
        return R.success(data);
    }






    // 商家端 查看某商户的评价
    @GetMapping("/merchant/list")
    public R<List<ReviewVO>> merchantReviews(HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        List<ReviewVO> list = reviewService.getMerchantReviewsDetail(merchantId);
        return R.success(list);
    }

    // 按商品ID查看评价（公开，无需登录）
    @GetMapping("/product/{productId}")
    public R<List<ReviewVO>> productReviews(@PathVariable("productId") int productId) {
        List<ReviewVO> list = reviewService.getProductReviews(productId);
        return R.success(list);
    }

}
