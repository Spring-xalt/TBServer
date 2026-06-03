package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        // 通过订单中的 product_id 直接查商品表
        Product product = productMapper.selectById(order.getProduct_id());
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
    public IPage<ReviewVO> getConsumerReviewsDetail(int consumerId, int page, int size) {
        int offset = (page - 1) * size;
        List<Review> reviews = reviewMapper.selectByConsumerIdPage(consumerId, offset, size);
        long total = reviewMapper.countByConsumerId(consumerId);
        List<ReviewVO> vos = new ArrayList<>();
        for (Review r : reviews) {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(r, vo);
            Product product = productMapper.selectById(r.getProduct_id());
            if (product != null) vo.setProductName(product.getProduct_name());
            Merchant merchant = merchantMapper.selectById(r.getMerchant_id());
            if (merchant != null) vo.setMerchantName(merchant.getMerchant_name());
            Consumer consumer = consumerMapper.selectById(r.getConsumer_id());
            if (consumer != null) vo.setConsumerName(consumer.getConsumer_name());
            vos.add(vo);
        }
        Page<ReviewVO> result = new Page<>(page, size, total);
        result.setRecords(vos);
        return result;
    }


    @Override
    public IPage<ReviewVO> getMerchantReviewsDetail(int merchantId, int page, int size) {
        int offset = (page - 1) * size;
        List<Review> reviews = reviewMapper.selectByMerchantIdPage(merchantId, offset, size);
        long total = reviewMapper.countByMerchantId(merchantId);
        List<ReviewVO> voList = new ArrayList<>();
        for (Review r : reviews) {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(r, vo);
            Product product = productMapper.selectById(r.getProduct_id());
            if (product != null) vo.setProductName(product.getProduct_name());
            Consumer consumer = consumerMapper.selectById(r.getConsumer_id());
            if (consumer != null) vo.setConsumerName(consumer.getConsumer_name());
            voList.add(vo);
        }
        Page<ReviewVO> result = new Page<>(page, size, total);
        result.setRecords(voList);
        return result;
    }

    @Override
    public List<ReviewVO> getProductReviews(int productId) {
        List<Review> reviews = reviewMapper.selectByProductId(productId);
        List<ReviewVO> voList = new ArrayList<>();
        for (Review r : reviews) {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(r, vo);
            Product product = productMapper.selectById(r.getProduct_id());
            if (product != null) vo.setProductName(product.getProduct_name());
            Consumer consumer = consumerMapper.selectById(r.getConsumer_id());
            if (consumer != null) vo.setConsumerName(consumer.getConsumer_name());
            voList.add(vo);
        }
        return voList;
    }


}
