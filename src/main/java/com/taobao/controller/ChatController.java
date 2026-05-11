package com.taobao.controller;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

import com.taobao.common.R;
import com.taobao.entity.ChatMessage;
import com.taobao.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 查两个用户之间的聊天记录(sockethandler要求必须在url上传身份凭证(id+role))
    @GetMapping("/conversation")
    public R<List<ChatMessage>> conversation(@RequestParam int userId1,
                                             @RequestParam int userId2,
                                             HttpSession session) {
        // 至少有一个 userId 是自己
        String role = (String) session.getAttribute("role");
        Object myId = session.getAttribute(role + "Id");
        if (myId == null) {
            return R.error(401, "请先登录");
        }
        List<ChatMessage> messages = chatService.getConversation(userId1, userId2);
        return R.success(messages);
    }

    // 查联系人列表
    @GetMapping("/contacts")
    public R<List<Integer>> contacts(HttpSession session) {
        String role = (String) session.getAttribute("role");
        Object myId = session.getAttribute(role + "Id");
        if (myId == null) {
            return R.error(401, "请先登录");
        }
        int userId = (Integer) myId;
        List<Integer> contacts;
        if ("consumer".equals(role)) {
            contacts = chatService.getConsumerContacts(userId);
        } else if ("merchant".equals(role)) {
            contacts = chatService.getMerchantContacts(userId);
        } else {
            return R.error("未知角色");
        }
        return R.success(contacts);
    }
}
