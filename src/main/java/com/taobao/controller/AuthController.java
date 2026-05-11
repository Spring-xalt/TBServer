package com.taobao.controller;

import com.taobao.common.R;
import com.taobao.dto.MerchantDto;
import com.taobao.dto.UserLoginDto;
import com.taobao.dto.UserRegisterDto;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.service.ConsumerService;
import com.taobao.service.MerchantService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ConsumerService consumerService;

    // 游客登录
    @PostMapping("/guest")
    public R<String> guestLogin(HttpSession session) {
        //游客登陆后要先删除之前的session内容
        session.removeAttribute("consumerId");
        session.removeAttribute("role");
        session.removeAttribute("cart");   // 加上这一行

        session.setAttribute("role", "guest");
        return R.success("游客登录成功");
    }
    @GetMapping("/status")
    public R<Map<String, Object>> status(HttpSession session) {
        Map<String,Object> data = new HashMap<>();
        Object role = session.getAttribute("role");
        if (role == null) {
            data.put("loggedIn", false);
            data.put("role", "guest");
        } else {
            data.put("loggedIn", !"guest".equals(role));
            data.put("role", role.toString());
        }
        return R.success("状态获取成功", data);
    }

    @PostMapping("/merchant/register")
    public R<String> registerMerchant(@RequestBody UserRegisterDto registerDto) {
        log.info("收到前端商户注册请求!");
        return merchantService.register(registerDto);
    }



    @PostMapping("/merchant/login")
    public R<Merchant> loginMerchant(@RequestBody UserLoginDto loginDto,HttpSession session) {
        return merchantService.login(loginDto,session);
    }


    //消费者和商户的登出可以合并 销毁session即可
    @PostMapping("/logout")
    public R<String> logout(HttpSession session) {
        session.invalidate();
        return R.success("已退出登录");
    }
//    @PostMapping("/merchant/logout")
//    public R<String> logoutMerchant(HttpSession session) {
//        session.invalidate();
//        return R.success("退出成功!");
//    }


    @PostMapping("/consumer/register")
    public R<String> registerConsumer(@RequestBody UserRegisterDto registerDto) {
        log.info("收到前端消费者注册请求!");
        return consumerService.register(registerDto);
    }


    @PostMapping("/consumer/login")
    public R<Consumer> loginConsumer(@RequestBody UserLoginDto loginDto,HttpSession session) {
        return consumerService.login(loginDto,session);
    }

//    @PostMapping("/consumer/logout")
//    public R<String> logout(HttpSession session) {
//        session.invalidate();
//        return R.success("退出成功!");
//    }


}
