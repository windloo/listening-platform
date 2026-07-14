package com.windloo.search.controller;
import com.windloo.model.dto.EpisodeIndexDTO;
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
}