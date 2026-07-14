package com.windloo.file.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("uploaded_item")
public class UploadedItem extends BaseEntity {
    private String fileName;
    private long fileSizeInBytes;
    private String fileSha256Hash;
    private String remoteUrl;
    private String backupUrl;
}