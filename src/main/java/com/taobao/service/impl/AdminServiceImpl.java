package com.taobao.service.impl;

import com.taobao.common.R;
import com.taobao.entity.Admin;
import com.taobao.mapper.AdminMapper;
import com.taobao.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;


    @Override
    public R<Admin> login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            return R.error("管理员不存在");
        }

        String md5Input = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!admin.getPassword().equals(md5Input)) {
            return R.error("密码错误");
        }

        // 不返回密码
        admin.setPassword(null);
        return R.success("登录成功", admin);
    }


}
