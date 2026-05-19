package com.taobao.controller;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

import com.taobao.common.R;
import com.taobao.dto.CartAddDto;
import com.taobao.dto.CartItem;
import com.taobao.dto.CartUpdateDto;
import com.taobao.entity.Merchant;
import com.taobao.entity.Product;
import com.taobao.mapper.CartMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.mapper.ProductMapper;
import com.taobao.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private CartMapper cartMapper;

    //根据登录校验拿到消费者id
    private Integer getConsumerId(HttpSession session) {
        return (Integer) session.getAttribute("consumerId");
    }

    @GetMapping("/list")
    public R<List<CartItem>> list(HttpSession session) {
        Integer consumerId = getConsumerId(session);
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<CartItem> cart = getCartFromSession(session);
        return R.success(cart);
    }


    // session层面的add，临时的cartItem层面处理
    @PostMapping("/add")
    public R<String> add(@RequestBody CartAddDto dto, HttpSession session) {
        int productId = dto.getProductId();

        Integer consumerId = getConsumerId(session);
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        // 查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return R.error("商品不存在");
        }
        // 查商户名称
        Merchant merchant = merchantMapper.selectById(product.getMerchant_id());
        String merchantName = (merchant != null) ? merchant.getMerchant_name() : "未知商户";

        // 获取 Session 中的购物车列表
        List<CartItem> cart = getCartFromSession(session);

        // 检查是否已存在，存在则数量+1
        for (CartItem item : cart) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + 1);
                return R.success("已加入购物车（数量+1）");
            }
        }

        // 不存在则新增
        CartItem newItem = new CartItem();

        newItem.setProductId(product.getId());
        newItem.setMerchantId(product.getMerchant_id());
        newItem.setProductName(product.getProduct_name());
        newItem.setPrice(product.getPrice());

        newItem.setQuantity(1);
        newItem.setMerchantName(merchantName);
        cart.add(newItem);

        return R.success("已加入购物车");
    }

    //session层面的更新
    @PutMapping("/update")
    public R<String> update(@RequestBody CartUpdateDto dto, HttpSession session) {
        Integer consumerId = getConsumerId(session);
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<CartItem> cart = getCartFromSession(session);
        int index = dto.getIndex();
        int quantity = dto.getQuantity();
        if (index < 0 || index >= cart.size()) {
            return R.error("无效的购物车项");
        }
        if (quantity < 1) {
            return R.error("数量至少为1");
        }
        cart.get(index).setQuantity(quantity);
        return R.success("数量已更新");
    }

    //session层面的删除
    @DeleteMapping("/delete/{index}")
    public R<String> delete(@PathVariable int index, HttpSession session) {
        Integer consumerId = getConsumerId(session);
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<CartItem> cart = getCartFromSession(session);
        if (index < 0 || index >= cart.size()) {
            return R.error("无效的购物车项");
        }
        cart.remove(index);
        return R.success("已删除");
    }

    // 清空购物车 ---数据库层面
    @DeleteMapping("/clear")
    public R<String> clear(HttpSession session) {


        Integer consumerId = getConsumerId(session);
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        //  删除数据库记录
        cartMapper.deleteByConsumerId(consumerId);
        // 清空 Session
        session.removeAttribute("cart");
        return R.success("购物车已清空");


    }



    //提交到数据库
    @PostMapping("/save")
    public R<String> save(HttpSession session) {
        Integer consumerId = getConsumerId(session);
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        List<CartItem> cart = getCartFromSession(session);
        return cartService.saveCart(consumerId, cart);
    }

    // 购物车角标数字 从加载的session中动态获取
    @GetMapping("/count")
    public R<Integer> count(HttpSession session) {
        Integer consumerId = getConsumerId(session);
        if (consumerId == null) {
            return R.success(0);
        }

        List<CartItem> cart = getCartFromSession(session);
        return R.success(cart.size());
    }

    // util方法:核心是为了 一些购物车之前无数据或者没有持久化的用户 用于new一个购物车list
    @SuppressWarnings("unchecked") //抑制警告
    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

}
