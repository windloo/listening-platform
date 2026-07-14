package com.windloo.listening.controller.main;
import com.windloo.common.api.JsonResponse;
import com.windloo.listening.entity.Episode;
import com.windloo.listening.service.ListeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/listening/episodes")
public class EpisodeController {
    @Autowired ListeningService service;
    @GetMapping
    public JsonResponse<List<Episode>> list(@RequestParam Long albumId) {
        List<Episode> eps = service.getEpisodesByAlbum(albumId);
        eps.forEach(e -> e.setSubtitle(null));
        return JsonResponse.ok(eps);
    }
    @GetMapping("/{id}")
    public JsonResponse<Episode> get(@PathVariable Long id) { return JsonResponse.ok(service.getEpisode(id)); }
}