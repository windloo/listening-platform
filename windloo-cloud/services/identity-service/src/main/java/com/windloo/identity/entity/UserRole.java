package com.windloo.identity.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_role")
public class UserRole extends BaseEntity {
    private Long userId;
    private Long roleId;
}