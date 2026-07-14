package com.windloo.model.feign;
import org.springframework.stereotype.Component;

@Component
public class ListeningFeignFallback implements ListeningFeignClient {
    @Override public void updateAudioUrl(Long id, String audioUrl) { }
}