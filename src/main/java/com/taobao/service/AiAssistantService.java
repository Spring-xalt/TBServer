package com.taobao.service;

import java.util.List;

public interface AiAssistantService {
    /**
     * 发送消息给 AI 助手并获取回复
     */
    String chat(String userMessage);

    /**
     * 随机获取一批推荐问题（换一批）
     */
    List<String> getSuggestions();
}
