package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {
    // sql原生分页功能 等同于分页查询（每一页取八条数据） SELECT * FROM product LIMIT 0, 8;
    @Select("SELECT * FROM product LIMIT #{offset}, #{size}")
    List<Product> selectByPage(@Param("offset") int offset, @Param("size") int size);
}
