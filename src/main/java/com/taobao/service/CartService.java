package com.taobao.service;

import com.taobao.common.R;
import com.taobao.dto.CartItem;

import java.util.List;

public interface CartService {
    // 登录时加载数据库中的购物车数据
    List<CartItem> loadCart(int consumerId);

    // 保存时用 Session 数据覆盖数据库
    R<String> saveCart(int consumerId, List<CartItem> items);
}
