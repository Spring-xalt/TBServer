package com.taobao.dto;

import lombok.Data;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Data
public class RefundApplyDto {

    private Integer orderId;
    // 1=退货退款, 2=换货
    private Integer type;
    private String reason;
}
