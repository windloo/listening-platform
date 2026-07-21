package com.windloo.ai.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windloo.ai.context.ContextProvider;
import com.windloo.ai.entity.Conversation;
import com.windloo.ai.entity.Message;
import com.windloo.ai.mapper.MessageMapper;
import com.windloo.ai.service.AiService;
import com.windloo.ai.service.ConversationService;
import com.windloo.common.exception.BizException;
import com.windloo.common.exception.ErrorCode;
import com.windloo.common.redis.RedisUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AiServiceImpl implements AiService {
    private final ChatClient chatClient;
    private final ConversationService conversationService;
    private final MessageMapper messageMapper;
    private final ContextProvider contextProvider;
    private final RedisUtil redisUtil;
    private final int maxMessages;
    private final int quotaDailyPerUser;

    public AiServiceImpl(ChatClient chatClient, ConversationService conversationService, MessageMapper messageMapper,
                         ContextProvider contextProvider, RedisUtil redisUtil,
                         @Value("${ai.context.max-messages:10}") int maxMessages,
                         @Value("${ai.quota.daily-per-user:0}") int quotaDailyPerUser) {
        this.chatClient = chatClient;
        this.conversationService = conversationService;
        this.messageMapper = messageMapper;
        this.contextProvider = contextProvider;
        this.redisUtil = redisUtil;
        this.maxMessages = maxMessages;
        this.quotaDailyPerUser = quotaDailyPerUser;
    }

    @Override
    public ChatStream prepare(Long userId, Long conversationId, Long episodeId, String message) {
        Prepared p = doPrepare(userId, conversationId, episodeId, message);
        Flux<String> tokens = chatClient.prompt().messages(p.messages()).stream().content();
        return new ChatStream(p.conversationId(), p.userMessageId(), tokens);
    }

    Prepared doPrepare(Long userId, Long conversationId, Long episodeId, String message) {
        checkQuota(userId);
        Conversation conv = conversationId == null
                ? conversationService.createConversation(userId, truncate(message, 30))
                : conversationService.requireOwned(userId, conversationId);

        List<Message> history = loadHistory(conv.getId());
        List<ContextProvider.ContextChunk> chunks = contextProvider.retrieve(message, userId, episodeId);

        Message userMsg = new Message();
        userMsg.setConversationId(conv.getId());
        userMsg.setRole("USER");
        userMsg.setContent(message);
        messageMapper.insert(userMsg);

        List<org.springframework.ai.chat.messages.Message> prompt = new ArrayList<>();
        if (!chunks.isEmpty()) {
            String ctx = chunks.stream().map(c -> "[" + c.source() + "] " + c.text())
                    .reduce("", (a, b) -> a + "\n" + b).strip();
            prompt.add(new SystemMessage("参考以下背景资料回答用户问题:\n" + ctx));
        }
        for (Message h : history) {
            if ("USER".equals(h.getRole())) prompt.add(new UserMessage(h.getContent()));
            else if ("ASSISTANT".equals(h.getRole())) prompt.add(new AssistantMessage(h.getContent()));
        }
        prompt.add(new UserMessage(message));
        return new Prepared(conv.getId(), userMsg.getId(), prompt);
    }

    private void checkQuota(Long userId) {
        if (quotaDailyPerUser <= 0) return;
        String key = "ai:quota:" + userId + ":" + LocalDate.now();
        long count = redisUtil.incr(key);
        if (count == 1L) redisUtil.expire(key, secondsUntilEndOfDay());
        if (count > quotaDailyPerUser) throw new BizException(ErrorCode.AI_QUOTA_EXCEEDED);
    }

    private long secondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, now.toLocalDate().atTime(23, 59, 59)).getSeconds();
    }

    private List<Message> loadHistory(Long conversationId) {
        List<Message> desc = new ArrayList<>(messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT " + Math.max(maxMessages, 1))));
        Collections.reverse(desc);
        return desc;
    }

    private String truncate(String s, int n) {
        if (s == null) return "新对话";
        return s.length() <= n ? s : s.substring(0, n);
    }

    @Override
    public Long finish(Long conversationId, String assistantContent) {
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setRole("ASSISTANT");
        m.setContent(assistantContent);
        messageMapper.insert(m);
        return m.getId();
    }

    record Prepared(Long conversationId, Long userMessageId,
                    List<org.springframework.ai.chat.messages.Message> messages) {}
}