package com.windloo.listening.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {
    private Integer sequenceNumber;
    private String nameChinese;
    private String nameEnglish;
    private String coverUrl;
    private Long createdBy;
}