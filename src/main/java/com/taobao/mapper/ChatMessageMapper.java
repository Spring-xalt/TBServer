package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    // 查两个用户之间的聊天记录
    @Select("SELECT * FROM chat_message " +
            "WHERE ((sender_id = #{userId1} AND receiver_id = #{userId2}) " +
            "    OR (sender_id = #{userId2} AND receiver_id = #{userId1})) " +
            "ORDER BY create_time ASC")
    List<ChatMessage> getConversation(@Param("userId1") int userId1,
                                      @Param("userId2") int userId2);
}
