package com.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taobao.common.R;
import com.taobao.dto.MerchantDto;
import com.taobao.dto.UserLoginDto;
import com.taobao.dto.UserRegisterDto;
import com.taobao.entity.Consumer;
import com.taobao.entity.Merchant;
import com.taobao.entity.Product;
import com.taobao.mapper.MerchantMapper;
import com.taobao.mapper.ProductMapper;
import com.taobao.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 @auther:Jimi
 @description: 处理与商户相关的业务逻辑 增删改查 其中增改查都有product附加
 */

/*
    @RequiredArgsConstructor为 lombok 提供的注解 为+final或被@NonNull注解的字段自动生成构造器
 */
@RequiredArgsConstructor
@Service
public class MerchantServiceImpl implements MerchantService {


    private final MerchantMapper merchantMapper;

    private final ProductMapper productMapper;

    @Override
    public List<Merchant> getAllMerchants() {
        List<Merchant> list = merchantMapper.selectList(null);
        list.forEach(System.out::println);

        return merchantMapper.selectList(null);
    }

    @Override
    public Merchant getMerchantById(Integer id) {
        return merchantMapper.selectById(id);
    }

    //删除对应的产品也应当下架
    @Transactional
    @Override
    public boolean deleteMerchant(Integer id) {
        //控制层做过筛选了
        QueryWrapper<Product> productQuery = new QueryWrapper<>();
        productQuery.eq("merchant_id", id);
        List<Product> merchantProducts = productMapper.selectList(productQuery);

        if (!merchantProducts.isEmpty()) {
            merchantProducts.forEach(product -> {
                productMapper.deleteById(product.getId());
            });
        }
        return merchantMapper.deleteById(id) > 0;
    }



    //开启事务支持 添加商户时的产品也应当持久化到products表中
    @Transactional
    @Override
    public boolean addMerchant(MerchantDto merchantDto) {
        // dto->实体
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantDto, merchant);
        // merchant基础信息入merchant库
        int result = merchantMapper.insert(merchant);

        // 保存到商品表 同时绑定product的外键merchant_id
        if (merchantDto.getProducts() != null) {
            merchantDto.getProducts().forEach(product -> {
                product.setMerchant_id(merchant.getId());
                productMapper.insert(product);
            });
        }
        return result>0;
    }

    @Transactional
    @Override
    public boolean updateMerchant(MerchantDto merchantDTO) {
        if (merchantDTO.getId() == null) {
            throw new IllegalStateException("更新操作必须传入id");
        }

        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantDTO, merchant);
        int result = merchantMapper.updateById(merchant);

        // 更新商品列表
        if (merchantDTO.getProducts() != null) {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("merchant_id", merchantDTO.getId());
            productMapper.delete(queryWrapper);

            merchantDTO.getProducts().forEach(product -> {
                product.setMerchant_id(merchantDTO.getId());
                productMapper.insert(product);
            });
        }
        return result > 0;
    }

    @Override
    public R<String> register(UserRegisterDto registerDto) {
        //先验证库里是否有该用户名(根据姓名)
        QueryWrapper<Merchant> mqr = new QueryWrapper<>();
        mqr.eq("username", registerDto.getUsername());
        Long count=merchantMapper.selectCount(mqr);
        //有就提示
        if(count>0){
            return R.error("您已经注册过商铺账号了,请跳转登录界面");
        }
        //没有就注册并入库
        Merchant merchant = new Merchant();
        merchant.setUsername(registerDto.getUsername());
        String pwd = registerDto.getPassword();
        if(pwd.length()<6||pwd.length()>18){
            return R.error("密码长度应当在6-18位之间,请重新输入!");
        }
        merchant.setPassword(pwd);
        //BeanUtils.copyProperties(registerDto, merchant);简洁写法
        //默认为username
        merchant.setMerchant_name(registerDto.getUsername());

        merchantMapper.insert(merchant);
        return R.success("注册成功!");
    }

    @Override
    public R<String> login(UserLoginDto loginDto) {
        QueryWrapper<Merchant> mqw = new QueryWrapper<>();
        mqw.eq("username", loginDto.getUsername());
        Merchant merchant = merchantMapper.selectOne(mqw);
        if(merchant == null){
            return R.error("您还未注册过商铺账号,请先注册");
        }

        if(!merchant.getPassword().equals(loginDto.getPassword())){
            return R.error("密码错误，请重试!");
        }
        return R.success("登录成功!");
    }

}
