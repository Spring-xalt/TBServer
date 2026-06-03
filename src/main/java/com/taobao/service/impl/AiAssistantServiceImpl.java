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
        "首页轮播：新品上市（最近7天）、热销推荐（近30天销量排行）、特价促销（0-50元）\n\n" +
        "⚠️ 重要规则，必须遵守：\n" +
        "1. 你只能回答与商品推荐、价格比较、分类浏览相关的问题。\n" +
        "2. 关于退换货、退款、物流时效、支付方式、保修、优惠券等平台政策，你没有任何内部信息。\n" +
        "   遇到这类问题，统一回复：建议查看平台帮助中心或联系具体卖家确认，不要编造任何政策。\n" +
        "3. 下方提供的商品数据是你唯一的信息来源，不要引用数据中没有的商品或价格。\n" +
        "4. 语气亲切活泼，每次2-4句话。\n";

    // 推荐问题池：只问 AI 能答的（商品相关），不问政策类的
    private static final List<String> SUGGESTION_POOL = Arrays.asList(
        "学生党预算有限，哪些东西性价比高？",
        "有没有适合送朋友的礼物推荐？",
        "有什么适合在办公室用的好东西？",
        "最近有什么值得关注的上新？",
        "同样的东西为什么价格差这么多？",
        "100元左右有什么好东西推荐？",
        "有没有适合送长辈的礼物？",
        "哪个分类的商品最多？帮我看看",
        "怎么快速找到我想要的东西？",
        "库存紧张的商品有哪些？",
        "有没有好看又实用的生活用品？",
        "想买点零食，有什么推荐的？",
        "数码产品哪款性价比最高？",
        "运动户外有什么装备推荐？",
        "美妆类的商品多吗？有什么好的？",
        "怎么看一个商品值不值得买？",
        "学习用品区有什么好东西？",
        "首页轮播的三个入口分别是什么？",
        "想买衣服，哪个分类里找？",
        "怎么快速搜到我想要的东西？"
    );

    @Override
    public String chat(String userMessage) {
        try {
            List<Product> relevantProducts = queryRelevantProducts(userMessage);
            String dbContext = buildProductContext(relevantProducts, userMessage);

            String fullSystemPrompt = BASE_SYSTEM_PROMPT + dbContext;

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
        List<String> pool = new ArrayList<>(SUGGESTION_POOL);
        Collections.shuffle(pool, random);
        return pool.subList(0, Math.min(2, pool.size()));
    }

    // ========== 数据库查询 ==========

    private List<Product> queryRelevantProducts(String message) {
        String lower = message.toLowerCase();

        boolean askingAboutProducts = lower.contains("推荐") || lower.contains("买")
            || lower.contains("商品") || lower.contains("产品") || lower.contains("有什么")
            || lower.contains("有没有") || lower.contains("哪些") || lower.contains("看看")
            || lower.contains("介绍") || lower.contains("好物") || lower.contains("值得")
            || lower.contains("库存") || lower.contains("分类") || lower.contains("哪个")
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
        if (lower.contains("库存") || lower.contains("紧张") || lower.contains("快没")) {
            List<Product> all = productMapper.selectNewArrivals(0, 50);
            return all.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .sorted(Comparator.comparingInt(Product::getStock))
                .limit(10)
                .collect(Collectors.toList());
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
            {"服装鞋帽", "服装", "衣服", "鞋", "穿", "裙子", "裤子", "T恤", "帽"},
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

    private String buildProductContext(List<Product> products, String userMessage) {
        String lower = userMessage.toLowerCase();
        boolean isPolicyQuestion = lower.contains("退") || lower.contains("退款")
            || lower.contains("换") || lower.contains("货") || lower.contains("售后")
            || lower.contains("物流") || lower.contains("快递") || lower.contains("发货")
            || lower.contains("支付") || lower.contains("付款") || lower.contains("优惠券")
            || lower.contains("砍价") || lower.contains("保修") || lower.contains("过期")
            || lower.contains("取消") || lower.contains("到账");

        if (products.isEmpty()) {
            if (isPolicyQuestion) {
                return "\n【注意：用户问的是平台政策类问题，你没有这方面的内部数据。" +
                    "请诚实告知用户你不了解具体政策，建议查看平台帮助中心或联系卖家，不要编造任何条款。】";
            }
            return "\n【当前数据库暂无精确匹配商品。请根据问题帮用户分析思路、提供选购建议，" +
                "但不要编造不存在的商品。可以引导用户使用搜索或分类筛选功能。】";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n【以下是数据库中真实的商品数据，请据此回答用户问题】\n");

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
        sb.append("\n商城共 ").append(total).append(" 件在售商品。请基于以上数据回答，不要编造数据中没有的信息。");
        return sb.toString();
    }
}
