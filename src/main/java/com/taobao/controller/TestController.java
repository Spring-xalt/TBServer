package com.taobao.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/1")
    public String test() {
        return "项目启动成功!";
    }
}
