package com.windloo.listening.controller.admin;
import com.windloo.common.api.JsonResponse;
import com.windloo.listening.entity.Category;
import com.windloo.listening.service.ListeningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/listening/admin/categories")
public class CategoryAdminController {
    @Autowired ListeningService service;
    public record CatReq(@NotBlank String nameChinese, @NotBlank String nameEnglish, String coverUrl) {}

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<Category> create(@RequestBody @Valid CatReq req) {
        return JsonResponse.ok(service.createCategory(req.nameChinese(), req.nameEnglish(), req.coverUrl()));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<Category> update(@PathVariable Long id, @RequestBody @Valid CatReq req) {
        return JsonResponse.ok(service.updateCategory(id, req.nameChinese(), req.nameEnglish(), req.coverUrl()));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> delete(@PathVariable Long id) { service.deleteCategory(id); return JsonResponse.ok(); }
    @PutMapping("/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public JsonResponse<Void> sort(@RequestBody List<Long> sortedIds) { service.sortCategories(sortedIds); return JsonResponse.ok(); }
}