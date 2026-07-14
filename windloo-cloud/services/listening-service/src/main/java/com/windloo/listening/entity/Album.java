package com.windloo.listening.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("album")
public class Album extends BaseEntity {
    private Integer sequenceNumber;
    private String nameChinese;
    private String nameEnglish;
    private Long categoryId;
    private Boolean isVisible;
    private Long createdBy;
}