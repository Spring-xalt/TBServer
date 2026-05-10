package com.taobao.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface CartMapper extends BaseMapper<Cart>{
    //  加载：查询某消费者在 cart 表中的所有记录（只查 cart 表，不关联其他表）
    @Select("SELECT * FROM cart WHERE consumer_id = #{consumerId}")
    List<Cart> selectByConsumerId(@Param("consumerId") int consumerId);

    //   清空：删除某消费者在 cart 表中的所有记录（保存前使用）
    @Delete("DELETE FROM cart WHERE consumer_id = #{consumerId}")
    int deleteByConsumerId(@Param("consumerId") int consumerId);

    //   插入：插入一条新的购物车记录（保存时逐条插入）（对于cart和cartItem存在不同的采用先删除后插入的方法实现）
    @Insert("INSERT INTO cart (consumer_id, product_id, merchant_id, quantity) " +
            "VALUES (#{consumerId}, #{productId}, #{merchantId}, #{quantity})")
    int insertOne(@Param("consumerId") int consumerId,
                  @Param("productId") int productId,
                  @Param("merchantId") int merchantId,
                  @Param("quantity") int quantity);

    //  统计：查询购物车中商品种类数（可用于角标，但实际多用 Session 计数）
    @Select("SELECT COUNT(*) FROM cart WHERE consumer_id = #{consumerId}")
    int countByConsumerId(@Param("consumerId") int consumerId);
}

