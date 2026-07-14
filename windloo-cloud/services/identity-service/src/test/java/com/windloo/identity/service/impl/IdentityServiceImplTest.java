package com.windloo.identity.service.impl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windloo.common.api.PageResult;
import com.windloo.common.exception.BizException;
import com.windloo.common.exception.ErrorCode;
import com.windloo.common.redis.RedisUtil;
import com.windloo.identity.entity.User;
import com.windloo.identity.entity.UserRole;
import com.windloo.identity.event.PasswordResetEvent;
import com.windloo.identity.event.ResetCodeEvent;
import com.windloo.identity.event.UserCreatedEvent;
import com.windloo.identity.mapper.RoleMapper;
import com.windloo.identity.mapper.UserMapper;
import com.windloo.identity.mapper.UserRoleMapper;
import com.windloo.identity.security.JwtService;
import com.windloo.model.dto.UserDTO;
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
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityServiceImplTest {
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

    @Test void createUser_inserts_with_admin_role_and_returns_10char_password() {
        when(userMapper.exists(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        String pwd = service.createUser("bob", "13800000000", "ADMIN");
        assertNotNull(pwd); assertEquals(10, pwd.length());
        verify(userMapper).insert(any(User.class));
        ArgumentCaptor<UserRole> ur = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleMapper).insert(ur.capture());
        assertEquals(2L, ur.getValue().getRoleId());
        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }

    @Test void createUser_duplicate_throws_user_exists() {
        when(userMapper.exists(any())).thenReturn(true);
        BizException ex = assertThrows(BizException.class, () -> service.createUser("bob", "138", "ADMIN"));
        assertEquals(ErrorCode.USER_EXISTS.getCode(), ex.getCode());
    }

    @Test void resetPassword_updates_hash_unlocks_and_publishes() {
        User u = new User(); u.setId(9L); u.setPhone("138"); u.setPasswordHash("old");
        when(userMapper.selectById(9L)).thenReturn(u);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        assertNotNull(service.resetPassword(9L));
        ArgumentCaptor<User> c = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(c.capture());
        assertEquals("encoded", c.getValue().getPasswordHash());
        assertFalse(c.getValue().getIsLocked());
        verify(eventPublisher).publishEvent(any(PasswordResetEvent.class));
    }

    @Test void resetPassword_missing_user_throws() {
        when(userMapper.selectById(9L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.resetPassword(9L));
    }

    @Test void softDeleteUser_uses_wrapper_update_to_clear_name_and_set_deleted() {
        User u = new User(); u.setId(9L);
        when(userMapper.selectById(9L)).thenReturn(u);
        service.softDeleteUser(9L);
        verify(userMapper).update(isNull(), any());
    }

    @Test void sendResetCode_no_user_is_silent_noop() {
        when(userMapper.selectOne(any())).thenReturn(null);
        service.sendResetCode("139");
        verify(redisUtil, never()).set(anyString(), anyString(), anyLong());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test void sendResetCode_stores_code_with_ttl_and_publishes() {
        User u = new User(); u.setPhone("139");
        when(userMapper.selectOne(any())).thenReturn(u);
        service.sendResetCode("139");
        verify(redisUtil).set(eq("resetcode:139"), anyString(), eq(300L));
        verify(eventPublisher).publishEvent(any(ResetCodeEvent.class));
    }

    @Test void resetByCode_short_password_throws_password_invalid() {
        assertThrows(BizException.class, () -> service.resetByCode("139", "x", "123"));
    }

    @Test void resetByCode_wrong_code_throws_code_invalid() {
        when(redisUtil.get("resetcode:139")).thenReturn("000000");
        BizException ex = assertThrows(BizException.class, () -> service.resetByCode("139", "111111", "newpwd123"));
        assertEquals(ErrorCode.CODE_INVALID.getCode(), ex.getCode());
    }

    @Test void resetByCode_valid_resets_password_and_deletes_code() {
        when(redisUtil.get("resetcode:139")).thenReturn("123456");
        User u = new User(); u.setId(1L); u.setPhone("139");
        when(userMapper.selectOne(any())).thenReturn(u);
        when(passwordEncoder.encode("newpwd123")).thenReturn("enc");
        service.resetByCode("139", "123456", "newpwd123");
        ArgumentCaptor<User> c = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(c.capture());
        assertEquals("enc", c.getValue().getPasswordHash());
        verify(redisUtil).delete("resetcode:139");
    }

    // @Test void initAdmin_when_none_creates_with_both_roles() {
    //     when(userMapper.exists(any())).thenReturn(false);
    //     when(passwordEncoder.encode(any())).thenReturn("enc");
    //     service.initAdmin();
    //     verify(userMapper).insert(any(User.class));
    //     verify(userRoleMapper, times(2)).insert(any(UserRole.class));
    // }

    // @Test void initAdmin_when_exists_throws_init_done() {
    //     when(userMapper.exists(any())).thenReturn(true);
    //     BizException ex = assertThrows(BizException.class, () -> service.initAdmin());
    //     assertEquals(ErrorCode.INIT_DONE.getCode(), ex.getCode());
    // }

    @Test void findUserById_maps_to_dto() {
        User u = new User(); u.setId(1L); u.setUserName("a"); u.setPhone("p"); u.setCreateTime(LocalDateTime.now());
        when(userMapper.selectById(1L)).thenReturn(u);
        UserDTO dto = service.findUserById(1L);
        assertEquals("a", dto.getUserName());
    }

    @Test void findUserById_null_when_missing() {
        when(userMapper.selectById(1L)).thenReturn(null);
        assertNull(service.findUserById(1L));
    }

    @Test void pageUsers_returns_mapped_page() {
        when(userMapper.selectPage(any(), isNull())).thenAnswer(inv -> {
            Page<User> p = inv.getArgument(0);
            User u = new User(); u.setId(1L); u.setUserName("a");
            p.setRecords(List.of(u)); p.setTotal(1L); p.setCurrent(1L); p.setSize(10L);
            return p;
        });
        PageResult<UserDTO> r = service.pageUsers(1, 10);
        assertEquals(1, r.getList().size());
        assertEquals(1L, r.getTotal());
    }
}