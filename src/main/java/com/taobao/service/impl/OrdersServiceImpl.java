package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.dto.CartItem;
import com.taobao.dto.OrderVO;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.entity.Orders;
import com.taobao.mapper.CartMapper;
import com.taobao.mapper.ConsumerMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.mapper.OrdersMapper;
import com.taobao.service.OrdersService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 @auther:Jimi
 @description: 订单表实现
 */
@Service
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private CartMapper cartMapper;

    @Override
    public List<Orders> getAllOrders() {
        return ordersMapper.selectList(null);
    }

    @Override
    public Orders getOrderById(Integer id) {
        return ordersMapper.selectById(id);
    }

    @Override
    public List<Orders> getOrdersByConsumerId(Integer consumerId) {
        return ordersMapper.selectOrdersByConsumerId(consumerId);
    }

    @Override
    public List<Orders> getOrdersByMerchantId(Integer merchantId) {
        return ordersMapper.selectOrdersByMerchantId(merchantId);
    }

    @Override
    public boolean addOrder(Orders order) {
        return ordersMapper.insert(order) > 0;
    }

    @Override
    public boolean updateOrder(Orders order) {
        return ordersMapper.updateById(order) > 0;
    }

    @Override
    public boolean deleteOrder(Integer id) {
        return ordersMapper.deleteById(id) >0;
    }


    @Override
    public List<OrderVO> listMyOrders(int consumerId) {
        return ordersMapper.selectOrdersWithMerchant(consumerId);
    }


    @Override
    @Transactional
    public List<Orders> createOrdersFromCart(int consumerId, List<CartItem> items) {
        // 根据购物车生成新订单
        List<Orders> newOrders = new ArrayList<>();
        for (CartItem item : items) {
            Orders order = new Orders();
            order.setConsumer_id(consumerId);
            order.setMerchant_id(item.getMerchantId());
            order.setProduct_name(item.getProductName());
            order.setTotal_amount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            // 先生成未支付订单
            order.setStatus(1);

            //持久化
            ordersMapper.insert(order);
            // 回填自增 id
            newOrders.add(order);
        }
        return newOrders;
    }

    @Override
    @Transactional
    public R<String> payOrders(int consumerId, List<Orders> orders, String password) {
        // 校验密码
        Consumer consumer = consumerMapper.selectById(consumerId);

        String md5Input = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!consumer.getPassword().equals(md5Input)) {
            return R.error("密码错误");
        }

        // 计算本次支付总金额
        BigDecimal totalToPay = orders.stream()
                .map(Orders::getTotal_amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //  余额检查
        if (consumer.getAccount_balance().compareTo(totalToPay) < 0) {
            return R.error("余额不足，还差 " + totalToPay.subtract(consumer.getAccount_balance()) + " 元");
        }

        // 扣款
        consumer.setAccount_balance(consumer.getAccount_balance().subtract(totalToPay));
        consumerMapper.updateById(consumer);

        // 更新订单状态为已支付，记录暂存金额
        for (Orders order : orders) {
            order.setStatus(2);
            // 支付之后金额暂存在order 的 temp_amount，之后reset订单
            order.setTemp_amount(order.getTotal_amount());
            ordersMapper.updateById(order);
        }

        // 清空购物车数据库记录
        cartMapper.deleteByConsumerId(consumerId);

        return R.success("支付成功，共 " + totalToPay + " 元");
    }





    //签收接口 改变status为3 同时把数据库表中的temp_amount数据减掉 去到 merchant 的账户中
    @Transactional
    @Override
    public R<String> confirm(Integer id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            return R.error("订单不存在");
        }
        if (order.getStatus() != 2) {
            return R.error("只有已支付的订单才能签收!");
        }

        //要转移的钱
        BigDecimal money = order.getTemp_amount();

        //签收后商户的金额++
        Merchant merchant = merchantMapper.selectById(order.getMerchant_id());

        // reset为空，触发自动填充
        merchant.setUpdate_time(null);
        order.setUpdate_time(null);

        merchant.setRevenue(merchant.getRevenue().add(money));
        merchantMapper.updateById(merchant);

        order.setStatus(3);
        order.setTemp_amount(BigDecimal.valueOf(0.00));
        //set签收时间
        order.setReceive_time(LocalDateTime.now());
        ordersMapper.updateById(order);

        return R.success("签收成功!");
    }



    @Override
    public List<OrderVO> listMerchantOrders(int merchantId) {
        List<Orders> orders = ordersMapper.selectOrdersByMerchantId(merchantId);
        List<OrderVO> voList = new ArrayList<>();

        for (Orders o : orders) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(o, vo);

            // 填充消费者昵称
            Consumer consumer = consumerMapper.selectById(o.getConsumer_id());
            vo.setConsumer_name(consumer != null ? consumer.getConsumer_name() : "未知");


            Merchant merchant = merchantMapper.selectById(o.getMerchant_id());
            vo.setMerchant_name(merchant != null ? merchant.getMerchant_name() : "未知");

            voList.add(vo);
        }
        return voList;
    }


}
