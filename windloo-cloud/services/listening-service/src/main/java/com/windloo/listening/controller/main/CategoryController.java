package com.windloo.listening.controller.main;
import com.windloo.common.api.JsonResponse;
import com.windloo.listening.entity.Category;
import com.windloo.listening.service.ListeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/listening/categories")
public class CategoryController {
    @Autowired ListeningService service;
    @GetMapping
    public JsonResponse<List<Category>> list() { return JsonResponse.ok(service.getCategories()); }
    @GetMapping("/{id}")
    public JsonResponse<Category> get(@PathVariable Long id) { return JsonResponse.ok(service.getCategory(id)); }
}