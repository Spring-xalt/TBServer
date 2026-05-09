package com.taobao.dto;



import lombok.Data;
import java.math.BigDecimal;
/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
// 用于支持在线更改购物车的临时购物车对象，若提交了，就把对象复制给cart持久化到数据库中
public class CartItem {
    private Integer productId;
    private Integer merchantId;
    // 商品名称
    private String productName;
    private BigDecimal price;
    private String merchantName;
    //数量
    private Integer quantity;
}
