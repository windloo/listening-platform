package com.windloo.listening.controller;
import com.windloo.listening.service.ListeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/listening")
public class InternalEpisodeController {
    @Autowired ListeningService service;

    @PutMapping("/episodes/{id}/audioUrl")
    public void updateAudioUrl(@PathVariable Long id, @RequestParam String audioUrl) {
        service.updateAudioUrl(id, audioUrl);
    }
}