package com.taobao.controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.taobao.common.R;
import com.taobao.config.AlipayConfig;
import com.taobao.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private AlipayConfig alipayConfig;

    //前端弹出充值弹窗，用户输入金额后调用此接口
    @PostMapping("/recharge")
    public R<String> recharge(@RequestParam BigDecimal amount, HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return R.error("充值金额必须大于0");
        }
        // 生成二维码
        return alipayService.createPayQrCode(consumerId, amount, "账户充值");
    }

    //支付成功后支付宝会主动调用此地址，告诉后端支付结果，此接口必须能被外网访问（通过 NATApp 穿透）

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        return alipayService.handleNotify(request);
    }


    @GetMapping("/status")
    public R<String> checkPayStatus(@RequestParam String outTradeNo) {
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getMerchantPrivateKey(),
                    "json", "UTF-8",
                    alipayConfig.getAlipayPublicKey(),
                    "RSA2"
            );

            // 交易查询请求
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"}");

            // 执行查询
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            // 判断是否支付成功
            if (response.isSuccess() && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
                return R.success("SUCCESS");
            } else {
                return R.success("WAITING");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return R.error("查询失败");
        }
    }

}
