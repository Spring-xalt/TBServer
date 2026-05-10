package com.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    // 退款退货(1)还是仅退款(0)
    private Integer type;
    // 退款原因
    private String reason;
    //状态(0待审核，1已同意，2已拒绝，3已完成)
    private Integer status;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
