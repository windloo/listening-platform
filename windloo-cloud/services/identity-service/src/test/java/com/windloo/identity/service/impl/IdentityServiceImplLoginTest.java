package com.windloo.identity.service.impl;
import com.windloo.common.exception.BizException;
import com.windloo.common.exception.ErrorCode;
import com.windloo.identity.entity.User;
import com.windloo.identity.mapper.RoleMapper;
import com.windloo.identity.mapper.UserMapper;
import com.windloo.identity.mapper.UserRoleMapper;
import com.windloo.identity.security.JwtService;
import com.windloo.common.redis.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityServiceImplLoginTest {
    @Mock UserMapper userMapper;
    @Mock RoleMapper roleMapper;
    @Mock UserRoleMapper userRoleMapper;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock RedisUtil redisUtil;
    @InjectMocks IdentityServiceImpl service;

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "maxAttempts", 5);
        ReflectionTestUtils.setField(service, "lockMinutes", 15);
        ReflectionTestUtils.setField(service, "resetCodeTtl", 300L);
    }

    @Test void login_success_returns_token() {
        User u = user(1L, "ALICE", "hash", 0, false);
        when(userMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);
        when(userRoleMapper.selectList(any())).thenReturn(List.of());
        when(jwtService.issue(1L, List.of())).thenReturn("TOKEN");
        assertEquals("TOKEN", service.loginByUserName("alice", "pw"));
    }

    @Test void login_wrong_password_increments_failure() {
        User u = user(1L, "ALICE", "hash", 3, false);
        when(userMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches("pw", "hash")).thenReturn(false);
        BizException ex = assertThrows(BizException.class, () -> service.loginByUserName("alice", "pw"));
        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), ex.getCode());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(4, captor.getValue().getAccessFailedCount());
    }

    @Test void login_locks_after_max_attempts() {
        User u = user(1L, "ALICE", "hash", 4, false);
        when(userMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        assertThrows(BizException.class, () -> service.loginByUserName("alice", "pw"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertTrue(captor.getValue().getIsLocked());
    }

    @Test void login_unknown_user_throws_login_failed() {
        when(userMapper.selectOne(any())).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> service.loginByUserName("nobody", "pw"));
        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), ex.getCode());
        verify(userMapper, never()).updateById(any(User.class));
    }

    private User user(long id, String norm, String hash, int failed, boolean locked) {
        User u = new User();
        u.setId(id); u.setNormalizedUserName(norm); u.setPasswordHash(hash);
        u.setAccessFailedCount(failed); u.setIsLocked(locked);
        return u;
    }
}