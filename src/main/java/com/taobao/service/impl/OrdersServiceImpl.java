package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.entity.Orders;
import com.taobao.mapper.ConsumerMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.mapper.OrdersMapper;
import com.taobao.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        return ordersMapper.deleteById(id) == 0;
    }

    //支付接口必须保证事务支持 (数据库新建订单时默认status为 1 )
    @Transactional
    @Override
    public R<String> payOrder(Integer id) {
        QueryWrapper<Orders> oqw = new QueryWrapper<>();
        oqw.eq("id", id);
        Orders order = ordersMapper.selectOne(oqw);
        Integer consumerId=order.getConsumer_id();

        BigDecimal money=order.getTotal_amount();

        QueryWrapper<Consumer> cqw = new QueryWrapper<>();
        cqw.eq("id", consumerId);
        Consumer consumer = consumerMapper.selectOne(cqw);

        //钱不够
        if(consumer.getAccount_balance().compareTo(money)<0){
            return R.error("余额不足，支付失败!");
        }
        //钱够 转换orders的status状态 把钱暂存到temp_amount中
        consumer.setAccount_balance(consumer.getAccount_balance().subtract(money));
        consumerMapper.updateById(consumer);

        order.setStatus(2);
        order.setTemp_amount(money);
        ordersMapper.updateById(order);

        return R.success("支付成功!");
    }

    //签收接口 改变status为3 同时把数据库表中的temp_amount数据减掉 去到 merchant 的账户中
    @Transactional
    @Override
    public R<String> confirm(Integer id) {
        Orders order = ordersMapper.selectById(id);

        if(order==null){
            return R.error("订单不存在");
        }
        if(order.getStatus()!=2){
            return R.error("只有已支付的订单才能签收!");
        }

        //要转移的钱
        BigDecimal money=order.getTemp_amount();
        //签收后商户的金额++
        Merchant merchant = merchantMapper.selectById(order.getMerchant_id());
        merchant.setRevenue(merchant.getRevenue().add(money));
        merchantMapper.updateById(merchant);


        order.setStatus(3);
        order.setTemp_amount(BigDecimal.valueOf(0.00));
        ordersMapper.updateById(order);

        return R.success("签收成功!");

    }
}
