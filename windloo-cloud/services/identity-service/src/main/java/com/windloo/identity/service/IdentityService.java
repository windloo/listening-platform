package com.windloo.identity.service;
import com.windloo.common.api.PageResult;
import com.windloo.model.dto.UserDTO;
import com.windloo.model.dto.IdentityStats;
import java.util.List;

public interface IdentityService {
    String loginByUserName(String userName, String password);
    String loginByPhone(String phone, String password);
    UserDTO findUserById(Long id);
    PageResult<UserDTO> pageUsers(long page, long size);
    UserDTO updateProfile(Long userId, String nickname, String phone);
    UserDTO updateAvatar(Long userId, String avatar);
    void changePassword(Long userId, String oldPassword, String newPassword);
    String createUser(String userName, String phone, String role);
    String resetPassword(Long userId);
    void softDeleteUser(Long userId);
    void sendResetCode(String phone);
    void resetByCode(String phone, String code, String newPassword);
//    void initAdmin();
    List<String> getRoleCodes(Long userId);
    IdentityStats getStats();
}
