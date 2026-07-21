package com.windloo.search.service;
import com.windloo.common.api.PageResult;
import com.windloo.model.dto.EpisodeIndexDTO;
import com.windloo.model.dto.SentenceDTO;
import java.util.List;

public interface SearchService {
    void upsert(EpisodeIndexDTO dto);
    void remove(Long id);
    PageResult<EpisodeIndexDTO> search(String keyword, int page, int size);
    List<SentenceDTO> searchInEpisode(Long episodeId, String keyword, int size);
}