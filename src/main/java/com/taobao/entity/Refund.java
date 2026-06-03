package com.taobao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/*
 *@auther:Jimi
 *@version:1.0
 *@description: 退款实体类
 */

@Data
public class Refund {


    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer order_id;
    private Integer consumer_id;
    private Integer merchant_id;
    private Integer product_id;
    // 退货退款(1)还是换货(2)
    private Integer type;
    // 退款原因
    private String reason;
    //状态(1待审核，2已同意，3已拒绝)
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime update_time;
}
