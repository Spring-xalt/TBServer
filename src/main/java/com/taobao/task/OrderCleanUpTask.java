package com.taobao.task;

/*
 @auther:Jimi
 @description: 定时清理订单任务(未支付的订单只存留1min，已签收超3个月的自动删除)
 */

import com.taobao.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderCleanUpTask {
    @Autowired
    private OrdersMapper ordersMapper;


    // 清理未支付订单：每30秒执行一次
    @Scheduled(fixedRate = 30000)
    public void cleanUnpaidOrders() {
        int deleted = ordersMapper.deleteExpiredUnpaidOrders();
        if (deleted > 0) {
            System.out.println("定时清理：已删除 " + deleted + " 条未支付过期订单（1分钟）");
        }
    }


    // 清理已签收订单：每天凌晨2点执行一次
    @Scheduled(cron = "0 0 16 * * ?")
    public void cleanReceivedOrders() {
        int received = ordersMapper.deleteExpiredReceivedOrders();
        if (received > 0) {
            System.out.println("定时清理：已删除 " + received + " 条已签收过期订单（3个月）");
        }
    }

    /*
    0 0 16 * * ?	每天下午 4 点 0 分 0 秒执行
    0 30 9 * * ?	每天早上 9 点 30 分 0 秒执行
    0 0/5 * * * ?	每 5 分钟执行一次
    0 0 2 1 * ?	    每月 1 号凌晨 2 点执行

     */

}



