package com.windloo.file.service.impl;
import com.windloo.file.entity.UploadedItem;
import com.windloo.file.mapper.UploadedItemMapper;
import com.windloo.file.storage.StorageClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {
    @Mock UploadedItemMapper mapper;
    @Mock StorageClient storageClient;
    @InjectMocks FileServiceImpl service;

    @Test void upload_dedup_returns_existing() throws Exception {
        UploadedItem existing = new UploadedItem();
        existing.setRemoteUrl("/api/file/files/abc");
        when(mapper.selectList(any())).thenReturn(List.of(existing));
        UploadedItem r = service.upload("test.txt", 11, "abc", new ByteArrayInputStream("hello".getBytes()));
        assertEquals("/api/file/files/abc", r.getRemoteUrl());
        verify(storageClient, never()).save(any(), any());
    }

    @Test void upload_new_saves_and_inserts() throws Exception {
        when(mapper.selectList(any())).thenReturn(List.of());
        when(storageClient.save(any(), any())).thenReturn("/api/file/files/abc");
        UploadedItem r = service.upload("test.txt", 11, "abc", new ByteArrayInputStream("hello".getBytes()));
        assertEquals("/api/file/files/abc", r.getRemoteUrl());
        verify(storageClient).save(any(), any());
        verify(mapper).insert(any(com.windloo.file.entity.UploadedItem.class));
    }

    @Test void checkExists_found() {
        UploadedItem existing = new UploadedItem();
        existing.setRemoteUrl("/api/file/files/abc");
        when(mapper.selectList(any())).thenReturn(List.of(existing));
        UploadedItem r = service.checkExists(11, "abc");
        assertNotNull(r);
        assertEquals("/api/file/files/abc", r.getRemoteUrl());
    }

    @Test void checkExists_not_found() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertNull(service.checkExists(11, "abc"));
    }
}