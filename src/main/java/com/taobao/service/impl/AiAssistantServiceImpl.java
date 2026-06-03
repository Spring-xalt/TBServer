package com.taobao.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.config.AiAssistantConfig;
import com.taobao.entity.Product;
import com.taobao.mapper.ProductMapper;
import com.taobao.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    @Autowired
    private AiAssistantConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProductMapper productMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private static final String BASE_SYSTEM_PROMPT =
        "你是「仿淘宝商城」的智能购物助手，名叫小淘。\n" +
        "商城分类：数码电子、服装鞋帽、生活用品、学习办公、食品饮料、美妆个护、运动户外\n" +
        "首页轮播：新品上市（最近7天）、热销推荐（近30天销量排行）、特价促销（0-50元）\n" +
        "要求：用中文回复，语气亲切活泼，每次2-4句话。回答时尽量引用下方的真实商品数据。\n";

    // 推荐问题池（不与首页 banner 重复）
    private static final List<String> SUGGESTION_POOL = Arrays.asList(
        "学生党预算有限，哪些东西性价比高？",
        "我想买礼物送朋友，有什么推荐吗？",
        "买了不喜欢可以退吗？怎么退？",
        "有没有适合办公室用的好东西？",
        "怎么判断一个商品靠不靠谱？",
        "下单后多久能发货？",
        "支持哪些支付方式？",
        "最近有什么值得关注的商品？",
        "同样的东西为什么价格差这么多？",
        "买东西能砍价或者用优惠券吗？",
        "退款一般几天到账？",
        "收到的商品有质量问题怎么办？",
        "还没发货可以取消订单吗？",
        "怎么查看物流信息？",
        "100元左右有什么好东西？",
        "有没有适合送父母的礼物？",
        "食品类会不会过期？保质期怎么看？",
        "怎么联系卖家咨询？",
        "第一次购物有什么注意事项？",
        "数码产品有保修吗？"
    );

    @Override
    public String chat(String userMessage) {
        try {
            // 1. 查数据库拿真实商品
            List<Product> relevantProducts = queryRelevantProducts(userMessage);
            String dbContext = buildProductContext(relevantProducts);

            // 2. 拼 system prompt
            String fullSystemPrompt = BASE_SYSTEM_PROMPT + dbContext;

            // 3. 调 DeepSeek
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            requestBody.put("stream", false);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", fullSystemPrompt);
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 600);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, config.defaultHeaders());
            ResponseEntity<String> httpResponse = restTemplate.postForEntity(config.getChatUrl(), entity, String.class);

            JsonNode root = objectMapper.readTree(httpResponse.getBody());
            return root.path("choices").get(0).path("message").path("content")
                    .asText("抱歉，我没能理解你的问题，请换个问法试试。").trim();

        } catch (Exception e) {
            return "小淘正在走神，请稍后再试～";
        }
    }

    @Override
    public List<String> getSuggestions() {
        // 从池子里随机取 4 条不重复的
        List<String> pool = new ArrayList<>(SUGGESTION_POOL);
        Collections.shuffle(pool, random);
        return pool.subList(0, Math.min(4, pool.size()));
    }

    // ========== 数据库查询 ==========

    private List<Product> queryRelevantProducts(String message) {
        String lower = message.toLowerCase();

        boolean askingAboutProducts = lower.contains("推荐") || lower.contains("买")
            || lower.contains("商品") || lower.contains("产品") || lower.contains("有什么")
            || lower.contains("有没有") || lower.contains("哪些") || lower.contains("看看")
            || lower.contains("介绍") || lower.contains("好物") || lower.contains("值得")
            || containsCategoryKeyword(lower);

        if (!askingAboutProducts) {
            return Collections.emptyList();
        }

        if (lower.contains("新") || lower.contains("上新") || lower.contains("最近")) {
            return productMapper.selectNewArrivals(0, 10);
        }
        if (lower.contains("热销") || lower.contains("热门") || lower.contains("卖得好")
                || lower.contains("大家都在") || lower.contains("销量")) {
            return productMapper.selectHotSales(0, 10);
        }
        if (lower.contains("便宜") || lower.contains("特价") || lower.contains("优惠")
                || lower.contains("低价") || lower.contains("实惠") || lower.contains("省钱")
                || (lower.contains("50") && lower.contains("元")) || (lower.contains("100") && lower.contains("元"))) {
            return productMapper.selectSpecialOffers(0, 10);
        }

        String category = extractCategory(lower);
        if (category != null) {
            return queryByCategory(category);
        }

        return productMapper.selectNewArrivals(0, 8);
    }

    private boolean containsCategoryKeyword(String msg) {
        return extractCategory(msg) != null;
    }

    private String extractCategory(String msg) {
        String[][] mappings = {
            {"数码电子", "数码", "手机", "电脑", "耳机", "平板", "充电"},
            {"服装鞋帽", "服装", "衣服", "鞋", "穿", "裙子", "裤子", "T恤"},
            {"生活用品", "日用", "家居", "生活"},
            {"学习办公", "学习", "办公", "文具", "书", "笔"},
            {"食品饮料", "食品", "饮料", "吃", "喝", "零食", "水果"},
            {"美妆个护", "美妆", "护肤", "化妆", "口红", "面膜"},
            {"运动户外", "运动", "户外", "健身", "跑步", "球"},
        };
        for (String[] group : mappings) {
            for (int i = 1; i < group.length; i++) {
                if (msg.contains(group[i])) return group[0];
            }
            if (msg.contains(group[0])) return group[0];
        }
        return null;
    }

    private List<Product> queryByCategory(String type) {
        List<Product> all = productMapper.selectNewArrivals(0, 50);
        return all.stream()
                .filter(p -> type.equals(p.getType()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private String buildProductContext(List<Product> products) {
        if (products.isEmpty()) {
            return "\n【当前数据库状态】暂无匹配商品。请根据你的知识给用户建议。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n【以下是数据库中真实的商品数据，请据此回答】\n");

        for (int i = 0; i < Math.min(products.size(), 15); i++) {
            Product p = products.get(i);
            sb.append(i + 1).append(". [").append(p.getType()).append("] ")
              .append(p.getProduct_name()).append(" — ¥").append(String.format("%.2f", p.getPrice()))
              .append("（库存:").append(p.getStock()).append("件）");
            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                sb.append(" ").append(p.getDescription().length() > 40
                    ? p.getDescription().substring(0, 40) + "…"
                    : p.getDescription());
            }
            sb.append("\n");
        }

        long total = productMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
        sb.append("\n商城共 ").append(total).append(" 件商品在售。请基于以上数据回答。");
        return sb.toString();
    }
}
