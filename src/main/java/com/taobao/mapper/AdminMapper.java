package com.taobao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taobao.entity.Admin;
import org.apache.ibatis.annotations.Select;


public interface AdminMapper extends BaseMapper<Admin> {
    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin selectByUsername(String username);
}
