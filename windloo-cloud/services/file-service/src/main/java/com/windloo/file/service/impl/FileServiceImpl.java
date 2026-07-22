package com.windloo.file.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windloo.file.entity.UploadedItem;
import com.windloo.file.mapper.UploadedItemMapper;
import com.windloo.file.service.FileService;
import com.windloo.file.storage.StorageClient;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    private final UploadedItemMapper mapper;
    private final StorageClient storageClient;

    public FileServiceImpl(UploadedItemMapper mapper, StorageClient storageClient) {
        this.mapper = mapper; this.storageClient = storageClient;
    }

    @Override
    public UploadedItem upload(String fileName, long fileSize, String sha256Hash, InputStream stream) throws Exception {
        UploadedItem existing = checkExists(fileSize, sha256Hash);
        if (existing != null) return existing;
        LocalDate today = LocalDate.now();
        String key = today.getYear() + "/" + today.getMonthValue() + "/" + today.getDayOfMonth() + "/" + sha256Hash + "/" + fileName;
        String url = storageClient.save(key, stream);
        UploadedItem item = new UploadedItem();
        item.setFileName(fileName);
        item.setFileSizeInBytes(fileSize);
        item.setFileSha256Hash(sha256Hash);
        item.setRemoteUrl(url);
        item.setBackupUrl(url);
        mapper.insert(item);
        return item;
    }

    @Override
    public UploadedItem checkExists(long fileSize, String sha256Hash) {
        List<UploadedItem> list = mapper.selectList(new LambdaQueryWrapper<UploadedItem>()
                .eq(UploadedItem::getFileSizeInBytes, fileSize)
                .eq(UploadedItem::getFileSha256Hash, sha256Hash));
        if (list.isEmpty()) return null;
        UploadedItem item = list.get(0);
        // DB 有记录但文件可能丢失(如重新部署后 file-storage 未持久化);文件不在则视为不存在,触发重新上传
        if (storageClient.load(sha256Hash) == null) return null;
        return item;
    }

    @Override
    public org.springframework.core.io.Resource loadFile(String sha256) {
        return storageClient.load(sha256);
    }
}