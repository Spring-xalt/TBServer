package com.taobao.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.taobao.common.R;
import com.taobao.entity.Consumer;
import com.taobao.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.taobao.config.AlipayConfig;
import com.taobao.mapper.ConsumerMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Service
public class AlipayServiceImpl implements AlipayService {

    //沙箱和内网穿透（配置）
    @Autowired
    private AlipayConfig alipayConfig;

    // 用于充值到账
    @Autowired
    private ConsumerMapper consumerMapper;


    @Override
    public R<String> createPayQrCode(int consumerId, BigDecimal amount, String subject) {
        //  生成唯一订单号(recharge_ + 时间_ + 消费者id) 方便后续直接利用第二个_split解析(T113)
        String outTradeNo = "RECHARGE_" + System.currentTimeMillis() + "_" + consumerId;;

        // 创建沙箱版支付宝客户端(根据yml配置)
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getMerchantPrivateKey(),
                "json", "UTF-8",
                alipayConfig.getAlipayPublicKey(),
                "RSA2"
        );

        //预创建接口
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //支付成功后的通知返回地址，返回一个json
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + outTradeNo + "\"," +
                "\"total_amount\":\"" + amount.toString() + "\"," +
                "\"subject\":\"" + subject + "\"," +
                "\"timeout_express\":\"10m\"" +
                "}");

        try {
            //执行请求
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                // 收到请求并返回给前端一个 二维码链接
                return R.success(response.getQrCode());
            } else {
                return R.error("生成支付二维码失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return R.error("支付系统繁忙");
        }
    }

    @Override
    public String handleNotify(HttpServletRequest request) {
        //用于接收alipay发来的支付是否成功的异步消息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            //解析请求参数
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            //验证alipay公钥，确保未被篡改
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, alipayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");

            if (signVerified) {
                String tradeStatus = params.get("trade_status");
                String outTradeNo = params.get("out_trade_no");
                //充值额度
                String totalAmount = params.get("total_amount");

                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    // 充值到账：解析订单号中的消费者ID，累加余额
                    // 根据 outTradeNo 找到消费者并充值
                    System.out.println("充值记录" + outTradeNo + "充值成功！");

                    //充值完成后要对consumer余额++,通过订单号的订单号的后戳中解析consumerId
                    int consumerId = Integer.parseInt(outTradeNo.split("_")[2]);


                    BigDecimal amount = new BigDecimal(totalAmount);
                    Consumer consumer = consumerMapper.selectById(consumerId);
                    if (consumer != null) {
                        // 更新余额
                        consumer.setAccount_balance(consumer.getAccount_balance().add(amount));
                        consumerMapper.updateById(consumer);
                        System.out.println("消费者 " + consumerId + " 充值 " + amount + " 元到账成功");
                    } else {
                        System.out.println("警告：消费者 " + consumerId + " 不存在，订单号：" + outTradeNo);
                    }


                }
                return "success";
            } else {
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "failure";
        }
    }
}
