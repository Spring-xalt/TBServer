package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Orders;
import com.taobao.entity.Review;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@SuppressWarnings("all")
public interface ReviewMapper extends BaseMapper<Review> {

    // 插入评价
    @Insert("INSERT INTO review (consumer_id, merchant_id, product_id, order_id, score, content) " +
            "VALUES (#{consumerId}, #{merchantId}, #{productId}, #{orderId}, #{score}, #{content})")
    int insertReview(@Param("consumerId") int consumerId,
                     @Param("merchantId") int merchantId,
                     @Param("productId") int productId,
                     @Param("orderId") int orderId,
                     @Param("score") int score,
                     @Param("content") String content);


    // 判断有没有评价过 （从orderid查）
    @Select("SELECT * FROM review WHERE order_id = #{orderId}")
    Review selectByOrderId(@Param("orderId") int orderId);

    // 根据消费者ID查所有评价（个人中心）
    @Select("SELECT * FROM review WHERE consumer_id = #{consumerId} ORDER BY create_time DESC")
    List<Review> selectByConsumerId(@Param("consumerId") int consumerId);

    // 查询商户的评价
    @Select("SELECT * FROM review WHERE merchant_id = #{merchantId} ORDER BY create_time DESC")
    List<Review> selectByMerchantId(@Param("merchantId") int merchantId);

    // 查询已评价订单
    @Select("SELECT order_id FROM review WHERE consumer_id = #{consumerId}")
    List<Integer> selectReviewedOrderIds(@Param("consumerId") int consumerId);

    // 按商品ID查评价
    @Select("SELECT * FROM review WHERE product_id = #{productId} ORDER BY create_time DESC")
    List<Review> selectByProductId(@Param("productId") int productId);

    // 分页查消费者评价
    @Select("SELECT * FROM review WHERE consumer_id = #{consumerId} ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Review> selectByConsumerIdPage(@Param("consumerId") int consumerId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM review WHERE consumer_id = #{consumerId}")
    long countByConsumerId(@Param("consumerId") int consumerId);

}
