package com.windloo.listening.service.impl;
import com.windloo.common.exception.BizException;
import com.windloo.listening.entity.Episode;
import com.windloo.listening.event.EpisodeRemovedEvent;
import com.windloo.listening.event.EpisodeUpsertedEvent;
import com.windloo.listening.mapper.AlbumMapper;
import com.windloo.listening.mapper.CategoryMapper;
import com.windloo.listening.mapper.EpisodeMapper;
import com.windloo.listening.subtitle.SubtitleParser;
import com.windloo.listening.subtitle.SubtitleParserFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListeningServiceImplTest {
    @Mock CategoryMapper categoryMapper;
    @Mock AlbumMapper albumMapper;
    @Mock EpisodeMapper episodeMapper;
    @Mock SubtitleParserFactory parserFactory;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks ListeningServiceImpl service;

    @Test void createCategory_inserts_with_seq1_when_empty() {
        when(categoryMapper.selectList(any())).thenReturn(List.of());
        var c = service.createCategory("中", "英", "cover");
        assertEquals(1, c.getSequenceNumber());
        verify(categoryMapper).insert(any(com.windloo.listening.entity.Category.class));
    }

    @Test void deleteCategory_soft_deletes_via_wrapper() {
        service.deleteCategory(1L);
        verify(categoryMapper).update(isNull(), any());
    }

    @Test void sortCategories_resequences_all() {
        service.sortCategories(List.of(3L, 1L, 2L));
        verify(categoryMapper, times(3)).update(isNull(), any());
    }

    @Test void createEpisode_inserts_and_publishes_upserted() {
        SubtitleParser p = mock(SubtitleParser.class);
        when(parserFactory.get("srt")).thenReturn(p);
        when(p.parse("content")).thenReturn(List.of());
        service.createEpisode("中", "英", 1L, "url", 60.0, "content", "srt");
        verify(episodeMapper).insert(any(Episode.class));
        verify(eventPublisher).publishEvent(any(EpisodeUpsertedEvent.class));
    }

    @Test void createEpisode_unsupported_subtitle_type_throws() {
        when(parserFactory.get("xyz")).thenReturn(null);
        assertThrows(BizException.class, () -> service.createEpisode("中", "英", 1L, "url", 60.0, "c", "xyz"));
    }

    @Test void hideEpisode_publishes_removed() {
        Episode e = new Episode(); e.setId(1L); e.setSubtitle("c"); e.setSubtitleType("srt");
        when(episodeMapper.selectById(1L)).thenReturn(e);
        service.hideEpisode(1L);
        verify(eventPublisher).publishEvent(any(EpisodeRemovedEvent.class));
    }

    @Test void deleteEpisode_soft_deletes_and_publishes_removed() {
        Episode e = new Episode(); e.setId(1L);
        when(episodeMapper.selectById(1L)).thenReturn(e);
        service.deleteEpisode(1L);
        verify(episodeMapper).update(isNull(), any());
        verify(eventPublisher).publishEvent(any(EpisodeRemovedEvent.class));
    }
}