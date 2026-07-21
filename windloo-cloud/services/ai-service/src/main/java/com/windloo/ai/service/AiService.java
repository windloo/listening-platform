package com.windloo.ai.service;
import reactor.core.publisher.Flux;

public interface AiService {
    ChatStream prepare(Long userId, Long conversationId, String message);
    Long finish(Long conversationId, String assistantContent);

    record ChatStream(Long conversationId, Long userMessageId, Flux<String> tokens) {}
}