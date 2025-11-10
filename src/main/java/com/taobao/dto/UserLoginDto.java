package com.taobao.dto;

import lombok.Data;

/*
 @auther:Jimi
 @description: 登录的dto设计
 */
@Data
public class UserLoginDto {
    private String username;
    private String password;
}
