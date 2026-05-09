package com.taobao.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartVO {
    private Integer id;          // cart 表 id
    private Integer consumer_id;
    private Integer product_id;
    private Integer merchant_id;
    private Integer quantity;

    // 根据产品名找
    private String product_name;
    // 需要根据价格
    private BigDecimal price;

    // 需要根据商家信息找订单
    private String merchant_name;
}