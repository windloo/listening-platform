package com.windloo.file.controller;
import com.windloo.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/file/files")
public class FileController {
    @Autowired FileService service;

    @GetMapping("/{sha256}")
    public ResponseEntity<Resource> download(@PathVariable String sha256) {
        Resource resource = service.loadFile(sha256);
        if (resource == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}