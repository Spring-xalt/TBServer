package com.taobao.controller;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

import com.taobao.common.R;
import com.taobao.dto.ChatContactDto;
import com.taobao.entity.ChatMessage;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.mapper.ConsumerMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.service.ChatService;
import com.taobao.service.ConsumerService;
import com.taobao.service.MerchantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private MerchantMapper merchantMapper;




    // 查联系人列表
    @GetMapping("/contacts")
    public R<List<ChatContactDto>> contacts(HttpSession session) {
        // 从session里面拿信息
        String role = (String) session.getAttribute("role");
        Object myId = session.getAttribute(role + "Id");
        if (myId == null) {
            return R.error(401, "请先登录");
        }
        int userId = (Integer) myId;

        // 获取联系人ID列表
        List<Integer> contactIds;
        if ("consumer".equals(role)) {
            contactIds = chatService.getConsumerContacts(userId);
        } else {
            contactIds = chatService.getMerchantContacts(userId);
        }

        //  组装 DTO
        List<ChatContactDto> result = new ArrayList<>();
        for (Integer contactId : contactIds) {

            ChatContactDto dto = new ChatContactDto();
            dto.setId(contactId);

            if ("consumer".equals(role)) {
                Merchant merchant = merchantMapper.selectById(contactId);
                dto.setName(merchant != null ? merchant.getMerchant_name() : "商户" + contactId);
                dto.setRole("merchant");
            } else {
                Consumer consumer = consumerMapper.selectById(contactId);
                dto.setName(consumer != null ? consumer.getConsumer_name() : "用户" + contactId);
                dto.setRole("consumer");
            }

            result.add(dto);
        }
        return R.success(result);
    }

    // 查两个id的聊天记录
    @GetMapping("/conversation")
    public R<List<ChatMessage>> conversation(@RequestParam int userId1,
                                             @RequestParam int userId2,
                                             HttpSession session) {
        // 两个ID中至少有一个是自己
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        Integer merchantId = (Integer) session.getAttribute("merchantId");

        boolean isConsumer = (consumerId != null && (userId1 == consumerId || userId2 == consumerId));
        boolean isMerchant = (merchantId != null && (userId1 == merchantId || userId2 == merchantId));

        if (!isConsumer && !isMerchant) {
            return R.error(403, "无权查看");
        }

        List<ChatMessage> messages = chatService.getConversation(userId1, userId2);
        return R.success(messages);
    }


    @GetMapping("/consumerNewChat")
    public R<ChatContactDto> consumerNewChat(@RequestParam int targetId, HttpSession session) {
        Integer consumerId = (Integer) session.getAttribute("consumerId");
        if (consumerId == null) {
            return R.error(401, "请先登录消费者账号");
        }

        ChatContactDto dto = chatService.startNewChat(consumerId, targetId);
        if (dto == null) {
            return R.error("商户不存在");
        }
        return R.success(dto);
    }



}
