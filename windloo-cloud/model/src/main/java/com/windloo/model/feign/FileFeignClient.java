package com.windloo.model.feign;
import com.windloo.model.dto.UploadedItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "file-service", contextId = "file-feign", fallback = FileFeignFallback.class)
public interface FileFeignClient {
    @GetMapping("/internal/file/exists")
    UploadedItemDTO checkExists(@RequestParam("fileSize") long fileSize, @RequestParam("sha256Hash") String sha256Hash);
    @PostMapping(value = "/internal/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UploadedItemDTO upload(@RequestPart("file") MultipartFile file);
}