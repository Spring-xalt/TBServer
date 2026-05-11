package com.taobao.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
// 负责连接，断开，收发消息
public class ChatWebSocketHandler extends TextWebSocketHandler {
    // 存储所有在线用户（role_id,session）保证线程安全
    private static final ConcurrentHashMap<String, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();

    // 前端建立连接后 自动触发该钩子函数
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            onlineUsers.put(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 负责消息转发与存储

    }

    // 客户端断开连接
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            // 断连了就剔除在线列表
            onlineUsers.remove(userId);
        }
    }

    private String getUserId(WebSocketSession session) {
        // 解析url参数，类似userId=7&role=consumer，用于map的key
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId")) {
            return query;
        }
        return null;
    }

    // 公共方法：向指定用户发送消息
    public static void sendToUser(String userId, String message) {
        WebSocketSession session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
