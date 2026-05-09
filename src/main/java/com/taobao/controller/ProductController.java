package com.taobao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.entity.Product;
import com.taobao.service.ProductService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
  @auther: Jimi
  @description: 产品表实现crud+获得对应商户ID
 */
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/all")
    public R<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size) {

        IPage<Product> result = productService.getProductsByPage(page, size);

        Map<String, Object> data = new HashMap<>();

        data.put("products", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pages", result.getPages());

        return R.success("共查询到" + result.getTotal() + "件商品", data);
    }

    @GetMapping("/{id}")
    public R<Product> getProductById(@PathVariable Integer id){
        Product product = productService.getProductById(id);
        if (product == null) {
            return R.error(404, "未找到ID为" + id + "的商品");
        }
        return R.success("查询商品成功", product);
    }

    @GetMapping("/search")
    public R<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return R.success("搜索到" + products.size() + "件商品", products);
    }



    @PutMapping("/update")
    public R<String> updateProduct(@RequestBody Product product){
        if (product.getId() == null) {
            return R.error(400, "更新操作必须传入商品ID");
        }
        if(productService.updateProduct(product)){
            return R.success("商品[" + product.getProduct_name() + "]更新成功");
        }
        return R.error(404, "更新失败");
    }

    @PostMapping("/add")
    public R<String> addProduct(@RequestBody Product product){
        boolean isSuccess = productService.addProduct(product);
        if (isSuccess) {
            return R.success("商品[" + product.getProduct_name() + "]新增成功");
        } else {
            return R.error("商品新增失败，请重试");
        }
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deleteProduct(@PathVariable Integer id){
        Product product = productService.getProductById(id);
        if (product == null) {
            return R.error(404, "未找到ID为" + id + "的商品，删除失败");
        }
        boolean isSuccess = productService.deleteProduct(id);
        if (isSuccess) {
            return R.success("商品[" + product.getProduct_name() + "]已成功删除");
        } else {
            return R.error("商品删除失败，请重试");
        }
    }


    // 获取某商户的所有商品
    @GetMapping("/{merchantId}/products")
    public R<List<Product>> getProductsByMerchantId(@PathVariable Integer merchantId) {
        List<Product> products = productService.getProductsByMerchantId(merchantId);
        return R.success("共查询到该商户的" + products.size() + "件商品", products);
    }
}
