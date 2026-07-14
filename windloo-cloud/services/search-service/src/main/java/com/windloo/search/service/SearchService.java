package com.windloo.search.service;
import com.windloo.common.api.PageResult;
import com.windloo.model.dto.EpisodeIndexDTO;

public interface SearchService {
    void upsert(EpisodeIndexDTO dto);
    void remove(Long id);
    PageResult<EpisodeIndexDTO> search(String keyword, int page, int size);
}