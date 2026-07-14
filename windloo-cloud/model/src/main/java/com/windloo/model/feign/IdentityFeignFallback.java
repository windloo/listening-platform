package com.windloo.model.feign;
import com.windloo.model.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class IdentityFeignFallback implements IdentityFeignClient {
    @Override
    public UserDTO getUserById(Long id) {
        UserDTO u = new UserDTO();
        u.setId(id);
        u.setUserName("unknown");
        return u;
    }
}