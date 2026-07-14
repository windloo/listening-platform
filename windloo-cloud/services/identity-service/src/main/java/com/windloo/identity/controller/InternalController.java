package com.windloo.identity.controller;
import com.windloo.identity.service.IdentityService;
import com.windloo.model.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class InternalController {
    @Autowired IdentityService service;

    @GetMapping("/internal/user/{id}")
    public UserDTO internalGet(@PathVariable Long id) {
        return service.findUserById(id);
    }
}