package com.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.taobao.common.R;
import com.taobao.entity.Product;
import com.taobao.service.ProductService;
import com.taobao.service.impl.ProductServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/all")
    public R<Map<String, Object>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "8") int size) {
        IPage<Product> result = productService.getProductsByPage(page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("products", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pages", result.getPages());
        return R.success("共查询到" + result.getTotal() + "件商品", data);
    }

    @GetMapping("/{id}")
    public R<Product> getProductById(@PathVariable Integer id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return R.error(404, "未找到ID为" + id + "的商品");
        }
        return R.success("查询商品成功", product);
    }


    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer id) {
        String imagePath = productService.getProductImage(id);
        if (imagePath == null || imagePath.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "图片不存在");
        }
        try {
            byte[] bytes = ((ProductServiceImpl) productService).getImageFile(imagePath);
            MediaType contentType = detectImageType(bytes);
            return ResponseEntity.ok().contentType(contentType).body(bytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "图片文件读取失败");
        }
    }


    private MediaType detectImageType(byte[] bytes) {
        if (bytes.length < 4) {
            return MediaType.IMAGE_JPEG;
        }
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return MediaType.IMAGE_JPEG;
        }
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }
        if (bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x38) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;
    }

    @GetMapping("/search")
    public R<List<Product>> searchProducts(@RequestParam("keyword") String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return R.success("搜索到" + products.size() + "件商品", products);
    }

    @PostMapping("/add")
    public R<String> addProductForMerchant(
            @RequestPart("product") Product product,
            @RequestPart("image") MultipartFile image,
            HttpSession session) {
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        if (image.isEmpty()) {
            return R.error("商品图片不能为空");
        }
try {
            String imagePath = ((ProductServiceImpl) productService).saveImage(image.getBytes(), image.getOriginalFilename());
            product.setImage(imagePath);
        } catch (IOException e) {
            return R.error("图片保存失败");
        }
        product.setMerchant_id(merchantId);
        boolean success = productService.addProduct(product);
        return success
                ? R.success("商品[" + product.getProduct_name() + "]上架成功")
                : R.error("商品新增失败，请重试");
    }

    @PutMapping("/update")
    public R<String> updateProduct(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpSession session) {
        if (product.getId() == null) {
            return R.error("更新必须传入商品ID");
        }
        Integer merchantId = (Integer) session.getAttribute("merchantId");
        if (merchantId == null) {
            return R.error(401, "请先登录商家账号");
        }
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > 2 * 1024 * 1024) {
                return R.error("图片大小不能超过2MB");
            }
            try {
                String oldImage = productService.getProductImage(product.getId());
                String newPath = ((ProductServiceImpl) productService).saveImage(image.getBytes(), image.getOriginalFilename());
                product.setImage(newPath);
                ((ProductServiceImpl) productService).deleteImageFile(oldImage);
            } catch (IOException e) {
                return R.error("图片保存失败");
            }
        }
        boolean success = productService.updateProductByMerchant(product, merchantId);
        return success ? R.success("更新成功") : R.error("更新失败，商品不存在或无权修改");
    }

    @PutMapping("/adminUpdate")
    public R<String> adminUpdateProduct(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return R.error(403, "无管理员权限");
        }
        if (product.getId() == null) {
            return R.error("更新必须传入商品ID");
        }
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > 2 * 1024 * 1024) {
                return R.error("图片大小不能超过2MB");
            }
            try {
                String oldImage = productService.getProductImage(product.getId());
                String newPath = ((ProductServiceImpl) productService).saveImage(image.getBytes(), image.getOriginalFilename());
                product.setImage(newPath);
                ((ProductServiceImpl) productService).deleteImageFile(oldImage);
            } catch (IOException e) {
                return R.error("图片保存失败");
            }
        }
        boolean success = productService.updateProductByAdmin(product);
        return success ? R.success("管理员更新成功") : R.error("更新失败，商品不存在");
    }

    @PostMapping("/adminAdd")
    public R<String> addProduct(
            @RequestPart("product") Product product,
            @RequestPart("image") MultipartFile image,
            HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return R.error(403, "无管理员权限");
        }
        if (image.isEmpty()) {
            return R.error("商品图片不能为空");
        }
try {
            String imagePath = ((ProductServiceImpl) productService).saveImage(image.getBytes(), image.getOriginalFilename());
            product.setImage(imagePath);
        } catch (IOException e) {
            return R.error("图片保存失败");
        }
        boolean isSuccess = productService.addProduct(product);
        if (isSuccess) {
            return R.success("商品[" + product.getProduct_name() + "]新增成功");
        } else {
            return R.error("商品新增失败，请重试");
        }
    }

    @DeleteMapping("/adminDelete/{id}")
    public R<String> deleteProductForAdmin(@PathVariable Integer id, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return R.error(403, "无管理员权限");
        }
        Product product = productService.getProductById(id);
        if (product == null) {
            return R.error(404, "未找到ID为" + id + "的商品，删除失败");
        }
        ((ProductServiceImpl) productService).deleteImageFile(product.getImage());
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
        Product product = productService.getProductById(id);
        if (product == null || !product.getMerchant_id().equals(merchantId)) {
            return R.error("删除失败，商品不存在或无权操作");
        }
        ((ProductServiceImpl) productService).deleteImageFile(product.getImage());
        boolean success = productService.deleteProductByMerchant(id, merchantId);
        return success ? R.success("商品已成功下架") : R.error("删除失败，商品不存在或无权操作");
    }

    @GetMapping("/{merchantId}/products")
    public R<List<Product>> getProductsByMerchantId(@PathVariable Integer merchantId) {
        List<Product> products = productService.getProductsByMerchantId(merchantId);
        return R.success("共查询到该商户的" + products.size() + "件商品", products);
    }
}
