package com.windloo.model.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String userName;
    private String phone;
    private LocalDateTime creationTime;
    private List<String> roles;
    private String avatar;
    private String nickname;
}