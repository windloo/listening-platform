package com.windloo.ai.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {
    @Value("${ai.system-prompt}") private String systemPrompt;

    @Bean
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.base-url}") String baseUrl,
                               @Value("${spring.ai.openai.api-key}") String apiKey) {
        // 火山引擎 Ark 的 OpenAI 兼容端点用 /v3 版本(如 .../api/coding/v3),
        // 而 Spring AI 默认 completionsPath 是 /v1/chat/completions,
        // 拼出来是 .../v3/v1/chat/completions -> 404。改用 /chat/completions(不带 /v1)。
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .completionsPath("/chat/completions")
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(systemPrompt).build();
    }
}