package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.dto.UserLoginDto;
import com.taobao.dto.UserRegisterDto;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.entity.Orders;
import com.taobao.mapper.ConsumerMapper;
import com.taobao.mapper.MerchantMapper;
import com.taobao.mapper.OrdersMapper;
import com.taobao.service.ConsumerService;
import com.taobao.service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 @auther:Jimi
 @description: 消费者表
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {
    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public List<Consumer> getAllConsumers() {
        return consumerMapper.selectList(null);
    }

    @Transactional
    @Override
    public boolean addConsumer(Consumer consumer) {
        return consumerMapper.insert(consumer) == 1;
    }

    //删除消费者的前提是 所有与他有关的订单都是已签收 3 的状态
    @Transactional
    @Override
    public boolean deleteConsumer(Integer id) {
        QueryWrapper<Orders> cqw = new QueryWrapper<>();
        cqw.eq("consumer_id", id);
        List<Orders> os= ordersMapper.selectList(cqw);
        //有与之相关的订单
        if(!os.isEmpty()){
            for(Orders o:os){
                if(o.getStatus()!=3){
                    //表示有的订单未签收
                    return false;
                }
            }
            return consumerMapper.deleteById(id)==1;
        }
        //没有就删除
        consumerMapper.deleteById(id);
        return consumerMapper.deleteById(id) == 1;
    }

    @Override
    public boolean updateConsumer(Consumer consumer) {
        return consumerMapper.updateById(consumer) == 1;
    }

    @Override
    public Consumer getConsumerById(Integer id) {
        return consumerMapper.selectById(id);
    }

    @Override
    public R<String> register(UserRegisterDto registerDto) {
        //先验证库里是否有该用户名
        QueryWrapper<Consumer> mqr = new QueryWrapper<>();
        mqr.eq("username", registerDto.getUsername());
        Long count= consumerMapper.selectCount(mqr);
        //有就提示
        if(count>0){
            return R.error("您已经注册过消费者账号了,请跳转登录界面");
        }
        //没有就注册并入库
        Consumer c = new Consumer();
//        merchant.setUsername(registerDto.getUsername());直接复制写法
//        merchant.setPassword(registerDto.getPassword());
        //同时默认设置消费者名字也为username
        c.setConsumer_name (registerDto.getUsername ());
        BeanUtils.copyProperties(registerDto, c);

        consumerMapper.insert(c);
        return R.success("注册成功!");
    }

    @Override
    public R<Consumer> login(UserLoginDto loginDto, HttpSession session) {
        QueryWrapper<Consumer> mqw = new QueryWrapper<>();
        mqw.eq("username", loginDto.getUsername());
        Consumer consumer = consumerMapper.selectOne(mqw);

        if(consumer == null){
            return R.error("您还未注册过消费者账号,请先注册");
        }

        if(!consumer.getPassword().equals(loginDto.getPassword())){
            return R.error("密码错误，请重试!");
        }

        // 登录之后的关键，将消费者id写入session，后续需要靠这个校验查数据
        session.setAttribute("consumerId", consumer.getId());

        // 前端角色判断
        session.setAttribute("role", "consumer");

        return R.success("登录成功!", consumer);
    }
}
