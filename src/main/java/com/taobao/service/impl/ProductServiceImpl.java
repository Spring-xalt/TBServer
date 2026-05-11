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

    //某个商户的搜索功能(商户id+关键词)
    @Override
    public List<Product> searchByMerchant(int merchantId, String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        //本商户 同时匹配商品名和描述
        wrapper.eq("merchant_id", merchantId)
                .and(w -> w.like("product_name", keyword)
                        .or()
                        .like("description", keyword));
        return productMapper.selectList(wrapper);

    }


    @Override
    public boolean updateProductByMerchant(Product product, Integer merchantId) {
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            return false;
        }
        // 必须是自家的商品(虽然前端展示的是自己家的 双重校验保证一下)
        if (!existing.getMerchant_id().equals(merchantId)) {
            return false;
        }
        // 只覆盖允许修改的字段
        existing.setProduct_name(product.getProduct_name());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setDescription(product.getDescription());
        return productMapper.updateById(existing) > 0;
    }




    @Override
    public boolean updateProductByAdmin(Product product) {
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            return false;
        }
        // 管理员可以修改所有字段（不校验 merchant）
        existing.setProduct_name(product.getProduct_name());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setDescription(product.getDescription());
        return productMapper.updateById(existing) > 0;
    }


    @Override
    public boolean deleteProductByMerchant(Integer productId, Integer merchantId) {
        //拿到商品
        Product existing = productMapper.selectById(productId);
        if (existing == null || !existing.getMerchant_id().equals(merchantId)) {
            return false;
        }
        return productMapper.deleteById(productId) > 0;
    }




}
