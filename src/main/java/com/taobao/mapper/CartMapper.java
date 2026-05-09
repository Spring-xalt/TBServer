package com.taobao.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.dto.CartVO;
import com.taobao.entity.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface CartMapper extends BaseMapper<Cart>{
    //  加入购物车（同一消费者重复加同一商品，数量+1而不是新增记录）
    @Insert("INSERT INTO cart (consumer_id, product_id, merchant_id, quantity) " +
            "VALUES (#{consumerId}, #{productId}, #{merchantId}, 1) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + 1")
    int insertOrAddQuantity(@Param("consumerId") int consumerId,
                            @Param("productId") int productId,
                            @Param("merchantId") int merchantId);

    // 2. 查看购物车列表（关联 product 和 merchant 表，一次查出商品名、价格、商户名）
    @Select("SELECT c.id, c.consumer_id, c.product_id, c.merchant_id, c.quantity, " +
            "p.product_name, p.price, m.merchant_name " +
            "FROM cart c " +
            "JOIN product p ON c.product_id = p.id " +
            "JOIN merchant m ON c.merchant_id = m.id " +
            "WHERE c.consumer_id = #{consumerId} " +
            "ORDER BY c.create_time DESC")
    List<CartVO> selectCartWithDetails(@Param("consumerId") int consumerId);

    // 3. 修改某条购物车项的数量（用 id 和 consumer_id 双重校验，防止越权）
    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND consumer_id = #{consumerId}")
    int updateQuantity(@Param("id") int id,
                       @Param("consumerId") int consumerId,
                       @Param("quantity") int quantity);

    // 4. 删除某条购物车项
    @Delete("DELETE FROM cart WHERE id = #{id} AND consumer_id = #{consumerId}")
    int deleteByIdAndConsumer(@Param("id") int id,
                              @Param("consumerId") int consumerId);

    // 5. 一键清空某消费者的购物车
    @Delete("DELETE FROM cart WHERE consumer_id = #{consumerId}")
    int clearByConsumer(@Param("consumerId") int consumerId);

    // 6. 统计某消费者购物车中商品种类数（用于顶部购物车角标）
    @Select("SELECT COUNT(*) FROM cart WHERE consumer_id = #{consumerId}")
    int countByConsumer(@Param("consumerId") int consumerId);
}

