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

    @Test void searchInEpisode_timestamp_returns_window_around_time() {
        EpisodeIndex idx = new EpisodeIndex();
        idx.setId(1L);
        List<EpisodeIndex.SentenceDoc> ss = new ArrayList<>();
        ss.add(doc(0, 1000, "intro"));
        ss.add(doc(94000, 96000, "the question text"));
        ss.add(doc(97000, 99000, "follow up"));
        idx.setSentences(ss);
        when(repository.findById(1L)).thenReturn(Optional.of(idx));
        List<SentenceDTO> r = service.searchInEpisode(1L, "1分35秒应该选哪个答案", 3);
        assertTrue(r.size() > 0 && r.size() <= 3);
        assertTrue(r.stream().anyMatch(s -> s.text().contains("the question text")));
    }

    @Test void searchInEpisode_keyword_matches_split_tokens() {
        when(repository.findById(1L)).thenReturn(Optional.of(episode(1L, "I love apples", "oranges are sweet", "apples and oranges")));
        List<SentenceDTO> r = service.searchInEpisode(1L, "apples oranges", 5);
        assertEquals(3, r.size());
        assertEquals("apples and oranges", r.get(0).text());
    }

    private EpisodeIndex.SentenceDoc doc(long start, long end, String text) {
        EpisodeIndex.SentenceDoc d = new EpisodeIndex.SentenceDoc();
        d.setStartMs(start); d.setEndMs(end); d.setText(text);
        return d;
    }
}