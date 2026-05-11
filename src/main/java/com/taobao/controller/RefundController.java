package com.taobao.controller;

import com.taobao.common.R;
import com.taobao.dto.RefundApplyDto;
import com.taobao.dto.RefundListVO;
import com.taobao.entity.Orders;
import com.taobao.entity.Refund;
import com.taobao.service.RefundService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public R<List<Orders>> availableOrders(HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<Orders> list = refundService.getAvailableOrders(consumerId);
        return R.success(list);

    }
    // 展示消费者退换货申请
    @GetMapping("/consumer/list")
    public R<List<Refund>> consumerRefundList(HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<Refund> list = refundService.listByConsumerId(consumerId);
        return R.success(list);
    }



    // 查询某个订单的退换货申请状态（返回完整 Refund 对象，供消费者或商户使用）
    @GetMapping("/status")
    public R<Refund> status(@RequestParam int orderId) {
        Refund refund = refundService.getByOrderId(orderId);
        // 未申请时 refund 为 null，直接返回
        return R.success(refund);
    }



    // 查询商户的售后订单(商家端售后管理)
    @GetMapping("/merchant/list")
    public R<List<RefundListVO>> merchantList(
            @RequestParam(required = false) Integer status,
            HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        List<RefundListVO> list = refundService.listMerchantRefunds(merchantId, status);
        return R.success(list);
    }
    // 商户处理售后订单
    @PutMapping("/audit")
    public R<String> audit(
            @RequestParam int refundId,
            @RequestParam String action,
            HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        return refundService.auditRefund(merchantId, refundId, action);
    }




}
