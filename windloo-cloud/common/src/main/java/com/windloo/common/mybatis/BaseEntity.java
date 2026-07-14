package com.windloo.common.mybatis;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime deletionTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}