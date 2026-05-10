package com.taobao.dto;

import lombok.Data;

import java.math.BigDecimal;

/*
*@auther:Jimi
*@version:1.0
*@description:
*/

@Data
public class RechargeDto {
    //充值金额
    private BigDecimal amount;
}
