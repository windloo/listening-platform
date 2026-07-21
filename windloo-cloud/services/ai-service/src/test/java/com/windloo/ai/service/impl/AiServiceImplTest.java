package com.windloo.ai.service.impl;
import com.windloo.ai.context.ContextProvider;
import com.windloo.ai.entity.Conversation;
import com.windloo.ai.entity.Message;
import com.windloo.ai.mapper.MessageMapper;
import com.windloo.ai.service.AiService;
import com.windloo.ai.service.ConversationService;
import com.windloo.common.exception.BizException;
import com.windloo.common.redis.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) ChatClient chatClient;
    @Mock ConversationService conversationService;
    @Mock MessageMapper messageMapper;
    @Mock ContextProvider contextProvider;
    @Mock RedisUtil redisUtil;
    AiServiceImpl service;

    @BeforeEach void setup() {
        service = new AiServiceImpl(chatClient, conversationService, messageMapper, contextProvider, redisUtil, 10, 0);
    }

    @Test void doPrepare_new_conversation_creates_with_truncated_title_and_saves_user_msg() {
        Conversation c = new Conversation(); c.setId(5L); c.setUserId(1L);
        when(conversationService.createConversation(eq(1L), anyString())).thenReturn(c);
        when(contextProvider.retrieve(anyString(), any(), any())).thenReturn(List.of());
        when(messageMapper.selectList(any())).thenReturn(List.of());

        AiServiceImpl.Prepared p = service.doPrepare(1L, null, "what is a noun?");

        verify(conversationService).createConversation(eq(1L), eq("what is a noun?"));
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper).insert(captor.capture());
        assertEquals("USER", captor.getValue().getRole());
        assertEquals("what is a noun?", captor.getValue().getContent());
        assertEquals(1, p.messages().size());
    }

    @Test void doPrepare_existing_conversation_requires_ownership() {
        Conversation c = new Conversation(); c.setId(7L); c.setUserId(1L); c.setTitle("t");
        when(conversationService.requireOwned(1L, 7L)).thenReturn(c);
        when(contextProvider.retrieve(anyString(), any(), any())).thenReturn(List.of());
        when(messageMapper.selectList(any())).thenReturn(List.of());

        AiServiceImpl.Prepared p = service.doPrepare(1L, 7L, "hi");

        verify(conversationService).requireOwned(1L, 7L);
        verify(conversationService, never()).createConversation(any(), any());
        assertEquals(7L, p.conversationId());
    }

    @Test void doPrepare_includes_history_and_context() {
        Conversation c = new Conversation(); c.setId(7L); c.setUserId(1L); c.setTitle("t");
        when(conversationService.requireOwned(1L, 7L)).thenReturn(c);
        Message hUser = new Message(); hUser.setRole("USER"); hUser.setContent("old q");
        Message hAsst = new Message(); hAsst.setRole("ASSISTANT"); hAsst.setContent("old a");
        when(messageMapper.selectList(any())).thenReturn(List.of(hUser, hAsst));
        when(contextProvider.retrieve(anyString(), any(), any()))
                .thenReturn(List.of(new ContextProvider.ContextChunk("ep1", "bg text")));

        AiServiceImpl.Prepared p = service.doPrepare(1L, 7L, "new q");

        assertEquals(4, p.messages().size());
    }

    @Test void doPrepare_enforces_daily_quota() {
        service = new AiServiceImpl(chatClient, conversationService, messageMapper, contextProvider, redisUtil, 10, 5);
        when(redisUtil.incr(anyString())).thenReturn(6L);
        assertThrows(BizException.class, () -> service.doPrepare(1L, null, "hi"));
        verify(conversationService, never()).createConversation(any(), any());
    }

    @Test void prepare_streams_tokens_from_chatClient() {
        Conversation c = new Conversation(); c.setId(5L); c.setUserId(1L);
        when(conversationService.createConversation(eq(1L), anyString())).thenReturn(c);
        when(contextProvider.retrieve(anyString(), any(), any())).thenReturn(List.of());
        when(messageMapper.selectList(any())).thenReturn(List.of());
        when(chatClient.prompt().messages(anyList()).stream().content()).thenReturn(Flux.just("Hel", "lo"));

        AiService.ChatStream s = service.prepare(1L, null, "hi");
        assertEquals(List.of("Hel", "lo"), s.tokens().collectList().block());
    }

    @Test void finish_saves_assistant_message_and_returns_id() {
        when(messageMapper.insert(any(Message.class))).thenAnswer(inv -> { ((Message) inv.getArgument(0)).setId(99L); return 1; });
        Long id = service.finish(7L, "a noun is a word");
        assertEquals(99L, id);
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper).insert(captor.capture());
        assertEquals("ASSISTANT", captor.getValue().getRole());
        assertEquals("a noun is a word", captor.getValue().getContent());
    }
}