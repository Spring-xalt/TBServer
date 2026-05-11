package com.taobao.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

@Data
public class OrderVO {
    private Integer id;
    private Integer merchant_id;
    private Integer consumer_id;
    private String product_name;
    private String consumer_name;
    private BigDecimal total_amount;

    // 1未支付 2已支付 3已签收
    private Integer status;
    private BigDecimal temp_amount;
    private LocalDateTime receive_time;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

    // 商户名称
    private String merchant_name;
}
