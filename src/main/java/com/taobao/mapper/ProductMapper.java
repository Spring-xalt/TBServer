package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {
    // sql原生分页功能(从所有的product中选 ) 等同于分页查询（每一页取八条数据） SELECT * FROM product LIMIT 0, 8;
    @Select("SELECT * FROM product LIMIT #{offset}, #{size}")
    List<Product> selectByPage(@Param("offset") int offset, @Param("size") int size);


    // 限定某个商户的分页
    @Select("SELECT * FROM product WHERE merchant_id = #{merchantId} LIMIT #{offset}, #{size}")
    List<Product> selectByMerchantIdAndPage(@Param("merchantId") int merchantId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    @Select("SELECT COUNT(*) FROM product WHERE merchant_id = #{merchantId}")
    long selectCountByMerchantId(@Param("merchantId") int merchantId);

    @Select("SELECT image FROM product WHERE id = #{id}")
    String selectImageById(@Param("id") Integer id);

}
