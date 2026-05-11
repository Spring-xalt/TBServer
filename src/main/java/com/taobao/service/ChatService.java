package com.taobao.service;

import com.taobao.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    // 发送消息+持久化
    ChatMessage sendMessage(int senderId, String senderRole,
                            int receiverId, String receiverRole, String content);

    // 查两者聊天记录
    List<ChatMessage> getConversation(int userId1, int userId2);

    // 消费者的联系人列表
    List<Integer> getConsumerContacts(int consumerId);

    // 商户的联系人列表
    List<Integer> getMerchantContacts(int merchantId);


}
