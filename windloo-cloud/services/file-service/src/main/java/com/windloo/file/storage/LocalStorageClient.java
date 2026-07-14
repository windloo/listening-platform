package com.windloo.file.storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class LocalStorageClient implements StorageClient {
    @Value("${file.storage-root:./file-storage}")
    String storageRoot;

    @Override
    public String save(String key, InputStream stream) throws Exception {
        Path dest = Paths.get(storageRoot, key);
        Files.createDirectories(dest.getParent());
        Files.copy(stream, dest, StandardCopyOption.REPLACE_EXISTING);
        String[] parts = key.split("/");
        String sha256 = parts[3];
        return "/api/file/files/" + sha256;
    }

    @Override
    public org.springframework.core.io.Resource load(String sha256) {
        try {
            Path dir = Paths.get(storageRoot);
            try (var paths = Files.walk(dir)) {
                var found = paths.filter(p -> p.toString().replace("\\", "/").contains("/" + sha256 + "/") && Files.isRegularFile(p)).findFirst();
                if (found.isEmpty()) return null;
                return new UrlResource(found.get().toUri());
            }
        } catch (Exception e) {
            log.error("load failed for sha256={}", sha256, e);
            return null;
        }
    }
}