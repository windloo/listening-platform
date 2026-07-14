package com.windloo.model.feign;
import com.windloo.model.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service", contextId = "identity-feign", fallback = IdentityFeignFallback.class)
public interface IdentityFeignClient {
    @GetMapping("/internal/user/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}