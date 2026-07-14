package com.windloo.search.controller;
import com.windloo.common.api.JsonResponse;
import com.windloo.common.api.PageResult;
import com.windloo.model.dto.EpisodeIndexDTO;
import com.windloo.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Autowired SearchService service;

    @GetMapping("/episodes")
    public JsonResponse<PageResult<EpisodeIndexDTO>> search(@RequestParam String keyword,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return JsonResponse.ok(service.search(keyword, page, size));
    }
}