package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.dto.ChatContactDto;
import com.taobao.entity.ChatMessage;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.mapper.ChatMessageMapper;
import com.taobao.mapper.ConsumerMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private MerchantMapper merchantMapper;




    @Override
    public ChatMessage sendMessage(int senderId, String senderRole,
                                   int receiverId, String receiverRole, String content) {
        //sendmessage就一个存储功能
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
        // 存消费者的聊过天的商家的ID（去重后的）（因为可能聊天记录有很多）
        Set<Integer> contactSet = new HashSet<>();

        //如果是发送方
        QueryWrapper<ChatMessage> wrapper1 = new QueryWrapper<>();

        wrapper1.eq("sender_id", consumerId).eq("sender_role", "consumer");
        List<ChatMessage> sentList = chatMessageMapper.selectList(wrapper1);

        for (ChatMessage msg : sentList) {
            // 入set
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
        // 存的是去重之后的商家联系过的消费者
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

        //最后从set---->list的
        return new ArrayList<>(contactSet);
    }



    @Override
    public ChatContactDto startNewChat(int consumerId, int merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            return null;
        }
        ChatContactDto dto = new ChatContactDto();
        dto.setId(merchantId);
        dto.setName(merchant.getMerchant_name());
        dto.setRole("merchant");
        return dto;
    }

    @Override
    public ChatContactDto startNewChatForMerchant(int merchantId, int consumerId) {
        Consumer consumer = consumerMapper.selectById(consumerId);
        if (consumer == null) return null;
        ChatContactDto dto = new ChatContactDto();
        dto.setId(consumerId);
        dto.setName(consumer.getConsumer_name() != null ? consumer.getConsumer_name() : consumer.getUsername());
        dto.setRole("consumer");
        return dto;
    }


}
