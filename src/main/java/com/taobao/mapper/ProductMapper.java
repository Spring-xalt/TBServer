package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Product;

public interface ProductMapper extends BaseMapper<Product> {
    // 继承basemapper后自动获得基础crud

}
