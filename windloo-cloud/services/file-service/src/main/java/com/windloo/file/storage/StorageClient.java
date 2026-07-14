package com.windloo.file.storage;
import java.io.InputStream;
import org.springframework.core.io.Resource;

public interface StorageClient {
    String save(String key, InputStream stream) throws Exception;
    Resource load(String sha256);
}