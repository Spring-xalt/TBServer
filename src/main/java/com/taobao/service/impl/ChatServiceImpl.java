package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.entity.ChatMessage;
import com.taobao.mapper.ChatMessageMapper;
import com.taobao.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage sendMessage(int senderId, String senderRole,
                                   int receiverId, String receiverRole, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSender_id(senderId);
        msg.setSender_role(senderRole);
        msg.setReceiver_id(receiverId);
        msg.setReceiver_role(receiverRole);
        msg.setContent(content);
        chatMessageMapper.insert(msg);
        return msg;
    }

    @Override
    public List<ChatMessage> getConversation(int userId1, int userId2) {
        //查聊天记录
        return chatMessageMapper.getConversation(userId1, userId2);
    }

    @Override
    public List<Integer> getConsumerContacts(int consumerId) {
        // 查所有发过或收过消息的商户ID(查chatMessage表内的)
        Set<Integer> contactSet = new HashSet<>();

        QueryWrapper<ChatMessage> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("sender_id", consumerId).eq("sender_role", "consumer");
        List<ChatMessage> sentList = chatMessageMapper.selectList(wrapper1);

        for (ChatMessage msg : sentList) {
            contactSet.add(msg.getReceiver_id());
        }

        QueryWrapper<ChatMessage> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("receiver_id", consumerId).eq("receiver_role", "consumer");
        List<ChatMessage> receivedList = chatMessageMapper.selectList(wrapper2);
        for (ChatMessage msg : receivedList) {
            contactSet.add(msg.getSender_id());
        }

        return new ArrayList<>(contactSet);
    }

    @Override
    public List<Integer> getMerchantContacts(int merchantId) {
        Set<Integer> contactSet = new HashSet<>();

        QueryWrapper<ChatMessage> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("sender_id", merchantId).eq("sender_role", "merchant");
        List<ChatMessage> sentList = chatMessageMapper.selectList(wrapper1);
        for (ChatMessage msg : sentList) {
            contactSet.add(msg.getReceiver_id());
        }

        QueryWrapper<ChatMessage> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("receiver_id", merchantId).eq("receiver_role", "merchant");
        List<ChatMessage> receivedList = chatMessageMapper.selectList(wrapper2);
        for (ChatMessage msg : receivedList) {
            contactSet.add(msg.getSender_id());
        }

        return new ArrayList<>(contactSet);
    }
}
