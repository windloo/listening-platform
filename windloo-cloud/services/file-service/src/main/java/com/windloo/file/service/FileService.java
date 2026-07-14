package com.windloo.file.service;
import com.windloo.file.entity.UploadedItem;
import java.io.InputStream;
import org.springframework.core.io.Resource;

public interface FileService {
    UploadedItem upload(String fileName, long fileSize, String sha256Hash, InputStream stream) throws Exception;
    UploadedItem checkExists(long fileSize, String sha256Hash);
    Resource loadFile(String sha256);
}