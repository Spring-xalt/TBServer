package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.dto.ReviewVO;
import com.taobao.entity.*;
import com.taobao.mapper.*;
import com.taobao.service.ReviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Override
    @Transactional
    public R<String> submitReview(int consumerId, int orderId, int score, String content) {
        // 匹配消费者
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || order.getConsumer_id() != consumerId) {
            return R.error("订单不存在");
        }
        if (order.getStatus() != 3) {
            return R.error("只有已签收的订单才能评价");
        }
        Review existing = reviewMapper.selectByOrderId(orderId);
        if (existing != null) {
            return R.error("该订单已评价过");
        }


        if (order.getCreate_time().plusMonths(3).isBefore(java.time.LocalDateTime.now())) {
            return R.error("订单已超过3个月，无法评价");
        }
        //  校验评分
        if (score < 1 || score > 5) {
            return R.error("评分必须在1-5之间");
        }
        /*
         屎山代码雏形，orders表中忘记了product_id字段设计。导致这里很被动，更改成本又过大
         */
        // 根据商品名查商品表，获取 product_id 和 merchant_id
        Product product = productMapper.selectOne(
                new QueryWrapper<Product>().eq("product_name", order.getProduct_name()));
        if (product == null) {
            return R.error("商品信息异常，无法评价");
        }
        // 插入评价
        reviewMapper.insertReview(consumerId, product.getMerchant_id(),
                product.getId(), orderId, score, content);
        return R.success("评价成功");
    }

    @Override
    public List<Review> getConsumerReviews(int consumerId) {
        return reviewMapper.selectByConsumerId(consumerId);
    }


    @Override
    public List<Orders> getReviewableOrders(int consumerId) {
        //3个月以上订单
        List<Orders> orders = ordersMapper.selectReceivedRecentByConsumerId(consumerId);
        //已经评价的订单
        List<Integer> reviewedIds = reviewMapper.selectReviewedOrderIds(consumerId);
        Set<Integer> reviewedSet = new HashSet<>(reviewedIds);

        //  流式过滤
        return orders.stream()
                .filter(o -> !reviewedSet.contains(o.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewVO> getConsumerReviewsDetail(int consumerId) {
        List<Review> reviews = reviewMapper.selectByConsumerId(consumerId);
        List<ReviewVO> vos = new ArrayList<>();

        for (Review r : reviews) {
            ReviewVO vo = new ReviewVO();
            // 基础属性拷贝
            BeanUtils.copyProperties(r, vo);

            // 商品名
            Product product = productMapper.selectById(r.getProduct_id());
            if (product != null) {
                vo.setProductName(product.getProduct_name());
            }
            // 商户名
            Merchant merchant = merchantMapper.selectById(r.getMerchant_id());
            if (merchant != null) {
                vo.setMerchantName(merchant.getMerchant_name());
            }

            // 消费者昵称
            Consumer consumer = consumerMapper.selectById(r.getConsumer_id());
            if (consumer != null) {
                vo.setConsumerName(consumer.getConsumer_name());
            }
            vos.add(vo);
        }
        return vos;
    }


    @Override
    public List<ReviewVO> getMerchantReviewsDetail(int merchantId) {
        List<Review> reviews = reviewMapper.selectByMerchantId(merchantId);
        List<ReviewVO> voList = new ArrayList<>();

        for (Review r : reviews) {
            //复制基础评价表里的内容
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(r, vo);

            // 填充商品名，消费者名
            Product product = productMapper.selectById(r.getProduct_id());
            if (product != null) {
                vo.setProductName(product.getProduct_name());
            }
            Consumer consumer = consumerMapper.selectById(r.getConsumer_id());
            if (consumer != null) {
                vo.setConsumerName(consumer.getConsumer_name());
            }
            // 商户看自己的评价，可以不用，但 VO 有这个字段，留空即可
            voList.add(vo);
        }
        return voList;
    }


}
