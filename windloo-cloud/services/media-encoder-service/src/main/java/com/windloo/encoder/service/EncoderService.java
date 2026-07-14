package com.windloo.encoder.service;
import com.windloo.encoder.entity.EncodingItem;
import java.util.List;

public interface EncoderService {
    void submit(Long episodeId, String sourceUrl, String outputFormat);
    List<EncodingItem> findReady();
    EncodingItem findCompletedBySourceUrl(String sourceUrl);
    void save(EncodingItem item);
}