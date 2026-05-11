package com.taobao.config;

import com.taobao.service.ChatService;
import com.taobao.websocket.ChatWebSocketHandler;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatService chatService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("===== WebSocket 配置加载 =====");

        registry.addHandler(new ChatWebSocketHandler(chatService), "/chat")  // 连接的路径
                .setAllowedOrigins("*");
    }


}
