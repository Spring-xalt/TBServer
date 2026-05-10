package com.taobao.dto;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

import com.taobao.entity.Review;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class ReviewVO extends Review {
    //既包含消费者端又包含商家端，双端备选
    private String productName;
    private String merchantName;
    private String consumerName;
}
