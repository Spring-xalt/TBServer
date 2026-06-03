package com.taobao.controller;

import com.taobao.common.R;
import com.taobao.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assistant")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public R<String> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "").trim();
        if (message.isEmpty()) {
            return R.error("消息不能为空");
        }
        if (message.length() > 500) {
            return R.error("消息过长，请控制在500字以内");
        }
        String reply = aiAssistantService.chat(message);
        return R.success(reply);
    }

    @GetMapping("/suggestions")
    public R<List<String>> suggestions() {
        List<String> list = aiAssistantService.getSuggestions();
        return R.success(list);
    }
}
