package com.windloo.ai.controller;
import com.windloo.ai.dto.ChatRequest;
import com.windloo.ai.service.AiService;
import com.windloo.common.security.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
public class ChatController {
    private final AiService aiService;
    public ChatController(AiService s) { this.aiService = s; }

    public record ChatMeta(String conversationId, String messageId) {}
    public record ChatToken(String content) {}
    public record ChatError(int code, String msg) {}

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid ChatRequest req) {
        SseEmitter emitter = new SseEmitter(120_000L);
        Long userId = SecurityUtil.currentUserId();
        Long cid = req.conversationId() == null || req.conversationId().isBlank() ? null : Long.parseLong(req.conversationId());
        AiService.ChatStream stream = aiService.prepare(userId, cid, req.message());

        send(emitter, "meta", new ChatMeta(String.valueOf(stream.conversationId()), String.valueOf(stream.userMessageId())));
        StringBuilder acc = new StringBuilder();
        final Long convId = stream.conversationId();
        stream.tokens().subscribe(
                token -> { acc.append(token); send(emitter, "token", new ChatToken(token)); },
                err -> { send(emitter, "error", new ChatError(50010, "AI 调用失败")); emitter.completeWithError(err); },
                () -> { Long asstId = aiService.finish(convId, acc.toString()); send(emitter, "done", new ChatMeta(String.valueOf(convId), String.valueOf(asstId))); emitter.complete(); }
        );
        return emitter;
    }

    private void send(SseEmitter emitter, String name, Object data) {
        try { emitter.send(SseEmitter.event().name(name).data(data)); }
        catch (IOException e) { emitter.completeWithError(e); }
    }
}