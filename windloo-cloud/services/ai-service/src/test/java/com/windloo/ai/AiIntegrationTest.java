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
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @Autowired com.windloo.ai.service.ConversationService conversationService;
    @org.springframework.boot.test.mock.mockito.MockBean(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    org.springframework.ai.chat.client.ChatClient chatClient;

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

    @Test
    void conversation_service_crud_and_ownership() {
        Long me = 1L, other = 2L;
        com.windloo.ai.entity.Conversation c = conversationService.createConversation(me, "我的会话");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.windloo.ai.entity.Conversation> page =
                conversationService.listConversations(me, 1, 20);
        assertTrue(page.getRecords().stream().anyMatch(x -> x.getId().equals(c.getId())));
        conversationService.listMessages(me, c.getId());
        assertThrows(com.windloo.common.exception.BizException.class,
                () -> conversationService.listMessages(other, c.getId()));
        conversationService.renameConversation(me, c.getId(), "改名");
        conversationService.deleteConversation(me, c.getId());
        assertNull(conversationMapper.selectById(c.getId()));
    }

    @Test
    void http_conversation_endpoints_require_auth_and_work() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/ai/conversations"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/ai/conversations")
                        .header(com.windloo.common.security.SecurityConstants.HEADER_USER_ID, "1")
                        .header(com.windloo.common.security.SecurityConstants.HEADER_USER_ROLES, "[USER]"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value(0));
    }

    @Test
    void chat_requires_auth() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/ai/chat")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hi\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void chat_streams_sse_events() throws Exception {
        org.mockito.Mockito.when(chatClient.prompt().messages(org.mockito.ArgumentMatchers.anyList()).stream().content())
                .thenReturn(reactor.core.publisher.Flux.just("Hel", "lo"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/ai/chat")
                        .header(com.windloo.common.security.SecurityConstants.HEADER_USER_ID, "1")
                        .header(com.windloo.common.security.SecurityConstants.HEADER_USER_ROLES, "[USER]")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hi\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentTypeCompatibleWith(org.springframework.http.MediaType.TEXT_EVENT_STREAM))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("meta")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("token")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("Hel")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("done")));
    }
}