package com.taobao.service;


import com.taobao.common.R;
import com.taobao.entity.Admin;

public interface AdminService {
    R<Admin> login(String username, String password);
}
