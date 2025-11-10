package com.taobao.service;

import com.taobao.common.R;
import com.taobao.dto.UserLoginDto;
import com.taobao.dto.UserRegisterDto;
import com.taobao.entity.Consumer;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ConsumerService {
    List<Consumer> getAllConsumers();
    boolean addConsumer(Consumer consumer);
    boolean deleteConsumer(Integer id);
    boolean updateConsumer(Consumer customer);
    Consumer getConsumerById(Integer id);


    R<String> register(UserRegisterDto registerDto);
    R<Consumer> login( UserLoginDto loginDto);
}
