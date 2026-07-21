package com.windloo.ai.context;
import com.windloo.model.dto.SentenceDTO;
import com.windloo.model.feign.SearchFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchContextProviderTest {
    @Mock SearchFeignClient searchFeignClient;
    SearchContextProvider provider;

    @BeforeEach void setup() { provider = new SearchContextProvider(searchFeignClient, 5); }

    @Test void returns_empty_when_episodeId_null_and_no_feign_call() {
        assertTrue(provider.retrieve("q", 1L, null).isEmpty());
        verifyNoInteractions(searchFeignClient);
    }

    @Test void maps_sentences_to_chunks_with_timestamp_source() {
        when(searchFeignClient.searchSentences(7L, "hello", 5)).thenReturn(List.of(
                new SentenceDTO(15000, 17000, "Hello world"),
                new SentenceDTO(22000, 24000, "Hello again")));
        List<ContextProvider.ContextChunk> chunks = provider.retrieve("hello", 1L, 7L);
        assertEquals(2, chunks.size());
        assertEquals("[00:15]", chunks.get(0).source());
        assertEquals("Hello world", chunks.get(0).text());
        assertEquals("[00:22]", chunks.get(1).source());
    }

    @Test void returns_empty_when_feign_returns_empty() {
        when(searchFeignClient.searchSentences(7L, "zzz", 5)).thenReturn(List.of());
        assertTrue(provider.retrieve("zzz", 1L, 7L).isEmpty());
    }
}