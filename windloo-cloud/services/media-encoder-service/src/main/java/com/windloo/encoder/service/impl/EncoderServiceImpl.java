package com.windloo.encoder.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windloo.encoder.entity.EncodingItem;
import com.windloo.encoder.mapper.EncodingItemMapper;
import com.windloo.encoder.service.EncoderService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EncoderServiceImpl implements EncoderService {
    private final EncodingItemMapper mapper;
    public EncoderServiceImpl(EncodingItemMapper mapper) { this.mapper = mapper; }
    @Override
    public void submit(Long episodeId, String sourceUrl, String outputFormat) {
        EncodingItem item = new EncodingItem();
        item.setId(episodeId);
        item.setSourceSystem("Listening");
        item.setName(sourceUrl.substring(sourceUrl.lastIndexOf('/') + 1));
        item.setSourceUrl(sourceUrl);
        item.setOutputFormat(outputFormat);
        item.setStatus("Ready");
        mapper.insert(item);
    }
    @Override
    public List<EncodingItem> findReady() {
        return mapper.selectList(new LambdaQueryWrapper<EncodingItem>().eq(EncodingItem::getStatus, "Ready"));
    }
    @Override
    public EncodingItem findCompletedBySourceUrl(String sourceUrl) {
        List<EncodingItem> list = mapper.selectList(new LambdaQueryWrapper<EncodingItem>()
                .eq(EncodingItem::getSourceUrl, sourceUrl).eq(EncodingItem::getStatus, "Completed"));
        return list.isEmpty() ? null : list.get(0);
    }
    @Override
    public void save(EncodingItem item) { mapper.updateById(item); }
}