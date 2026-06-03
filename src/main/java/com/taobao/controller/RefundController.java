package com.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.dto.OrderVO;
import com.taobao.dto.RefundApplyDto;
import com.taobao.dto.RefundListVO;
import com.taobao.entity.Orders;
import com.taobao.entity.Refund;
import com.taobao.service.RefundService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

@RestController
@RequestMapping("/refund")
public class RefundController {

    @Autowired
    private RefundService refundService;

    // 消费者提交退换货申请
    @PostMapping("/apply")
    public R<String> apply(@RequestBody RefundApplyDto dto, HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        return refundService.applyRefund(
                consumerId,
                dto.getOrderId(),
                dto.getType(),
                dto.getReason()
        );
    }

    // 展示可提交退换货申请的订单(消费者端)
    @GetMapping("/consumer/available-orders")
    public R<Map<String, Object>> availableOrders(
            HttpSession session,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) return R.error(401, "请先登录");
        IPage<OrderVO> result = refundService.getAvailableOrders(consumerId, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("orders", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pages", result.getPages());
        return R.success(data);
    }

    @GetMapping("/consumer/list")
    public R<Map<String, Object>> consumerRefundList(
            HttpSession session,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) return R.error(401, "请先登录");
        IPage<RefundListVO> result = refundService.listConsumerRefundsDetail(consumerId, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("refunds", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pages", result.getPages());
        return R.success(data);
    }



    // 查询某个订单的退换货申请状态（返回完整 Refund 对象，供消费者或商户使用）
    @GetMapping("/status")
    public R<Refund> status(@RequestParam("orderId") int orderId) {
        Refund refund = refundService.getByOrderId(orderId);
        // 未申请时 refund 为 null，直接返回
        return R.success(refund);
    }



    // 查询商户的售后订单(商家端售后管理)
    @GetMapping("/merchant/list")
    public R<Map<String, Object>> merchantList(
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) return R.error(401, "请先登录商家账号");
        IPage<RefundListVO> result = refundService.listMerchantRefunds(merchantId, status, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("refunds", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pages", result.getPages());
        return R.success(data);
    }
    // 商户处理售后订单
    @PutMapping("/audit")
    public R<String> audit(
            @RequestParam("refundId") int refundId,
            @RequestParam("action") String action,
            HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        return refundService.auditRefund(merchantId, refundId, action);
    }




}
