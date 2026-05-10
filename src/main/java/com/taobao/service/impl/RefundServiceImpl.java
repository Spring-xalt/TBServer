package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.entity.Orders;
import com.taobao.entity.Product;
import com.taobao.entity.Refund;
import com.taobao.entity.Review;
import com.taobao.mapper.OrdersMapper;
import com.taobao.mapper.ProductMapper;
import com.taobao.mapper.RefundMapper;
import com.taobao.mapper.ReviewMapper;
import com.taobao.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Service
public class RefundServiceImpl implements RefundService {
    @Autowired
    private RefundMapper refundMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public R<String> applyRefund(int consumerId, int orderId, int type, String reason) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || order.getConsumer_id() != consumerId) {
            return R.error("订单不存在或无权操作");
        }

        // 我们设计只有已签收的才可申请
        if (order.getStatus() != 3) {
            return R.error("只有已签收的订单才能申请退换货");
        }

        // 下单30天内
        if (order.getCreate_time().plusDays(30).isBefore(LocalDateTime.now())) {
            return R.error("已超过30天退换货期限");
        }

        //  如果你都写完评价了 就不能
        Review review = reviewMapper.selectByOrderId(orderId);
        if (review != null) {
            return R.error("该订单已评价，无法申请退换货");
        }


        Refund existing = refundMapper.selectByOrderId(orderId);
        if (existing != null) {
            return R.error("该订单已申请过退换货，请勿重复申请");
        }

        // 只设置两种退换货方式
        if (type != 1 && type != 2) {
            return R.error("申请类型无效（1=退货退款，2=换货）");
        }

        //  倒查商品信息
        Product product = productMapper.selectOne(
                new QueryWrapper<Product>().eq("product_name", order.getProduct_name()));
        if (product == null) {
            return R.error("商品信息异常，无法申请");
        }

        // 入库
        Refund refund = new Refund();
        refund.setOrder_id(orderId);
        refund.setConsumer_id(consumerId);
        refund.setMerchant_id(product.getMerchant_id());
        refund.setProduct_id(product.getId());
        refund.setType(type);
        refund.setReason(reason);
        // 1=待审核,默认待审查
        refund.setStatus(1);

        refundMapper.insert(refund);
        return R.success("退换货申请已提交，等待商家审核");
    }

    @Override
    public Refund getByOrderId(int orderId) {
        return refundMapper.selectByOrderId(orderId);
    }
}
