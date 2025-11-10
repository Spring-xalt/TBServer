package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Admin;


public interface AdminMapper extends BaseMapper<Admin> {
    Admin selectByUsername(String username);
}
