package com.windloo.identity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windloo.common.security.SecurityConstants;
import com.windloo.identity.entity.User;
import com.windloo.identity.mapper.UserMapper;
import com.windloo.identity.service.IdentityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.import-check.enabled=false",
        "seata.enabled=false",
        "spring.cloud.sentinel.eager=false",
        "management.zipkin.tracing.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers
class IdentityIntegrationTest {
    @Container static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0").withDatabaseName("windloo_identity");
    @Container static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        r.add("jwt.secret", () -> "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
    }

    @Autowired MockMvc mvc;
    @Autowired IdentityService service;
    @Autowired UserMapper userMapper;

    @Test void init_is_one_shot() throws Exception {
        mvc.perform(post("/api/identity/init")).andExpect(status().isOk());
        mvc.perform(post("/api/identity/init")).andExpect(status().isBadRequest());
    }

    @Test void createAdmin_login_me_flow() throws Exception {
        String pwd = service.createUser("dave", "13600000001", "ADMIN");
        String token = service.loginByUserName("dave", pwd);
        org.junit.jupiter.api.Assertions.assertNotNull(token);
        Long daveId = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getNormalizedUserName, "DAVE")).getId();
        mvc.perform(get("/api/identity/me")
                        .header(SecurityConstants.HEADER_USER_ID, String.valueOf(daveId))
                        .header(SecurityConstants.HEADER_USER_ROLES, "[ADMIN]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("dave"));
    }

    @Test void softDelete_then_recreate_same_name_succeeds() {
        service.createUser("carol", "13700000001", "ADMIN");
        User carol = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getNormalizedUserName, "CAROL"));
        service.softDeleteUser(carol.getId());
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.createUser("carol", "13700000002", "ADMIN"));
    }

    @Test void non_admin_cannot_access_admin_endpoint() throws Exception {
        mvc.perform(get("/api/identity/admin/users")
                        .header(SecurityConstants.HEADER_USER_ID, "1")
                        .header(SecurityConstants.HEADER_USER_ROLES, "[USER]"))
                .andExpect(status().isForbidden());
    }
}