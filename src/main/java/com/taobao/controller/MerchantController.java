package com.taobao.controller;

/*
 @auther:Jimi
 @description:商户表controller层
 */

import com.taobao.common.R;
import com.taobao.dto.MerchantDto;
import com.taobao.entity.Merchant;
import com.taobao.entity.Product;
import com.taobao.service.MerchantService;
import com.taobao.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/merchant")
public class MerchantController {
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProductService productService;

    @GetMapping("/all")
    public R<List<Merchant>> getAllMerchants() {
        List<Merchant> merchants = merchantService.getAllMerchants();
        return R.success("共查询到" + merchants.size() + "家商户", merchants);
    }

    //从 URL 路径中获取参数 restful风格
    @GetMapping("/{id}")
    public R<Merchant> getMerchantById(@PathVariable Integer id) {
        Merchant merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return R.error("404, 未找到ID为" + id + "的商户");
        }
        return R.success("已查询到商户：" + merchant.getUsername(), merchant);
    }

    @GetMapping("/{id}/products")
    public R<List<Product>> getMerchantProducts(@PathVariable Integer id) {
        // 1. 先通过Service验证商户是否存在（复用已有方法）
        Merchant merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return R.error("商户不存在");
        }
        List<Product> products = productService.getProductsByMerchantId(id);
        String msg = products.isEmpty()
                ? "该商户暂无产品"
                : "共查询到" + products.size() + "个产品（商户：" + merchant.getMerchant_name()+ "）";
        return R.success(msg, products);
    }

    @GetMapping("/my-products")
    public R<List<Product>> myProducts(HttpSession session) {
        //从session中获取本人商铺信息
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录");
        }
        List<Product> products = productService.getProductsByMerchantId(merchantId);
        return R.success(products);
    }


    @PostMapping("/add")
    //json要求
    public R<String> addMerchant(@RequestBody  MerchantDto merchantDto) {
        if (merchantService.addMerchant(merchantDto)) {
            return R.success("商户[" + merchantDto.getMerchant_name() + "]创建成功，可继续添加产品");
        }
        return R.error("商户创建失败，请检查信息后重试");
    }

    @PutMapping("/update")
    public R<String> updateMerchant(@RequestBody MerchantDto merchantDto) {
        try {
            if (merchantService.updateMerchant(merchantDto)) {
                return R.success("商户[" + merchantDto.getMerchant_name() + "]信息更新成功");
            }
            return R.error(404, "更新失败：未找到该商户");
        } catch (IllegalStateException e) {
            return R.error("更新失败：" + e.getMessage());
        }
    }

    // 删除商户(其对应的产品也应当下架)
    @DeleteMapping("/delete/{id}")
    public R<String> deleteMerchant(@PathVariable Integer id) {
        Merchant merchant = merchantService.getMerchantById(id);
        if (merchant == null) {
            return R.error(404, "删除失败：未找到该商户");
        }
        if (merchantService.deleteMerchant(id)) {
            return R.success("商户[" + merchant.getUsername() + "]已成功删除，其下产品已同步移除");
        }
        return R.error("删除失败：系统异常，请稍后重试");
    }



}

    /*
        @PathVariable:遵循restful风格的资源表示形式（"/delete/{id}"）
        @RequestParam:适用于表单参数"search?name=测试&page=2"这样的(问号后)
        @RequestBody:适用去传输json数据的 post

     */
