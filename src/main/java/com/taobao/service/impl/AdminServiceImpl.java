package com.taobao.service.impl;

import com.taobao.common.R;
import com.taobao.dto.UserLoginDto;
import com.taobao.entity.Admin;
import com.taobao.mapper.AdminMapper;
import com.taobao.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*
 @auther:Jimi
 @description: 管理员
 */
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private HttpSession session;


    @Override
    public R<String> login(UserLoginDto loginDto) {
        // 1. 获取用户名和密码
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();

        // 2. 根据用户名查询管理员
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            return R.error("管理员不存在");
        }

        // 3. 检查密码（MD5加密验证）
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(admin.getPassword())) {
            return R.error("密码错误");
        }

        // 4. 登录成功，将用户信息存入session
        session.setAttribute("admin", admin);
        session.setAttribute("role", "admin");

        return R.success("管理员登录成功");
    }

    @Override
    public R<String> logout() {
        // 清除session
        session.removeAttribute("admin");
        session.removeAttribute("role");
        return R.success("管理员退出成功");
    }
}
