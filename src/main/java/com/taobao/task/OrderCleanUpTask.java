package com.taobao.task;

/*
 @auther:Jimi
 @description: 定时清理订单的任务
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.entity.Orders;
import com.taobao.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderCleanUpTask {
    @Autowired
    private OrdersMapper ordersMapper;

    @Scheduled(cron = "0 0 * * * ?") // 每天凌晨3点执行
    public void cleanSignedOrders() {
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        //对已经签收过的超过三个月的订单 批量清理
        wrapper.eq("status", 3)
                .lt("receive_time", LocalDateTime.now().minusMonths(3));
        ordersMapper.delete(wrapper);
        
    }
}

/*
    cron表达式:(星期 与 日 二选一)
       秒  分   时  日  月  星期
        0  *   *   *   *   ? → 每分钟执行一次(即每分钟的第0秒执行一次任务)

        0  0   *   *   *   ? → 每小时整点执行一次(即每小时的第0分执行一次任务)

        0  0   2   *   *   ? → 每天凌晨 2点执行一次(即每天的第2时执行一次)

        0  0   2   ?   *   MON → 每周一凌晨 2 点执行一次

        0  0   0   1   *   ? → 每月 1 号凌晨执行一次
 */