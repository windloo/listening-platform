package com.windloo.listening;
import com.windloo.common.security.SecurityConstants;
import com.windloo.listening.entity.Category;
import com.windloo.listening.entity.Episode;
import com.windloo.listening.service.ListeningService;
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
import static org.junit.jupiter.api.Assertions.*;
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
class ListeningIntegrationTest {
    @Container static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0").withDatabaseName("windloo_listening");
    @Container static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired MockMvc mvc;
    @Autowired ListeningService service;

    @Test void crud_full_flow() {
        Category c = service.createCategory("中文分类", "EnglishCat", null);
        var a = service.createAlbum("中文专辑", "EnglishAlbum", c.getId());
        Episode e = service.createEpisode("中文单集", "EnglishEp", a.getId(),
                "http://example.com/a.m4a", 120.0,
                "1\n00:00:01,000 --> 00:00:03,000\nHello\n", "srt");
        assertNotNull(e.getId());
        assertEquals(1, service.parseSubtitle(e.getId()).size());
        assertEquals(1, service.getEpisodesByAlbum(a.getId()).size());
        service.hideEpisode(e.getId());
        assertEquals(0, service.getEpisodesByAlbum(a.getId()).size());
        service.deleteEpisode(e.getId());
        assertNull(service.getEpisode(e.getId()));
    }

    @Test void cache_invalidated_after_update() {
        Category c = service.createCategory("原中文", "OrigEn", null);
        service.getCategory(c.getId());
        service.updateCategory(c.getId(), "新中文", "NewEn", null);
        Category again = service.getCategory(c.getId());
        assertEquals("新中文", again.getNameChinese());
    }

    @Test void non_admin_cannot_access_admin_endpoint() throws Exception {
        mvc.perform(get("/api/listening/admin/categories")
                        .header(SecurityConstants.HEADER_USER_ID, "1")
                        .header(SecurityConstants.HEADER_USER_ROLES, "[USER]"))
                .andExpect(status().isForbidden());
    }

    @Test void main_read_is_public() throws Exception {
        mvc.perform(get("/api/listening/categories")).andExpect(status().isOk());
    }
}