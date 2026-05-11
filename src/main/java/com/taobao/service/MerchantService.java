package com.taobao.service;

import com.taobao.common.R;
import com.taobao.dto.MerchantDto;
import com.taobao.dto.UserLoginDto;
import com.taobao.dto.UserRegisterDto;
import com.taobao.entity.Merchant;
import com.taobao.entity.Product;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface MerchantService {

    boolean addMerchant(MerchantDto merchantdto);
    boolean updateMerchant(MerchantDto merchantdto);
    List<Merchant> getAllMerchants();
    Merchant getMerchantById(Integer id);
    boolean deleteMerchant(Integer id);


    R<String> register(UserRegisterDto registerDto);
    R<String> login(UserLoginDto loginDto, HttpSession session);


}
