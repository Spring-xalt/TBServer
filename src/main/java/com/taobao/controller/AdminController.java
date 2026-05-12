package com.taobao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.dto.AdminLoginDto;
import com.taobao.entity.Admin;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.entity.Orders;
import com.taobao.mapper.ConsumerMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.mapper.OrdersMapper;
import com.taobao.mapper.ProductMapper;
import com.taobao.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrdersMapper ordersMapper;




    @GetMapping("/consumers")
    public R<List<Consumer>> consumers() {
        return R.success(consumerMapper.selectList(null));
    }


    @GetMapping("/merchants")
    public R<List<Merchant>> merchants() {
        return R.success(merchantMapper.selectList(null));
    }

    @GetMapping("/orders")
    public R<List<Orders>> orders() {
        return R.success(ordersMapper.selectList(null));
    }

    @GetMapping("/today-total")
    public R<BigDecimal> todayTotal() {
        // 查今天签收的订单总金额
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 3);
        wrapper.apply("DATE(create_time) = CURDATE()");
        List<Orders> list = ordersMapper.selectList(wrapper);
        BigDecimal total = list.stream()
                .map(Orders::getTotal_amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return R.success(total);
    }
}
