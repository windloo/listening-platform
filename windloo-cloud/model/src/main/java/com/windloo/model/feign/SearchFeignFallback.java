package com.windloo.model.feign;
import com.windloo.model.dto.EpisodeIndexDTO;
import com.windloo.model.dto.SentenceDTO;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SearchFeignFallback implements SearchFeignClient {
    @Override public void upsert(EpisodeIndexDTO dto) { }
    @Override public void remove(Long id) { }
    @Override public List<SentenceDTO> searchSentences(Long episodeId, String keyword, int size) { return List.of(); }
}