package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {
    // sql原生分页功能(从所有的product中选 ) 等同于分页查询（每一页取八条数据） SELECT * FROM product LIMIT 0, 8;
    @Select("SELECT * FROM product ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> selectByPage(@Param("offset") int offset, @Param("size") int size);


    // 限定某个商户的分页
    @Select("SELECT * FROM product WHERE merchant_id = #{merchantId} ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> selectByMerchantIdAndPage(@Param("merchantId") int merchantId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    @Select("SELECT COUNT(*) FROM product WHERE merchant_id = #{merchantId}")
    long selectCountByMerchantId(@Param("merchantId") int merchantId);

    @Select("SELECT image FROM product WHERE id = #{id}")
    String selectImageById(@Param("id") Integer id);

    // 新品上市：最近7天上架的商品（分页）
    @Select("SELECT * FROM product WHERE create_time > DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> selectNewArrivals(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM product WHERE create_time > DATE_SUB(NOW(), INTERVAL 7 DAY)")
    long selectNewArrivalsCount();

    // 特价促销：价格0-50的商品（分页）
    @Select("SELECT * FROM product WHERE price >= 0 AND price <= 50 ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Product> selectSpecialOffers(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM product WHERE price >= 0 AND price <= 50")
    long selectSpecialOffersCount();

    // 热销推荐：近30天订单量最高的商品（分页）
    @Select("SELECT p.* FROM product p " +
            "INNER JOIN ( " +
            "  SELECT product_id, COUNT(*) AS order_count " +
            "  FROM orders " +
            "  WHERE create_time > DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
            "  GROUP BY product_id " +
            "  ORDER BY order_count DESC " +
            ") hot ON p.id = hot.product_id " +
            "ORDER BY hot.order_count DESC " +
            "LIMIT #{offset}, #{size}")
    List<Product> selectHotSales(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(DISTINCT product_id) FROM orders WHERE create_time > DATE_SUB(NOW(), INTERVAL 1 MONTH)")
    long selectHotSalesCount();

}
