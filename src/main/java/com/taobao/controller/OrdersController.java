package com.taobao.controller;

import com.taobao.common.R;
import com.taobao.dto.CartItem;
import com.taobao.dto.OrderVO;
import com.taobao.entity.Orders;
import com.taobao.service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 @auther:Jimi
 @description: 订单表控制层
 */
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    
    @GetMapping("/all")
    public R<List<Orders>> getAllOrders() {
        List<Orders> orders = ordersService.getAllOrders();
        return R.success("共查询到" + orders.size() + "条订单", orders);
    }

    @GetMapping("/{id}")
    public R<Orders> getOrderById(@PathVariable Integer id) {
        Orders order = ordersService.getOrderById(id);
        if (order == null) {
            return R.error(404, "未找到ID为" + id + "的订单");
        }
        return R.success("查询订单成功", order);
    }

    @GetMapping("/consumer/{consumer_id}")
    public List<Orders> getOrdersByConsumer(@PathVariable Integer consumer_id) {
        return ordersService.getOrdersByConsumerId(consumer_id);
    }

    @GetMapping("/merchant/{merchant_id}")
    public List<Orders> getOrdersByMerchant(@PathVariable Integer merchant_id) {
        return ordersService.getOrdersByMerchantId(merchant_id);
    }


    @PostMapping("/add")
    public R<String> addOrder(@RequestBody Orders orders)  {
        if (ordersService.addOrder(orders)) {
            return R.success("id为[" + orders.getId() + "]的订单创建成功");
        }
        return R.error("订单创建失败，请重试");
    }

    @PutMapping("/update")
    public boolean updateOrder(@RequestBody Orders orders) {
        //更新操作必须在未签收之前  即1或者2
        if(orders.getId()==null){
            throw new IllegalStateException("更新必须传入id");
        }
        return ordersService.updateOrder(orders);
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deleteOrder(@PathVariable Integer id) {
        Orders order = ordersService.getOrderById(id);
        if (order == null) {
            return R.error(404, "未找到ID为" + id + "的订单，删除失败");
        }
        if (ordersService.deleteOrder(id)) {
            return R.success("ID为[" + order.getId() + "]的订单已成功删除");
        }
        return R.error("订单删除失败，请重试");
    }

    @GetMapping("/consumerSelf")
    public R<List<OrderVO>> consumerOrders(HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<OrderVO> list = ordersService.listMyOrders(consumerId);
        return R.success(list);
    }


    @PostMapping("/createAndPay")
    public R<String> createAndPay(@RequestParam String password, HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }

        // 获取 Session 购物车数据
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return R.error("购物车为空");
        }

        // 生成未支付订单
        List<Orders> newOrders = ordersService.createOrdersFromCart(consumerId, cart);

        // 执行支付
        R<String> payResult = ordersService.payOrders(consumerId, newOrders, password);

        // 根据支付结果处理 Session 购物车
        if (payResult.getCode() == 200) {
            // 支付成功，清空 Session 购物车
            session.removeAttribute("cart");
        }

        // 支付失败时，购物车保持原样，订单已经存在（未支付），不用额外操作
        return payResult;
    }


    // 待修改
    @PutMapping("/{orderId}/confirm")
    public R<String> confirmReciveOrder(@PathVariable Integer orderId) {
        return ordersService.confirm(orderId);
    }
}
