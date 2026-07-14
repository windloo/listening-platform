package com.windloo.listening.event;

import com.windloo.listening.subtitle.Sentence;
import com.windloo.model.dto.EpisodeIndexDTO;
import com.windloo.model.dto.SentenceDTO;
import com.windloo.model.feign.SearchFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EpisodeEventHandler {
    @Autowired SearchFeignClient searchFeignClient;

    @Async @EventListener
    public void onUpserted(EpisodeUpsertedEvent e) {
        List<SentenceDTO> sentences = e.sentences().stream()
                .map(s -> new SentenceDTO(s.startMs(), s.endMs(), s.text()))
                .toList();
        EpisodeIndexDTO dto = new EpisodeIndexDTO();
        dto.setId(e.id()); dto.setNameChinese(e.nameChinese());
        dto.setNameEnglish(e.nameEnglish()); dto.setAlbumId(e.albumId());
        dto.setSentences(sentences);
        searchFeignClient.upsert(dto);
    }

    @Async @EventListener
    public void onRemoved(EpisodeRemovedEvent e) {
        searchFeignClient.remove(e.id());
    }
}
