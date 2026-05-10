package com.taobao.service;

import com.taobao.common.R;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

public interface AlipayService {

    // 生成并返回二维码链接
    R<String> createPayQrCode(int consumerId, BigDecimal amount, String subject);

    // 处理支付宝异步通知（通知者模式）
    String handleNotify(HttpServletRequest request);
}
