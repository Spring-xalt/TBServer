package com.taobao.entity;

/*
 @auther:Jimi
 @description: 订单信息
 */

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Orders {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    // 商户ID（外键）
    private Integer merchant_id;
    // 消费者ID（外键）
    private Integer consumer_id;
    private String product_name;
    // 商品ID（外键）
    private Integer product_id;
    private BigDecimal total_amount;


    // 1:未支付 2:已支付 3:已签收
    private Integer status;
    // 支付后暂存金额 中间存储位置
    private BigDecimal temp_amount;
    // 确认签收时间
    private LocalDateTime receive_time;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime update_time;
}

