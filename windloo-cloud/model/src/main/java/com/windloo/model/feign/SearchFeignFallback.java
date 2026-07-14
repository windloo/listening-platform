package com.windloo.model.feign;
import com.windloo.model.dto.EpisodeIndexDTO;
import org.springframework.stereotype.Component;

@Component
public class SearchFeignFallback implements SearchFeignClient {
    @Override public void upsert(EpisodeIndexDTO dto) { }
    @Override public void remove(Long id) { }
}