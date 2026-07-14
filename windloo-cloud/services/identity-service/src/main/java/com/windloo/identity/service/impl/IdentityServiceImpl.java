package com.windloo.identity.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windloo.common.api.PageResult;
import com.windloo.common.exception.BizException;
import com.windloo.common.exception.ErrorCode;
import com.windloo.common.redis.RedisUtil;
import com.windloo.identity.entity.Role;
import com.windloo.identity.entity.User;
import com.windloo.identity.entity.UserRole;
import com.windloo.identity.event.PasswordResetEvent;
import com.windloo.identity.event.ResetCodeEvent;
import com.windloo.identity.event.UserCreatedEvent;
import com.windloo.identity.mapper.RoleMapper;
import com.windloo.identity.mapper.UserMapper;
import com.windloo.identity.mapper.UserRoleMapper;
import com.windloo.identity.security.JwtService;
import com.windloo.identity.service.IdentityService;
import com.windloo.model.dto.UserDTO;
import com.windloo.model.dto.IdentityStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class IdentityServiceImpl implements IdentityService {
    private static final String RESET_CODE_PREFIX = "resetcode:";
    private static final long ADMIN_ROLE_ID = 2L;
    private static final long USER_ROLE_ID = 1L;
    private static final String PW_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$%";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisUtil redisUtil;

    @Value("${identity.lockout.max-attempts:5}") private int maxAttempts;
    @Value("${identity.lockout.lock-minutes:15}") private int lockMinutes;
    @Value("${identity.reset-code.ttl-seconds:300}") private long resetCodeTtl;

    public IdentityServiceImpl(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper,
                               PasswordEncoder passwordEncoder, JwtService jwtService,
                               ApplicationEventPublisher eventPublisher, RedisUtil redisUtil) {
        this.userMapper = userMapper; this.roleMapper = roleMapper; this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
        this.eventPublisher = eventPublisher; this.redisUtil = redisUtil;
    }

    @Override
    public String loginByUserName(String userName, String password) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getNormalizedUserName, normalize(userName)));
        return doLogin(u, password);
    }

    @Override
    public String loginByPhone(String phone, String password) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        return doLogin(u, password);
    }

    private String doLogin(User user, String password) {
        if (user == null) throw new BizException(ErrorCode.LOGIN_FAILED);
        if (Boolean.TRUE.equals(user.getIsLocked())
                && user.getLockoutEnd() != null && user.getLockoutEnd().isAfter(LocalDateTime.now())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            user.setIsLocked(false); user.setAccessFailedCount(0); user.setLockoutEnd(null);
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            registerFailure(user);
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        user.setAccessFailedCount(0); user.setIsLocked(false); user.setLockoutEnd(null);
        userMapper.updateById(user);
        return jwtService.issue(user.getId(), getRoleCodes(user.getId()));
    }

    private void registerFailure(User user) {
        int count = (user.getAccessFailedCount() == null ? 0 : user.getAccessFailedCount()) + 1;
        user.setAccessFailedCount(count);
        if (count >= maxAttempts) {
            user.setIsLocked(true);
            user.setLockoutEnd(LocalDateTime.now().plusMinutes(lockMinutes));
        }
        userMapper.updateById(user);
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        List<UserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (urs.isEmpty()) return List.of();
        List<Long> roleIds = urs.stream().map(UserRole::getRoleId).toList();
        return roleMapper.selectBatchIds(roleIds).stream().map(Role::getCode).toList();
    }

    @Override
    public UserDTO findUserById(Long id) {
        User u = userMapper.selectById(id);
        return u == null ? null : toDTO(u);
    }

    @Override
    public PageResult<UserDTO> pageUsers(long page, long size) {
        Page<User> p = new Page<>(page, size);
        userMapper.selectPage(p, null);
        return new PageResult<>(p.getRecords().stream().map(this::toDTO).toList(),
                p.getTotal(), p.getCurrent(), p.getSize());
    }

    @Override
    public String createUser(String userName, String phone, String role) {
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getNormalizedUserName, normalize(userName))))
            throw new BizException(ErrorCode.USER_EXISTS);
        User u = new User();
        u.setUserName(userName); u.setNormalizedUserName(normalize(userName)); u.setPhone(phone); u.setNickname(userName);
        String password = generateSecurePassword();
        u.setPasswordHash(passwordEncoder.encode(password));
        userMapper.insert(u);
        long roleId = "ADMIN".equalsIgnoreCase(role) ? ADMIN_ROLE_ID : USER_ROLE_ID;
        assignRole(u.getId(), roleId);
        eventPublisher.publishEvent(new UserCreatedEvent(u.getId(), phone, password));
        return password;
    }

    @Override
    public UserDTO updateProfile(Long userId, String nickname, String phone) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(ErrorCode.USER_NOT_FOUND);
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, phone).ne(User::getId, userId)))
            throw new BizException(ErrorCode.USER_EXISTS);
        u.setNickname(nickname); u.setPhone(phone);
        userMapper.updateById(u);
        return toDTO(u);
    }

    @Override
    public UserDTO updateAvatar(Long userId, String avatar) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(ErrorCode.USER_NOT_FOUND);
        u.setAvatar(avatar);
        userMapper.updateById(u);
        return toDTO(u);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) throw new BizException(ErrorCode.PASSWORD_INVALID);
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(ErrorCode.USER_NOT_FOUND);
        if (!passwordEncoder.matches(oldPassword, u.getPasswordHash()))
            throw new BizException(ErrorCode.PASSWORD_INVALID.getCode(), "旧密码错误");
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(u);
    }

    @Override
    public String resetPassword(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(ErrorCode.USER_NOT_FOUND);
        String password = generateSecurePassword();
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setIsLocked(false); u.setAccessFailedCount(0); u.setLockoutEnd(null);
        userMapper.updateById(u);
        eventPublisher.publishEvent(new PasswordResetEvent(u.getId(), u.getPhone(), password));
        return password;
    }

    @Override
    public void softDeleteUser(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new BizException(ErrorCode.USER_NOT_FOUND);
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", userId)
                .set("is_deleted", 1)
                .set("deletion_time", LocalDateTime.now())
                .set("normalized_user_name", null));
    }

    @Override
    public void sendResetCode(String phone) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (u == null) return;
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        redisUtil.set(RESET_CODE_PREFIX + phone, code, resetCodeTtl);
        eventPublisher.publishEvent(new ResetCodeEvent(phone, code));
    }

    @Override
    public void resetByCode(String phone, String code, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) throw new BizException(ErrorCode.PASSWORD_INVALID);
        String stored = redisUtil.get(RESET_CODE_PREFIX + phone);
        if (stored == null || !stored.equals(code)) throw new BizException(ErrorCode.CODE_INVALID);
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (u == null) throw new BizException(ErrorCode.USER_NOT_FOUND);
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        u.setIsLocked(false); u.setAccessFailedCount(0);
        userMapper.updateById(u);
        redisUtil.delete(RESET_CODE_PREFIX + phone);
    }

//    public void initAdmin() {
//        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getNormalizedUserName, normalize("test")))) {
//            throw new BizException(ErrorCode.INIT_DONE);
//        }
//        User u = new User();
//        u.setUserName("test"); u.setNormalizedUserName(normalize("test")); u.setPhone("00000000000");
//        String password = "123456";
//        u.setPasswordHash(passwordEncoder.encode(password));
//        userMapper.insert(u);
//        assignRole(u.getId(), ADMIN_ROLE_ID);
//        assignRole(u.getId(), USER_ROLE_ID);
//        log.warn("[INIT] default admin user 'test' created with password '123456'");
//    }

    private void assignRole(Long userId, Long roleId) {
        UserRole ur = new UserRole();
        ur.setUserId(userId); ur.setRoleId(roleId);
        userRoleMapper.insert(ur);
    }

    private String generateSecurePassword() {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 10) sb.append(PW_CHARS.charAt(rnd.nextInt(PW_CHARS.length())));
        return sb.toString();
    }

    private UserDTO toDTO(User u) {
        UserDTO d = new UserDTO();
        d.setId(u.getId()); d.setUserName(u.getUserName()); d.setPhone(u.getPhone()); d.setCreationTime(u.getCreateTime());
        d.setRoles(getRoleCodes(u.getId()));
        d.setAvatar(u.getAvatar());
        d.setNickname(u.getNickname());
        return d;
    }

    @Override
    public IdentityStats getStats() {
        long total = userMapper.selectCount(null);
        long adminCount = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, ADMIN_ROLE_ID));
        return new IdentityStats(total, adminCount, total - adminCount);
    }
    private String normalize(String s) { return s == null ? null : s.toUpperCase(); }
}
