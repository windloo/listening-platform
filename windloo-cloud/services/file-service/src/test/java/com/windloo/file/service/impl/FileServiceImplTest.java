package com.windloo.file.service.impl;
import com.windloo.file.entity.UploadedItem;
import com.windloo.file.mapper.UploadedItemMapper;
import com.windloo.file.storage.StorageClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
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

    private UploadedItem existingItem() {
        UploadedItem e = new UploadedItem();
        e.setRemoteUrl("/api/file/files/abc");
        e.setFileSha256Hash("abc");
        e.setFileSizeInBytes(11L);
        return e;
    }

    @Test void upload_dedup_returns_existing_when_file_on_disk() throws Exception {
        when(mapper.selectList(any())).thenReturn(List.of(existingItem()));
        when(storageClient.load("abc")).thenReturn(new ByteArrayResource(new byte[0]));
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
        verify(mapper).insert(any(UploadedItem.class));
    }

    @Test void upload_re_saves_and_updates_when_db_has_record_but_file_missing() throws Exception {
        when(mapper.selectList(any())).thenReturn(List.of(existingItem()));
        when(storageClient.load("abc")).thenReturn(null);
        when(storageClient.save(any(), any())).thenReturn("/api/file/files/abc");
        UploadedItem r = service.upload("test.txt", 11, "abc", new ByteArrayInputStream("hello".getBytes()));
        assertEquals("/api/file/files/abc", r.getRemoteUrl());
        verify(storageClient).save(any(), any());
        verify(mapper).updateById(any(UploadedItem.class));
        verify(mapper, never()).insert(any(UploadedItem.class));
    }

    @Test void checkExists_found_when_file_on_disk() {
        when(mapper.selectList(any())).thenReturn(List.of(existingItem()));
        when(storageClient.load("abc")).thenReturn(new ByteArrayResource(new byte[0]));
        UploadedItem r = service.checkExists(11, "abc");
        assertNotNull(r);
        assertEquals("/api/file/files/abc", r.getRemoteUrl());
    }

    @Test void checkExists_not_found() {
        when(mapper.selectList(any())).thenReturn(List.of());
        assertNull(service.checkExists(11, "abc"));
    }

    @Test void checkExists_returns_null_when_file_missing() {
        when(mapper.selectList(any())).thenReturn(List.of(existingItem()));
        when(storageClient.load("abc")).thenReturn(null);
        assertNull(service.checkExists(11, "abc"));
    }
}