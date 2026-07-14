package com.windloo.model.feign;
import com.windloo.model.dto.UploadedItemDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileFeignFallback implements FileFeignClient {
    @Override
    public UploadedItemDTO checkExists(long fileSize, String sha256Hash) { return null; }
    @Override
    public UploadedItemDTO upload(MultipartFile file) { UploadedItemDTO d = new UploadedItemDTO(); d.setRemoteUrl(null); return d; }
}