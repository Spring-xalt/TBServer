package com.taobao.dto;

import lombok.Data;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
//保持独立类 不继承实体
@Data
public class SubmitReviewDto {
    private Integer orderId;
    private Integer score;
    private String content;
}
