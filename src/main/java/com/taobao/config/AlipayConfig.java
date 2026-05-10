package com.taobao.config;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {
    // 支付宝沙箱APPID
    private String appId;
    // 私钥
    private String merchantPrivateKey;
    // 公钥
    private String alipayPublicKey;
    // 沙箱网关
    private String gatewayUrl;


    // natapp的用于内网穿透的异步通知地址
    private String notifyUrl;
    // 同步跳转地址
    private String returnUrl;
}
