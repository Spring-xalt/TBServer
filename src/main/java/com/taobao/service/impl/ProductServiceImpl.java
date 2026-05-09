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

    @Override
    public IPage<Product> getProductsByPage(int page, int size) {
        //计算偏移
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectByPage(offset, size);
        long total = productMapper.selectCount(null);
        //偏移后将所有记录写入构造的Page对象
        Page<Product> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

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

        //search核心是利用sql条件构造器利用通配符实现的 like通配符绑定name和descrription
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(Product::getProduct_name, msg)
                .or()
                .like(Product::getDescription, msg);

        return productMapper.selectList(queryWrapper);
    }


}
