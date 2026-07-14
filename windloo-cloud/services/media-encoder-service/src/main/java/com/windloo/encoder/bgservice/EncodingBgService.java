package com.windloo.encoder.bgservice;
import com.windloo.encoder.entity.EncodingItem;
import com.windloo.encoder.encoder.MediaEncoder;
import com.windloo.encoder.service.EncoderService;
import com.windloo.model.feign.ListeningFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class EncodingBgService {
    @Autowired EncoderService encoderService;
    @Autowired List<MediaEncoder> encoders;
    @Autowired ListeningFeignClient listeningFeignClient;

    @Scheduled(fixedDelay = 5000)
    public void poll() {
        List<EncodingItem> readyItems = encoderService.findReady();
        for (EncodingItem item : readyItems) {
            try {
                item.start();
                encoderService.save(item);
                EncodingItem prev = encoderService.findCompletedBySourceUrl(item.getSourceUrl());
                String outputUrl;
                if (prev != null) {
                    outputUrl = prev.getOutputUrl();
                    log.info("[DEDUP] episodeId={} reused outputUrl={}", item.getId(), outputUrl);
                } else {
                    MediaEncoder encoder = encoders.stream().filter(e -> e.accept(item.getOutputFormat())).findFirst().orElse(null);
                    if (encoder == null) { item.fail("不支持的格式: " + item.getOutputFormat()); encoderService.save(item); continue; }
                    outputUrl = encoder.encode(item.getSourceUrl(), item.getOutputFormat());
                }
                item.complete(outputUrl);
                encoderService.save(item);
                listeningFeignClient.updateAudioUrl(item.getId(), outputUrl);
                log.info("[COMPLETED] episodeId={} outputUrl={}", item.getId(), outputUrl);
            } catch (Exception e) {
                log.error("[FAIL] episodeId={}", item.getId(), e);
                item.fail(e.getMessage());
                encoderService.save(item);
            }
        }
    }
}