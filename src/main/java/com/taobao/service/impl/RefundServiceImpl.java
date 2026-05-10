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
import java.util.ArrayList;
import java.util.List;

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

    //事务级支持 防止有些写入了有些回滚了
    @Override
    @Transactional
    public R<String> applyRefund(int consumerId, int orderId, int type, String reason) {
        // 1. 公共校验
        String error = canApplyRefund(consumerId, orderId);
        if (error != null) {
            return R.error(error);
        }
        // 2. 类型校验
        if (type != 1 && type != 2) {
            return R.error("申请类型无效（1=退货退款，2=换货）");
        }
        // 3. 倒查商品信息
        Orders order = ordersMapper.selectById(orderId);
        Product product = productMapper.selectOne(
                new QueryWrapper<Product>().eq("product_name", order.getProduct_name()));
        if (product == null) {
            return R.error("商品信息异常，无法申请");
        }
        // 4. 入库
        Refund refund = new Refund();
        refund.setOrder_id(orderId);
        refund.setConsumer_id(consumerId);
        refund.setMerchant_id(product.getMerchant_id());
        refund.setProduct_id(product.getId());
        refund.setType(type);
        refund.setReason(reason);
        refund.setStatus(1);
        refundMapper.insert(refund);
        return R.success("退换货申请已提交，等待商家审核");
    }


    @Override
    public String canApplyRefund(int consumerId, int orderId) {
        // 订单存在且属于该消费者
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || order.getConsumer_id() != consumerId) {
            return "订单不存在或无权操作";
        }
        //  已签收
        if (order.getStatus() != 3) {
            return "只有已签收的订单才能申请退换货";
        }

        // 下单30天内
        if (order.getCreate_time().plusDays(30).isBefore(LocalDateTime.now())) {
            return "已超过30天退换货期限";
        }
        // 未评价
        Review review = reviewMapper.selectByOrderId(orderId);
        if (review != null) {
            return "该订单已评价，无法申请退换货";
        }
        // 未申请过
        Refund existing = refundMapper.selectByOrderId(orderId);
        if (existing != null) {
            return "该订单已申请过退换货，请勿重复申请";
        }
        return null;  // 表示可以申请
    }

    @Override
    public List<Refund> listByConsumerId(int consumerId) {
        return refundMapper.selectByConsumerId(consumerId);
    }


    @Override
    public List<Orders> getAvailableOrders(int consumerId) {
        // 查询条件：已签收且下单30天内，且属于当前消费者
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq("consumer_id", consumerId);
        wrapper.eq("status", 3);
        wrapper.gt("create_time", LocalDateTime.now().minusDays(30));
        List<Orders> candidateOrders = ordersMapper.selectList(wrapper);
        // 利用 canApplyRefund 进行完整校验（未评价、未申请等）
        List<Orders> result = new ArrayList<>();
        for (Orders order : candidateOrders) {
            if (canApplyRefund(consumerId, order.getId()) == null) {
                result.add(order);
            }
        }
        return result;
    }




    @Override
    public Refund getByOrderId(int orderId) {
        return refundMapper.selectByOrderId(orderId);
    }
}
