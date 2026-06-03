package com.taobao.entity;

/*
 @auther: Jimi
 @description: 商户类
 */

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Merchant {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    private String username;
    private String password;

    private String merchant_name;
    // 营收
    private BigDecimal revenue;



    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime update_time;

    // 账号状态(0=正常, 1=禁用)
    private Integer status;}