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
        UploadedItem existing = findByHashSize(fileSize, sha256Hash);
        // DB 有记录且文件在磁盘 -> 快传
        if (existing != null && storageClient.load(sha256Hash) != null) return existing;
        // 保存文件(无记录 或 文件丢失)
        LocalDate today = LocalDate.now();
        String key = today.getYear() + "/" + today.getMonthValue() + "/" + today.getDayOfMonth() + "/" + sha256Hash + "/" + fileName;
        String url = storageClient.save(key, stream);
        if (existing != null) {
            // DB 有记录但文件丢了:更新 remoteUrl,不插新记录(避免 uk_hash_size 唯一键冲突)
            existing.setRemoteUrl(url);
            existing.setBackupUrl(url);
            mapper.updateById(existing);
            return existing;
        }
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
        UploadedItem item = findByHashSize(fileSize, sha256Hash);
        if (item == null) return null;
        // DB 有记录但文件可能丢失(如重新部署后 file-storage 未持久化);文件不在则视为不存在,触发重新上传
        if (storageClient.load(sha256Hash) == null) return null;
        return item;
    }

    private UploadedItem findByHashSize(long fileSize, String sha256Hash) {
        List<UploadedItem> list = mapper.selectList(new LambdaQueryWrapper<UploadedItem>()
                .eq(UploadedItem::getFileSizeInBytes, fileSize)
                .eq(UploadedItem::getFileSha256Hash, sha256Hash));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public org.springframework.core.io.Resource loadFile(String sha256) {
        return storageClient.load(sha256);
    }
}