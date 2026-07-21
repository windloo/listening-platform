package com.windloo.ai;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.import-check.enabled=false",
        "seata.enabled=false",
        "spring.cloud.sentinel.eager=false",
        "management.zipkin.tracing.enabled=false",
        "spring.flyway.enabled=false",
        "spring.ai.openai.api-key=${ARK_API_KEY:dummy}"
})
class ChatClientSmokeTest {
    @Autowired ChatClient.Builder builder;

    @Test
    void calls_volcengine_and_returns_text() {
        String key = System.getenv("ARK_API_KEY");
        Assumptions.assumeTrue(key != null && !key.isBlank(), "ARK_API_KEY not set; skipping spike");
        String reply = builder.build().prompt().user("Say hi in one word.").call().content();
        assertNotNull(reply);
        assertFalse(reply.isBlank());
    }
}