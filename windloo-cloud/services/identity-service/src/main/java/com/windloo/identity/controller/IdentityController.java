package com.windloo.identity.controller;
import com.windloo.common.api.JsonResponse;
import com.windloo.identity.service.IdentityService;
import com.windloo.model.dto.UserDTO;
import com.windloo.common.security.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {
    @Autowired IdentityService service;

    @GetMapping("/me")
    public JsonResponse<UserDTO> me(Authentication auth) {
        Long userId = Long.parseLong((String) auth.getPrincipal());
        return JsonResponse.ok(service.findUserById(userId));
    }

    public record ProfileReq(@NotBlank String nickname, @NotBlank String phone) {}
    public record AvatarReq(@NotBlank String avatar) {}
    public record ChangePasswordReq(@NotBlank String oldPassword, @NotBlank String newPassword) {}

    @PutMapping("/profile")
    public JsonResponse<UserDTO> updateProfile(@RequestBody @Valid ProfileReq req) {
        Long me = SecurityUtil.currentUserId();
        return JsonResponse.ok(service.updateProfile(me, req.nickname(), req.phone()));
    }

    @PostMapping("/profile/password")
    public JsonResponse<Void> changePassword(@RequestBody @Valid ChangePasswordReq req) {
        Long me = SecurityUtil.currentUserId();
        service.changePassword(me, req.oldPassword(), req.newPassword());
        return JsonResponse.ok();
    }

    @PutMapping("/profile/avatar")
    public JsonResponse<UserDTO> updateAvatar(@RequestBody @Valid AvatarReq req) {
        Long me = SecurityUtil.currentUserId();
        return JsonResponse.ok(service.updateAvatar(me, req.avatar()));
    }
}