package com.taobao.entity;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Admin {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;
    private String password;


}

