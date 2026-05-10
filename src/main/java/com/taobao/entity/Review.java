package com.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

@Data
public class Review {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer consumer_id;
    private Integer merchant_id;
    private Integer product_id;
    private Integer order_id;
    private Integer score;
    private String content;
    private LocalDateTime create_time;
}
