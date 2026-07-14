package com.windloo.model.feign;
import com.windloo.model.dto.SubmitEncoderReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "media-encoder-service", url = "${feign.url.media-encoder:}",
             contextId = "media-encoder-feign", fallback = MediaEncoderFeignFallback.class)
public interface MediaEncoderFeignClient {
    @PostMapping("/internal/encoder/submit")
    void submit(@RequestBody SubmitEncoderReq req);
}