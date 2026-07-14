package com.windloo.identity.controller;
import com.windloo.common.api.JsonResponse;
import com.windloo.identity.service.IdentityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity")
public class LoginController {
    @Autowired IdentityService service;

    public record LoginByUserNameReq(@NotBlank String userName, @NotBlank String password) {}
    public record LoginByPhoneReq(@NotBlank String phone, @NotBlank String password) {}
    public record SendCodeReq(@NotBlank String phone) {}
    public record ResetByCodeReq(@NotBlank String phone, @NotBlank String code, @NotBlank String newPassword) {}

    @PostMapping("/login/byUserName")
    public JsonResponse<String> byUserName(@RequestBody @Valid LoginByUserNameReq req) {
        return JsonResponse.ok(service.loginByUserName(req.userName(), req.password()));
    }
    @PostMapping("/login/byPhone")
    public JsonResponse<String> byPhone(@RequestBody @Valid LoginByPhoneReq req) {
        return JsonResponse.ok(service.loginByPhone(req.phone(), req.password()));
    }
    @PostMapping("/forgot/sendCode")
    public JsonResponse<Void> sendCode(@RequestBody @Valid SendCodeReq req) {
        service.sendResetCode(req.phone());
        return JsonResponse.ok();
    }
    @PostMapping("/forgot/reset")
    public JsonResponse<Void> reset(@RequestBody @Valid ResetByCodeReq req) {
        service.resetByCode(req.phone(), req.code(), req.newPassword());
        return JsonResponse.ok();
    }

//    @PostMapping("/init")
//    public JsonResponse<String> init() {
//        service.initAdmin();
//        return JsonResponse.ok("默认账号已创建：用户名 test，密码 123456");
//    }
}