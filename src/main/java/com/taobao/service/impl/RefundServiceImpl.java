package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.dto.OrderVO;
import com.taobao.dto.RefundListVO;
import com.taobao.entity.*;
import com.taobao.mapper.*;
import com.taobao.service.RefundService;
import com.taobao.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ConsumerMapper consumerMapper;

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
        // 3. 通过订单中的 product_id 直接查商品信息
        Orders order = ordersMapper.selectById(orderId);
        Product product = productMapper.selectById(order.getProduct_id());
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
    public List<RefundListVO> listConsumerRefundsDetail(int consumerId) {
        List<Refund> refunds = refundMapper.selectByConsumerId(consumerId);
        List<RefundListVO> voList = new ArrayList<>();
        for (Refund r : refunds) {
            RefundListVO vo = new RefundListVO();
            vo.setId(r.getId());
            vo.setOrderId(r.getOrder_id());
            vo.setConsumerId(r.getConsumer_id());
            vo.setProductId(r.getProduct_id());
            vo.setType(r.getType());
            vo.setReason(r.getReason());
            vo.setStatus(r.getStatus());
            vo.setCreateTime(r.getCreate_time());

            Product product = productMapper.selectById(r.getProduct_id());
            if (product != null) vo.setProductName(product.getProduct_name());

            Merchant merchant = merchantMapper.selectById(r.getMerchant_id());
            if (merchant != null) vo.setMerchantName(merchant.getMerchant_name() != null ? merchant.getMerchant_name() : merchant.getUsername());

            Consumer consumer = consumerMapper.selectById(r.getConsumer_id());
            if (consumer != null) vo.setConsumerName(consumer.getConsumer_name());

            Orders order = ordersMapper.selectById(r.getOrder_id());
            if (order != null) {
                vo.setOrderAmount(order.getTotal_amount());
                vo.setUnitPrice(order.getUnit_price());
                vo.setQuantity(order.getQuantity());
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<OrderVO> getAvailableOrders(int consumerId) {
        List<OrderVO> candidates = ordersMapper.selectAvailableOrdersWithMerchant(consumerId);
        // 用 canApplyRefund 过滤（未评价、未申请）
        List<OrderVO> result = new ArrayList<>();
        for (OrderVO o : candidates) {
            if (canApplyRefund(consumerId, o.getId()) == null) {
                result.add(o);
            }
        }
        return result;
    }


    @Override
    public Refund getByOrderId(int orderId) {
        return refundMapper.selectByOrderId(orderId);
    }



    @Override
    public List<RefundListVO> listMerchantRefunds(int merchantId, Integer status) {
        QueryWrapper<Refund> wrapper = new QueryWrapper<>();

        //查看自己的售后订单(分状态的)
        wrapper.eq("merchant_id", merchantId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        List<Refund> refunds = refundMapper.selectList(wrapper);

        List<RefundListVO> voList = new ArrayList<>();
        for (Refund r : refunds) {
            RefundListVO vo = new RefundListVO();
            vo.setId(r.getId());
            vo.setOrderId(r.getOrder_id());
            vo.setConsumerId(r.getConsumer_id());
            vo.setProductId(r.getProduct_id());
            vo.setType(r.getType());
            vo.setReason(r.getReason());
            vo.setStatus(r.getStatus());
            vo.setCreateTime(r.getCreate_time());

            // 填充消费者昵称
            Consumer consumer = consumerMapper.selectById(r.getConsumer_id());
            vo.setConsumerName(consumer != null ? consumer.getConsumer_name() : "未知");

            // 填充商品名
            Product product = productMapper.selectById(r.getProduct_id());
            vo.setProductName(product != null ? product.getProduct_name() : "未知");

            // 填充商户名
            Merchant merchant = merchantMapper.selectById(r.getMerchant_id());
            if (merchant != null) vo.setMerchantName(merchant.getMerchant_name() != null ? merchant.getMerchant_name() : merchant.getUsername());

            // 填充订单金额、单价、数量
            Orders order = ordersMapper.selectById(r.getOrder_id());
            if (order != null) {
                vo.setOrderAmount(order.getTotal_amount());
                vo.setUnitPrice(order.getUnit_price());
                vo.setQuantity(order.getQuantity());
            }

            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional
    public R<String> auditRefund(int merchantId, int refundId, String action) {
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null || refund.getMerchant_id() != merchantId) {
            return R.error("退款单不存在或无权操作");
        }
        if (refund.getStatus() != 1) {
            return R.error("该申请已被处理，无法重复审核");
        }

        // 校验前端传来的 action
        if (!"agree".equals(action) && !"reject".equals(action)) {
            return R.error("无效操作，请传 agree 或 reject");
        }

        if ("agree".equals(action)) {
            refund.setStatus(2); // 已同意
            refundMapper.updateById(refund);

            if (refund.getType() == 1) {
                // 退货退款：退款给消费者 + 扣减商户营收
                Orders order = ordersMapper.selectById(refund.getOrder_id());
                if (order == null) {
                    return R.error("关联订单不存在");
                }
                BigDecimal amount = order.getTotal_amount();

                Consumer consumer = consumerMapper.selectById(refund.getConsumer_id());
                if (consumer != null) {
                    consumer.setAccount_balance(consumer.getAccount_balance().add(amount));
                    consumerMapper.updateById(consumer);
                }

                Merchant merchant = merchantMapper.selectById(merchantId);
                if (merchant != null) {
                    BigDecimal newRevenue = merchant.getRevenue().subtract(amount);
                    if (newRevenue.compareTo(BigDecimal.ZERO) < 0) {
                        newRevenue = BigDecimal.ZERO;
                    }
                    merchant.setRevenue(newRevenue);
                    merchant.setUpdate_time(null);
                    merchantMapper.updateById(merchant);
                }

                return R.success("已同意退货退款申请，钱款已退给消费者，请注意查收！");

            } else if (refund.getType() == 2) {
                // 换货：不涉及资金变动
                return R.success("已同意换货申请");
            }
        }
        else {
            //  拒绝情况
            refund.setStatus(3);
            refundMapper.updateById(refund);
            return R.success("已拒绝退换货申请");
        }


        return R.success("操作成功");
    }



}
