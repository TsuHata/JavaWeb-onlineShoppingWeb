package com.example.auth.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * 用于自动填充创建时间和更新时间
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final Logger log = LoggerFactory.getLogger(MyMetaObjectHandler.class);

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始执行插入字段自动填充");
        
        // 设置createTime字段（插入时填充）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 设置updateTime字段（插入和更新时都填充）
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始执行更新字段自动填充");
        
        // 设置updateTime字段（更新时填充）
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
} 