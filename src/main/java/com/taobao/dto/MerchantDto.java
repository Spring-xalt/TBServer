package com.taobao.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.taobao.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/*
  @auther:Jimi
  @description: 商户表的 dto
 */
@Data
public class MerchantDto {
    // update 时需要
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String merchant_name;
    private BigDecimal revenue;
    private String username;
    private String password;

    // 关联产品信息
    private List<Product> products;
}
    