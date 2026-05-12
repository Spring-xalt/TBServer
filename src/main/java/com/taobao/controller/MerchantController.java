package com.taobao.controller;

/*
 @auther:Jimi
 @description:商户表controller层
 */

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.dto.MerchantDto;
import com.taobao.entity.Merchant;
import com.taobao.entity.Product;
import com.taobao.service.MerchantService;
import com.taobao.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @GetMapping("/my-products")
    public R<Map<String, Object>> myProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            HttpSession session) {

        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录");
        }

        IPage<Product> result = productService.getProductsByMerchantIdAndPage(merchantId, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("products", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pages", result.getPages());

        return R.success("共查询到" + result.getTotal() + "件商品", data);
    }


    @PostMapping("/add")
    //json要求的body体提交方式
    public R<String> addMerchant(@RequestBody  MerchantDto merchantDto) {
        if (merchantService.addMerchant(merchantDto)) {
            return R.success("商户[" + merchantDto.getMerchant_name() + "]创建成功，可继续添加产品");
        }
        return R.error("商户创建失败，请检查信息后重试");
    }


    // 查询当前商户信息
    @GetMapping("/myInfo")
    public R<Merchant> myInfo(HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            return R.error("商户信息不存在");
        }
        return R.success(merchant);
    }


    //管理员用于商家管理的
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


    //商户个人中心用于修改个人信息(商家个人修改信息只能修改店铺名，故选择url传参最方便)
    @PutMapping("/updateInfo")
    public R<String> updateInfo(@RequestParam String merchantName, HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");

        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }

        if (merchantName == null || merchantName.trim().isEmpty()) {
            return R.error("店铺名称不能为空");
        }
        boolean success = merchantService.updateMerchantName(merchantId, merchantName.trim());
        return success ? R.success("店铺名称已更新") : R.error("更新失败");
    }



    @GetMapping("/my-products/search")
    public R<List<Product>> searchMyProducts(@RequestParam String keyword, HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录");
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词为空，直接返回全部商品
            return R.success(productService.getProductsByMerchantId(merchantId));
        }
        List<Product> result = productService.searchByMerchant(merchantId, keyword.trim());
        return R.success(result);
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
