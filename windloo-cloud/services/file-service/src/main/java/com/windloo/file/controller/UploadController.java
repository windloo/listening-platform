package com.windloo.file.controller;
import com.windloo.common.api.JsonResponse;
import com.windloo.file.entity.UploadedItem;
import com.windloo.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file/admin")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class UploadController {
    @Autowired FileService service;

    @GetMapping("/exists")
    public JsonResponse<UploadedItem> exists(@RequestParam long fileSize, @RequestParam String sha256Hash) {
        return JsonResponse.ok(service.checkExists(fileSize, sha256Hash));
    }

    @PostMapping("/upload")
    public JsonResponse<UploadedItem> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String sha256 = sha256(file.getBytes());
        UploadedItem item = service.upload(file.getOriginalFilename(), file.getSize(), sha256, file.getInputStream());
        return JsonResponse.ok(item);
    }

    private String sha256(byte[] data) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}