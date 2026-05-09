package com.taobao.service.impl;

import com.taobao.common.R;
import com.taobao.dto.CartItem;
import com.taobao.entity.Cart;
import com.taobao.entity.Merchant;
import com.taobao.entity.Product;
import com.taobao.mapper.CartMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.mapper.ProductMapper;
import com.taobao.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/*
*@auther:Jimi
*@version:1.0
*@description:
*/

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private MerchantMapper merchantMapper;

    //用户二次登录需要回溯购物车信息

    @Override
    public List<CartItem> loadCart(int consumerId) {
        //此处不选择直接传入session是职责分离的考虑 ，由调用者（控制层来拿session进而取id）
        List<Cart> cartList = cartMapper.selectByConsumerId(consumerId);
        List<CartItem> items = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProduct_id());
            if (product == null) {
                continue; // 商品已下架，跳过
            }
            Merchant merchant = merchantMapper.selectById(cart.getMerchant_id());
            CartItem item = new CartItem();

            item.setProductId(product.getId());
            item.setMerchantId(product.getMerchant_id());
            item.setProductName(product.getProduct_name());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setMerchantName(merchant != null ? merchant.getMerchant_name() : "未知商户");
            items.add(item);
        }
        return items;
    }

    @Transactional
    @Override
    public R<String> saveCart(int consumerId, List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return R.error("购物车为空，无需保存");
        }
        // 先清空该消费者的所有旧记录
        cartMapper.deleteByConsumerId(consumerId);
        // 逐条插入新的
        for (CartItem item : items) {
            cartMapper.insertOne(consumerId, item.getProductId(), item.getMerchantId(), item.getQuantity());
        }
        return R.success("购物车已保存");
    }
}
