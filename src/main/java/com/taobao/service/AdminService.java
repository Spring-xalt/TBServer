package com.taobao.service;

import com.taobao.common.R;
import com.taobao.dto.UserLoginDto;

public interface AdminService {
    // 管理员登录
    R<String> login(UserLoginDto loginDto);

    // 管理员退出
    R<String> logout();
}
