package com.windloo.identity.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String userName;
    private String normalizedUserName;
    private String phone;
    private String passwordHash;
    private Boolean isLocked;
    private LocalDateTime lockoutEnd;
    private Integer accessFailedCount;
    private String avatar;
    private String nickname;
}