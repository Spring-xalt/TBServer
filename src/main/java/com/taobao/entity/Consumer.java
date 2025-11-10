package com.taobao.entity;

/*
 @auther: Jimi
 @description: 消费者
 */

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Consumer {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    private String username;
    private String password;

    private String consumer_name;
    //账户余额
    private BigDecimal account_balance;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime update_time;
}