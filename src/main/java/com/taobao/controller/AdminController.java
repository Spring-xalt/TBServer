package com.taobao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.dto.AdminStats;
import com.taobao.dto.PageResult;
import com.taobao.entity.*;
import com.taobao.mapper.*;
import com.taobao.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private ConsumerMapper consumerMapper;
    @Autowired private MerchantMapper merchantMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrdersMapper ordersMapper;

    // ======== 老接口（admin-index.html 用，全量返回） ========
    @GetMapping("/consumers")
    public R<List<Consumer>> consumers() {
        return R.success(consumerMapper.selectList(new QueryWrapper<Consumer>().orderByDesc("create_time")));
    }

    @GetMapping("/merchants")
    public R<List<Merchant>> merchants() {
        return R.success(merchantMapper.selectList(new QueryWrapper<Merchant>().orderByDesc("create_time")));
    }

    @GetMapping("/orders")
    public R<List<Orders>> orders() {
        return R.success(ordersMapper.selectList(new QueryWrapper<Orders>().orderByDesc("create_time")));
    }

    @GetMapping("/today-total")
    public R<BigDecimal> todayTotal() {
        QueryWrapper<Orders> w = new QueryWrapper<>();
        w.eq("status", 3);
        w.apply("DATE(create_time) = CURDATE()");
        BigDecimal total = ordersMapper.selectList(w).stream()
                .map(Orders::getTotal_amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return R.success(total);
    }

    // ======== 新分页接口（admin-dashboard.html 用） ========
    @GetMapping("/products/paged")
    public R<PageResult<Product>> productsPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "time") String orderBy) {
        QueryWrapper<Product> w = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) w.eq("type", type);
        if ("price".equals(orderBy)) {
            w.orderByDesc("price");
        } else {
            w.orderByDesc("create_time");
        }
        return R.success(PageResult.of(productMapper.selectList(w), page, size));
    }

    @GetMapping("/consumers/paged")
    public R<PageResult<Consumer>> consumersPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) BigDecimal balanceMin,
            @RequestParam(required = false) BigDecimal balanceMax) {
        QueryWrapper<Consumer> w = new QueryWrapper<>();
        if (balanceMin != null) w.ge("account_balance", balanceMin);
        if (balanceMax != null) w.le("account_balance", balanceMax);
        w.orderByDesc("create_time");
        return R.success(PageResult.of(consumerMapper.selectList(w), page, size));
    }

    @GetMapping("/merchants/paged")
    public R<PageResult<Merchant>> merchantsPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size) {
        QueryWrapper<Merchant> w = new QueryWrapper<>();
        w.orderByDesc("create_time");
        return R.success(PageResult.of(merchantMapper.selectList(w), page, size));
    }

    @GetMapping("/orders/paged")
    public R<PageResult<Orders>> ordersPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String amountRange) {
        QueryWrapper<Orders> w = new QueryWrapper<>();
        if ("small".equals(amountRange)) w.le("total_amount", new BigDecimal("100"));
        else if ("large".equals(amountRange)) w.ge("total_amount", new BigDecimal("500"));
        else if ("mid".equals(amountRange)) w.between("total_amount", new BigDecimal("100"), new BigDecimal("500"));
        w.orderByDesc("create_time");
        return R.success(PageResult.of(ordersMapper.selectList(w), page, size));
    }

    @GetMapping("/stats")
    public R<AdminStats> stats() {
        AdminStats s = new AdminStats();
        s.setConsumerCount(consumerMapper.selectCount(null));
        s.setMerchantCount(merchantMapper.selectCount(null));
        s.setProductCount(productMapper.selectCount(null));
        s.setOrderCount(ordersMapper.selectCount(null));
        QueryWrapper<Orders> w = new QueryWrapper<>();
        w.eq("status", 3);
        w.apply("DATE(create_time) = CURDATE()");
        BigDecimal today = ordersMapper.selectList(w).stream()
                .map(Orders::getTotal_amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        s.setTodayTotal(today);
        return R.success(s);
    }

    @PutMapping("/consumer/{id}/toggle")
    public R<String> toggleConsumer(@PathVariable("id") Integer id) {
        Consumer c = consumerMapper.selectById(id);
        if (c == null) return R.error("消费者不存在");
        c.setStatus(c.getStatus() == null || c.getStatus() == 0 ? 1 : 0);
        c.setUpdate_time(null);
        consumerMapper.updateById(c);
        return R.success(c.getStatus() == 1 ? "已禁用" : "已解禁");
    }

    @PutMapping("/merchant/{id}/toggle")
    public R<String> toggleMerchant(@PathVariable("id") Integer id) {
        Merchant m = merchantMapper.selectById(id);
        if (m == null) return R.error("商户不存在");
        m.setStatus(m.getStatus() == null || m.getStatus() == 0 ? 1 : 0);
        m.setUpdate_time(null);
        merchantMapper.updateById(m);
        return R.success(m.getStatus() == 1 ? "已禁用" : "已解禁");
    }
}
