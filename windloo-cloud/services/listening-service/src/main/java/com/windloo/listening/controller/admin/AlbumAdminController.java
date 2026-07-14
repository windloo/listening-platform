package com.windloo.listening.controller.admin;
import com.windloo.common.api.JsonResponse;
import com.windloo.listening.entity.Album;
import com.windloo.listening.service.ListeningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/listening/admin/albums")
public class AlbumAdminController {
    @Autowired ListeningService service;
    public record AlbumReq(@NotBlank String nameChinese, @NotBlank String nameEnglish, @NotNull Long categoryId) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<List<Album>> list(@RequestParam Long categoryId) {
        return JsonResponse.ok(service.getAlbumsByCategoryAll(categoryId));
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<Album> create(@RequestBody @Valid AlbumReq req) {
        return JsonResponse.ok(service.createAlbum(req.nameChinese(), req.nameEnglish(), req.categoryId()));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<Album> update(@PathVariable Long id, @RequestBody @Valid AlbumReq req) {
        return JsonResponse.ok(service.updateAlbum(id, req.nameChinese(), req.nameEnglish(), req.categoryId()));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> delete(@PathVariable Long id) { service.deleteAlbum(id); return JsonResponse.ok(); }
    @PutMapping("/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> sort(@RequestBody List<Long> sortedIds) { service.sortAlbums(sortedIds); return JsonResponse.ok(); }
    @PutMapping("/{id}/show")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> show(@PathVariable Long id) { service.showAlbum(id); return JsonResponse.ok(); }
    @PutMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> hide(@PathVariable Long id) { service.hideAlbum(id); return JsonResponse.ok(); }
}