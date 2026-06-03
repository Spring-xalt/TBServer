package com.taobao.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/*
 @auther:Jimi
 @description: time 的自动填充处理器
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    //数据库字段自动填充set
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "create_time", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "update_time", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 使用 setFieldValByName 而非 strictUpdateFill，因为 strict 模式仅在字段为 null 时填充，
        // 而从 DB 查出的实体 update_time 已有值，会导致旧时间被写回
        this.setFieldValByName("update_time", LocalDateTime.now(), metaObject);
    }
}
