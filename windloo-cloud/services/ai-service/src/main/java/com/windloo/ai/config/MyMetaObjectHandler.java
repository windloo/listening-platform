package com.windloo.ai.config;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override public void insertFill(MetaObject m) {
        this.strictInsertFill(m, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(m, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(m, "isDeleted", Integer.class, 0);
    }
    @Override public void updateFill(MetaObject m) {
        this.strictUpdateFill(m, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}