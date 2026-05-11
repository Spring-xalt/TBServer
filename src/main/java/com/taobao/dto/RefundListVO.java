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
public class RefundListVO {
    private Integer id;
    private Integer orderId;
    private Integer consumerId;
    // 消费者昵称（关联 consumer 表）
    private String consumerName;
    private Integer productId;
    // 商品名（关联 product 表）
    private String productName;
    // 1=退货退款，2=换货
    private Integer type;
    private String reason;
    // 1=待审核，2=已同意，3=已拒绝，4=已完成
    private Integer status;
    //若涉及退款的退款金额（从订单表拿）
    private BigDecimal orderAmount;
    private LocalDateTime createTime;
}
