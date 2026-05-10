package com.taobao.controller;

import com.taobao.common.R;
import com.taobao.dto.RechargeDto;
import com.taobao.entity.Consumer;
import com.taobao.service.ConsumerService;
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
@RequestMapping("/consumer")
public class ConsumerController {
    @Autowired
    private ConsumerService consumerService;

    @GetMapping("/all")
    public R<List<Consumer>> getAllConsumers() {
        List<Consumer> consumers = consumerService.getAllConsumers();
        return R.success("共查询到" + consumers.size() + "位消费者", consumers);
    }

    @PostMapping("/add")
    public R<String> addConsumer(@RequestBody  Consumer consumer) {
        if (consumerService.addConsumer(consumer)) {
            return R.success("消费者[" + consumer.getUsername() + "]新增成功");
        }
        return R.error("消费者新增失败,请重试!");
    }

    @GetMapping("/{id}")
    public R<Consumer> getConsumerById(@PathVariable int id) {
        Consumer consumer = consumerService.getConsumerById(id);
        if (consumer == null) {
            return R.error(404, "未找到ID为" + id + "的消费者");
        }
        return R.success("查询成功", consumer);
    }

    @GetMapping("/me")
    public R<Consumer> getCurrentConsumer(HttpSession session) {
        // 依赖登录时存的 session
        Integer id = (Integer) session.getAttribute("consumerId");
        if(id == null){
            return R.error("未登录");
        }
        Consumer consumer = consumerService.getConsumerById(id);
        return R.success("查询成功", consumer);
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deleteCustomer(@PathVariable Integer id) {
        Consumer consumer = consumerService.getConsumerById(id);
        if (consumer == null) {
            return R.error(404, "删除失败：未找到ID为" + id + "的消费者");
        }
        if (consumerService.deleteConsumer(id)) {
            return R.success("消费者[" + consumer.getUsername() + "]已成功删除");
        }
        return R.error("删除失败,有订单可能未处理!");
    }

    // 更新客户信息
    @PutMapping("/update")
    public R<String> updateCustomer(@RequestBody Consumer consumer) {
        if(consumer.getId()==null){
            throw new IllegalStateException("更新必须传入id信息");
        }
        boolean done=consumerService.updateConsumer(consumer);
        return done ? R.success("个人信息已更新") : R.error("更新失败");
    }


    //充值
    @PostMapping("/recharge")
    public R<String> recharge(@RequestBody RechargeDto rechargeDto, HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        return consumerService.recharge(consumerId, rechargeDto.getAmount());
    }

}
