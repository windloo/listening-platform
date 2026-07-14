package com.windloo.listening.controller.admin;
import com.windloo.common.api.JsonResponse;
import com.windloo.listening.service.ListeningService;
import com.windloo.model.dto.ListeningStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listening/admin")
public class StatsController {
    @Autowired ListeningService service;
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public JsonResponse<ListeningStats> stats() { return JsonResponse.ok(service.getStats()); }
}