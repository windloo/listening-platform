package com.windloo.identity.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {
    private String name;
    private String code;
}