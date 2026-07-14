package com.windloo.model.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "listening-service", url = "${feign.url.listening:}",
             contextId = "listening-feign", fallback = ListeningFeignFallback.class)
public interface ListeningFeignClient {
    @PutMapping("/internal/listening/episodes/{id}/audioUrl")
    void updateAudioUrl(@PathVariable Long id, @RequestParam String audioUrl);
}