package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taobao.common.AuditLogger;
import com.taobao.entity.Product;
import com.taobao.mapper.ProductMapper;
import com.taobao.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final String PRODUCT_IMAGE_SUB = "products";

    @Override
    public boolean addProduct(Product product) {
        AuditLogger.log("商品上架 | id={} | name={} | price=¥{}", product.getId(), product.getProduct_name(), product.getPrice());
        log.info("商品上架 | {} | ¥{}", product.getProduct_name(), product.getPrice());
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
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectByPage(offset, size);
        long total = productMapper.selectCount(null);
        Page<Product> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<Product> getProductsByMerchantId(Integer merchantId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> searchProducts(String msg) {
        System.out.println("进入 searchProducts, keyword=" + msg);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(Product::getProduct_name, msg)
                .or()
                .like(Product::getDescription, msg);
        queryWrapper.orderByDesc("create_time");
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public List<Product> searchByMerchant(int merchantId, String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId)
                .and(w -> w.like("product_name", keyword)
                        .or()
                        .like("description", keyword));
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public boolean updateProductByMerchant(Product product, Integer merchantId) {
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            return false;
        }
        if (!existing.getMerchant_id().equals(merchantId)) {
            return false;
        }
        existing.setProduct_name(product.getProduct_name());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setDescription(product.getDescription());
        existing.setType(product.getType());
        if (product.getImage() != null) {
            existing.setImage(product.getImage());
        }
        return productMapper.updateById(existing) > 0;
    }

    @Override
    public boolean updateProductByAdmin(Product product) {
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            return false;
        }
        existing.setProduct_name(product.getProduct_name());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setDescription(product.getDescription());
        existing.setType(product.getType());
        if (product.getImage() != null) {
            existing.setImage(product.getImage());
        }
        return productMapper.updateById(existing) > 0;
    }

    @Override
    public boolean deleteProductByMerchant(Integer productId, Integer merchantId) {
        Product existing = productMapper.selectById(productId);
        if (existing == null || !existing.getMerchant_id().equals(merchantId)) {
            return false;
        }
        return productMapper.deleteById(productId) > 0;
    }

    @Override
    public IPage<Product> getProductsByFilterAndPage(String type, BigDecimal minPrice,
                                                      BigDecimal maxPrice, String sort,
                                                      int page, int size) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (minPrice != null) {
            wrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("price", maxPrice);
        }
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc("price");
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc("price");
        } else {
            wrapper.orderByDesc("create_time");
        }
        // 先查全量拿总数
        List<Product> all = productMapper.selectList(wrapper);
        long total = all.size();
        // 再手动截取当前页
        int offset = (page - 1) * size;
        int to = Math.min(offset + size, all.size());
        List<Product> records;
        if (offset < all.size()) {
            records = all.subList(offset, to);
        } else {
            records = new java.util.ArrayList<>();
        }

        Page<Product> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<Product> getProductsByMerchantIdAndPage(int merchantId, int page, int size) {
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectByMerchantIdAndPage(merchantId, offset, size);
        long total = productMapper.selectCountByMerchantId(merchantId);
        Page<Product> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public String getProductImage(Integer id) {
        return productMapper.selectImageById(id);
    }

    @Override
    public IPage<Product> getNewArrivals(int page, int size) {
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectNewArrivals(offset, size);
        long total = productMapper.selectNewArrivalsCount();
        Page<Product> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<Product> getHotSales(int page, int size) {
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectHotSales(offset, size);
        long total = productMapper.selectHotSalesCount();
        Page<Product> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    @Override
    public IPage<Product> getSpecialOffers(int page, int size) {
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectSpecialOffers(offset, size);
        long total = productMapper.selectSpecialOffersCount();
        Page<Product> result = new Page<>(page, size, total);
        result.setRecords(records);
        return result;
    }

    public String saveImage(byte[] bytes, String originalFilename) throws IOException {
        Path dir = Paths.get(uploadDir, PRODUCT_IMAGE_SUB);
        Files.createDirectories(dir);
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path filePath = dir.resolve(filename);
        Files.write(filePath, bytes);
        return PRODUCT_IMAGE_SUB + "/" + filename;
    }

    public void deleteImageFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return;
        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("删除旧图片失败: " + relativePath);
        }
    }

    public byte[] getImageFile(String relativePath) throws IOException {
        Path filePath = Paths.get(uploadDir, relativePath);
        return Files.readAllBytes(filePath);
    }
}
