package com.windloo.encoder.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.windloo.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("encoding_item")
public class EncodingItem extends BaseEntity {
    private String sourceSystem;
    private String name;
    private String sourceUrl;
    private String outputUrl;
    private String outputFormat;
    private String status;
    private String fileSha256Hash;
    private Long fileSizeInBytes;
    private String logText;
    public void start() { this.status = "Started"; }
    public void complete(String outputUrl) { this.status = "Completed"; this.outputUrl = outputUrl; this.logText = "stub转码成功"; }
    public void fail(String log) { this.status = "Failed"; this.logText = log; }
}