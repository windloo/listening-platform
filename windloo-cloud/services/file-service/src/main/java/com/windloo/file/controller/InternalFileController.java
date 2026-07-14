package com.windloo.file.controller;
import com.windloo.file.entity.UploadedItem;
import com.windloo.file.service.FileService;
import com.windloo.model.dto.UploadedItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/internal/file")
public class InternalFileController {
    @Autowired FileService service;

    @GetMapping("/exists")
    public UploadedItemDTO exists(@RequestParam long fileSize, @RequestParam String sha256Hash) {
        UploadedItem item = service.checkExists(fileSize, sha256Hash);
        if (item == null) return null;
        return toDTO(item);
    }

    @PostMapping("/upload")
    public UploadedItemDTO upload(@RequestParam("file") MultipartFile file) throws Exception {
        return toDTO(service.upload(file.getOriginalFilename(), file.getSize(),
                sha256(file.getBytes()), file.getInputStream()));
    }

    private UploadedItemDTO toDTO(UploadedItem item) {
        UploadedItemDTO d = new UploadedItemDTO();
        d.setId(item.getId()); d.setFileName(item.getFileName());
        d.setFileSizeInBytes(item.getFileSizeInBytes());
        d.setFileSha256Hash(item.getFileSha256Hash()); d.setRemoteUrl(item.getRemoteUrl());
        return d;
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