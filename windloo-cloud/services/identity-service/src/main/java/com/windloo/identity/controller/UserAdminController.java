package com.windloo.identity.controller;
import com.windloo.common.api.JsonResponse;
import com.windloo.common.api.PageResult;
import com.windloo.identity.service.IdentityService;
import com.windloo.model.dto.UserDTO;
import com.windloo.model.dto.IdentityStats;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    @Autowired IdentityService service;

    public record AddAdminReq(@NotBlank String userName, @NotBlank String phone, @NotBlank String role) {}

    @PostMapping("/user")
    public JsonResponse<String> add(@RequestBody @Valid AddAdminReq req) {
        return JsonResponse.ok(service.createUser(req.userName(), req.phone(), req.role()));
    }
    @PostMapping("/user/{id}/resetPassword")
    public JsonResponse<String> reset(@PathVariable Long id) {
        return JsonResponse.ok(service.resetPassword(id));
    }
    @DeleteMapping("/user/{id}")
    public JsonResponse<Void> delete(@PathVariable Long id) {
        service.softDeleteUser(id);
        return JsonResponse.ok();
    }
    @GetMapping("/stats")
    public JsonResponse<IdentityStats> stats() { return JsonResponse.ok(service.getStats()); }

    @GetMapping("/users")
    public JsonResponse<PageResult<UserDTO>> page(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size) {
        return JsonResponse.ok(service.pageUsers(page, size));
    }
}