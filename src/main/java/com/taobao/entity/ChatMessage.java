package com.taobao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Data
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer sender_id;
    private String sender_role;
    private Integer receiver_id;
    private String receiver_role;
    private String content;
    private LocalDateTime create_time;
}
