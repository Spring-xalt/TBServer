package com.taobao.service;

import com.taobao.entity.Product;

import java.util.List;

public interface ProductService {
    boolean addProduct(Product product);

    boolean updateProduct(Product product);

    boolean deleteProduct(Integer id);

    Product getProductById(Integer id);

    List<Product> getAllProducts();

    List<Product> getProductsByMerchantId(Integer merchantId);

    List<Product> searchProducts(String msg);
}
