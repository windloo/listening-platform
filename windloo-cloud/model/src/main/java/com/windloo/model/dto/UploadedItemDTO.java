package com.windloo.model.dto;
import lombok.Data;
@Data
public class UploadedItemDTO {
    private Long id;
    private String fileName;
    private long fileSizeInBytes;
    private String fileSha256Hash;
    private String remoteUrl;
}