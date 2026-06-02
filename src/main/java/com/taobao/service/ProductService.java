package com.taobao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    boolean addProduct(Product product);

    boolean deleteProduct(Integer id);

    Product getProductById(Integer id);

    List<Product> getAllProducts();

    //分页
    IPage<Product> getProductsByPage(int page, int size);

    List<Product> getProductsByMerchantId(Integer merchantId);

    List<Product> searchProducts(String msg);


    boolean updateProductByMerchant(Product product, Integer merchantId);

    boolean updateProductByAdmin(Product product);


    boolean deleteProductByMerchant(Integer productId, Integer merchantId);


    List<Product> searchByMerchant(int merchantId, String keyword);


    IPage<Product> getProductsByMerchantIdAndPage(int merchantId, int page, int size);

    // 按分类、价格区间、排序分页筛选
    IPage<Product> getProductsByFilterAndPage(String type, BigDecimal minPrice, BigDecimal maxPrice,
                                               String sort, int page, int size);

    String getProductImage(Integer id);

}
