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



    /*
    MP的自动填充机制存在一个问题：如果为空是可以填充当下时间，
    但是只做改动不会触发时间覆盖机制，所以每次做修改后都应该reset为null，
    然后触发自动填充
    */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime update_time;

}