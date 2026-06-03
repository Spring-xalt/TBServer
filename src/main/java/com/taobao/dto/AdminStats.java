package com.taobao.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminStats {
    private long consumerCount;
    private long merchantCount;
    private long productCount;
    private long orderCount;
    private BigDecimal todayTotal;
}
