package com.taobao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.dto.RefundListVO;
import com.taobao.entity.Product;
import com.taobao.service.ProductService;
import com.taobao.service.RefundService;
import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private RefundService refundService;


    @GetMapping("/all")
    public R<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size) {

        //让如果要修改单页商品数 此处defaultValue和前端的pageSize都要修改
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


    @PostMapping("/add")
    public R<String> addProductForMerchant(@RequestBody Product product, HttpSession session) {
        // 校验商户身份（Controller 层负责权限）
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }

        //  强制绑定商品到当前商户（防止前端篡改）
        product.setMerchant_id(merchantId);

        boolean success = productService.addProduct(product);
        return success
                ? R.success("商品[" + product.getProduct_name() + "]上架成功")
                : R.error("商品新增失败，请重试");
    }


    @PutMapping("/update")
    public R<String> updateProduct(@RequestBody Product product, HttpSession session) {
        if (product.getId() == null) {
            return R.error("更新必须传入商品ID");
        }
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        boolean success = productService.updateProductByMerchant(product, merchantId);
        return success ? R.success("更新成功") : R.error("更新失败，商品不存在或无权修改");
    }



    // 储备管理员更新的接口
    @PutMapping("/adminUpdate")
    public R<String> adminUpdateProduct(@RequestBody Product product, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return R.error(403, "无管理员权限");
        }
        if (product.getId() == null) {
            return R.error("更新必须传入商品ID");
        }
        boolean success = productService.updateProductByAdmin(product);
        return success ? R.success("管理员更新成功") : R.error("更新失败，商品不存在");
    }

    @PostMapping("/adminAdd")
    public R<String> addProduct(@RequestBody Product product,HttpSession session){
        if (!"admin".equals(session.getAttribute("role"))) {
            return R.error(403, "无管理员权限");
        }
        boolean isSuccess = productService.addProduct(product);
        if (isSuccess) {
            return R.success("商品[" + product.getProduct_name() + "]新增成功");
        } else {
            return R.error("商品新增失败，请重试");
        }
    }

    @DeleteMapping("/adminDelete/{id}")
    public R<String> deleteProductForAdmin(@PathVariable Integer id,HttpSession session){
        if (!"admin".equals(session.getAttribute("role"))) {
            return R.error(403, "无管理员权限");
        }
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


    @DeleteMapping("/delete/{id}")
    public R<String> deleteProduct(@PathVariable Integer id, HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        // 安全删除
        boolean success = productService.deleteProductByMerchant(id, merchantId);
        return success ? R.success("商品已成功下架") : R.error("删除失败，商品不存在或无权操作");
    }


    // 获取某商户的所有商品
    @GetMapping("/{merchantId}/products")
    public R<List<Product>> getProductsByMerchantId(@PathVariable Integer merchantId) {
        List<Product> products = productService.getProductsByMerchantId(merchantId);
        return R.success("共查询到该商户的" + products.size() + "件商品", products);
    }






}
