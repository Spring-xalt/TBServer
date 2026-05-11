package com.taobao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.entity.Product;

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
    

    List<Product> searchByMerchant(int merchantId, String keyword);

}
