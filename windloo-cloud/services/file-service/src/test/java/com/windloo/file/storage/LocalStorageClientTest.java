package com.windloo.file.storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.*;

class LocalStorageClientTest {
    @TempDir Path tempDir;

    @AfterEach void cleanup() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir).sorted(Comparator.reverseOrder())
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception e) {} });
        }
    }

    @Test void save_then_load_roundtrip() throws Exception {
        LocalStorageClient c = new LocalStorageClient();
        java.lang.reflect.Field f = LocalStorageClient.class.getDeclaredField("storageRoot");
        f.setAccessible(true);
        f.set(c, tempDir.toString());

        byte[] data = "hello world".getBytes();
        String url = c.save("2026/07/08/abc123/test.txt", new ByteArrayInputStream(data));
        assertEquals("/api/file/files/abc123", url);

        var resource = c.load("abc123");
        assertNotNull(resource);
        var is = resource.getInputStream();
        assertArrayEquals(data, is.readAllBytes());
        is.close();
    }
}