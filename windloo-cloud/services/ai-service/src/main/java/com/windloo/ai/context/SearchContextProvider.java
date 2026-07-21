package com.windloo.ai.context;
import com.windloo.model.dto.SentenceDTO;
import com.windloo.model.feign.SearchFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@Primary
public class SearchContextProvider implements ContextProvider {
    private final SearchFeignClient searchFeignClient;
    private final int topK;

    public SearchContextProvider(SearchFeignClient searchFeignClient,
                                 @Value("${ai.context.episode-top-k:5}") int topK) {
        this.searchFeignClient = searchFeignClient;
        this.topK = topK;
    }

    @Override
    public List<ContextChunk> retrieve(String query, Long userId, Long episodeId) {
        if (episodeId == null) return List.of();
        List<SentenceDTO> sentences = searchFeignClient.searchSentences(episodeId, query, topK);
        if (sentences == null || sentences.isEmpty()) {
            log.info("RAG episodeId={} no sentences for query=\"{}\"", episodeId, truncate(query));
            return List.of();
        }
        log.info("RAG episodeId={} query=\"{}\" -> {} sentences", episodeId, truncate(query), sentences.size());
        return sentences.stream()
                .map(s -> new ContextChunk(fmtTime(s.startMs()), s.text()))
                .toList();
    }

    private String truncate(String s) {
        return s == null ? "" : (s.length() > 50 ? s.substring(0, 50) + "..." : s);
    }

    private String fmtTime(long ms) {
        long sec = ms / 1000;
        return String.format("[%02d:%02d]", sec / 60, sec % 60);
    }
}