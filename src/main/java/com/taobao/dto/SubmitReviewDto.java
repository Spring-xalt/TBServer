package com.taobao.dto;

import lombok.Data;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Data
public class SubmitReviewDto {
    private Integer orderId;
    private Integer score;
    private String content;
}
