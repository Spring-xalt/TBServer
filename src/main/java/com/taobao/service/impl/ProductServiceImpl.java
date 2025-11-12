package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taobao.entity.Product;
import com.taobao.mapper.ProductMapper;
import com.taobao.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 @auther:Jimi
 @description: 产品表
 */

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;


    @Override
    public boolean addProduct(Product product) {
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean updateProduct(Product product) {
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean deleteProduct(Integer id) {
        return productMapper.deleteById(id) > 0;
    }

    @Override
    public Product getProductById(Integer id) {
        return productMapper.selectById(id);
    }


    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectList(null);
    }
//    @Override
//    public IPage<Product> getProductsByPage(int page, int size) {
//        Page<Product> p = new Page<>(page, size);
//        return productMapper.selectPage(p, null);
//    }

    @Override
    public List<Product> getProductsByMerchantId(Integer merchantId) {
        //条件查询构造器
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        //如果数据库表merchant_id字段和传入的一致
        wrapper.eq("merchant_id", merchantId);
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> searchProducts(String msg) {
        System.out.println("进入 searchProducts, keyword=" + msg);

        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(Product::getProduct_name, msg)
                .or()
                .like(Product::getDescription, msg);

        return productMapper.selectList(queryWrapper);
    }


}
