package com.windloo.listening.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.windloo.listening.subtitle.Sentence;
import java.util.List;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("episode")
public class Episode extends BaseEntity {
    private Integer sequenceNumber;
    private String nameChinese;
    private String nameEnglish;
    private Long albumId;
    private String audioUrl;
    private Double durationInSecond;
    private String subtitle;
    private String subtitleType;
    private Boolean isVisible;
    private Long createdBy;

    @TableField(exist = false)
    private List<Sentence> sentences;
}