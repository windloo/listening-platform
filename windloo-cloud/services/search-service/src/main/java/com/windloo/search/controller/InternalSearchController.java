package com.windloo.search.controller;
import com.windloo.model.dto.EpisodeIndexDTO;
import com.windloo.model.dto.SentenceDTO;
import java.util.List;
import com.windloo.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/search")
public class InternalSearchController {
    @Autowired SearchService service;

    @PostMapping("/episodes")
    public void upsert(@RequestBody EpisodeIndexDTO dto) { service.upsert(dto); }

    @DeleteMapping("/episodes/{id}")
    public void remove(@PathVariable Long id) { service.remove(id); }

    @GetMapping("/episodes/{episodeId}/sentences")
    public List<SentenceDTO> sentences(@PathVariable Long episodeId,
                                       @RequestParam String keyword,
                                       @RequestParam(defaultValue = "5") int size) {
        return service.searchInEpisode(episodeId, keyword, size);
    }
}