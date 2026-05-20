package com.taobao.entity;

/*
 @auther: Jimi
 @description: 商品实体类
 */

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Lombok 注解：自动生成 getter/setter/toString
@Data
public class Product {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    //外键
    private Integer merchant_id;

    private String product_name;

    private BigDecimal price;
    //库存量
    private Integer stock;

    private String description;

    private String image;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime update_time;
}