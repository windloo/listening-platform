package com.windloo.ai;
import com.windloo.ai.entity.Conversation;
import com.windloo.ai.mapper.ConversationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.import-check.enabled=false",
        "seata.enabled=false",
        "spring.cloud.sentinel.eager=false",
        "management.zipkin.tracing.enabled=false",
        "spring.ai.openai.api-key=${ARK_API_KEY:dummy}"
})
@AutoConfigureMockMvc
@Testcontainers
class AiIntegrationTest {
    @Container static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0").withDatabaseName("windloo_ai");
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

    @Autowired ConversationMapper conversationMapper;

    @Test void flyway_and_mapper_work() {
        Conversation c = new Conversation();
        c.setUserId(1L);
        c.setTitle("hello");
        conversationMapper.insert(c);
        assertNotNull(c.getId());
        Conversation got = conversationMapper.selectById(c.getId());
        assertNotNull(got);
        assertEquals("hello", got.getTitle());
        assertEquals(0, got.getIsDeleted());
    }
}