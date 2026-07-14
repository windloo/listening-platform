package com.windloo.listening.controller.main;
import com.windloo.common.api.JsonResponse;
import com.windloo.listening.entity.Album;
import com.windloo.listening.service.ListeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/listening/albums")
public class AlbumController {
    @Autowired ListeningService service;
    @GetMapping
    public JsonResponse<List<Album>> list(@RequestParam Long categoryId) { return JsonResponse.ok(service.getAlbumsByCategory(categoryId)); }
    @GetMapping("/{id}")
    public JsonResponse<Album> get(@PathVariable Long id) { return JsonResponse.ok(service.getAlbum(id)); }
}