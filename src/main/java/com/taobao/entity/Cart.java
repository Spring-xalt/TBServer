package com.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

//购物车实体类

@Data
public class Cart {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer consumer_id;
    private Integer product_id;
    private Integer merchant_id;
    private Integer quantity;
    private LocalDateTime create_time;
}