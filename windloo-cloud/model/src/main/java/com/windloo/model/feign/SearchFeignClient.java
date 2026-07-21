package com.windloo.model.feign;
import com.windloo.model.dto.EpisodeIndexDTO;
import com.windloo.model.dto.SentenceDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "search-service", url = "${feign.url.search:}",
             contextId = "search-feign", fallback = SearchFeignFallback.class)
public interface SearchFeignClient {
    @PostMapping("/internal/search/episodes")
    void upsert(@RequestBody EpisodeIndexDTO dto);
    @DeleteMapping("/internal/search/episodes/{id}")
    void remove(@PathVariable Long id);

    @GetMapping("/internal/search/episodes/{episodeId}/sentences")
    List<SentenceDTO> searchSentences(@PathVariable("episodeId") Long episodeId,
                                      @RequestParam("keyword") String keyword,
                                      @RequestParam("size") int size);
}