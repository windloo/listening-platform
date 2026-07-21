package com.windloo.search.service.impl;
import com.windloo.model.dto.SentenceDTO;
import com.windloo.search.entity.EpisodeIndex;
import com.windloo.search.repository.EpisodeIndexRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {
    @Mock EpisodeIndexRepository repository;
    @InjectMocks SearchServiceImpl service;

    private EpisodeIndex episode(Long id, String... texts) {
        EpisodeIndex idx = new EpisodeIndex();
        idx.setId(id);
        List<EpisodeIndex.SentenceDoc> ss = new ArrayList<>();
        for (String t : texts) {
            EpisodeIndex.SentenceDoc d = new EpisodeIndex.SentenceDoc();
            d.setStartMs(0); d.setEndMs(0); d.setText(t);
            ss.add(d);
        }
        idx.setSentences(ss);
        return idx;
    }

    @Test void searchInEpisode_filters_case_insensitive_and_limits() {
        when(repository.findById(1L)).thenReturn(Optional.of(episode(1L, "Hello world", "Goodbye world", "Hello again")));
        assertEquals(2, service.searchInEpisode(1L, "hello", 5).size());
        assertEquals(1, service.searchInEpisode(1L, "hello", 1).size());
    }

    @Test void searchInEpisode_empty_keyword_returns_empty_without_query() {
        assertEquals(List.of(), service.searchInEpisode(1L, "  ", 5));
        verifyNoInteractions(repository);
    }

    @Test void searchInEpisode_not_found_returns_empty() {
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertEquals(List.of(), service.searchInEpisode(2L, "x", 5));
    }
}