package com.windloo.ai.service.impl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windloo.ai.entity.Conversation;
import com.windloo.ai.mapper.ConversationMapper;
import com.windloo.ai.mapper.MessageMapper;
import com.windloo.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {
    @Mock ConversationMapper conversationMapper;
    @Mock MessageMapper messageMapper;
    @InjectMocks ConversationServiceImpl service;

    @Test void createConversation_defaults_title_when_blank() {
        Conversation c = service.createConversation(1L, "   ");
        assertEquals("新对话", c.getTitle());
        assertEquals(1L, c.getUserId());
        verify(conversationMapper).insert(any(Conversation.class));
    }

    @Test void requireOwned_throws_when_not_owner() {
        Conversation c = new Conversation(); c.setUserId(2L);
        when(conversationMapper.selectById(10L)).thenReturn(c);
        assertThrows(BizException.class, () -> service.requireOwned(1L, 10L));
    }

    @Test void requireOwned_throws_when_not_found() {
        when(conversationMapper.selectById(10L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.requireOwned(1L, 10L));
    }

    @Test void deleteConversation_soft_deletes_conversation_and_messages() {
        Conversation c = new Conversation(); c.setId(10L); c.setUserId(1L);
        when(conversationMapper.selectById(10L)).thenReturn(c);
        service.deleteConversation(1L, 10L);
        verify(conversationMapper).update(isNull(), any());
        verify(messageMapper).update(isNull(), any());
    }

    @Test void listMessages_requires_ownership_then_queries() {
        Conversation c = new Conversation(); c.setId(10L); c.setUserId(1L);
        when(conversationMapper.selectById(10L)).thenReturn(c);
        when(messageMapper.selectList(any())).thenReturn(List.of());
        service.listMessages(1L, 10L);
        verify(messageMapper).selectList(any());
    }

    @Test void listConversations_delegates_to_selectPage() {
        Page<Conversation> page = new Page<>(1, 20);
        page.setRecords(List.of(new Conversation()));
        when(conversationMapper.selectPage(any(), any())).thenReturn(page);
        Page<Conversation> result = service.listConversations(1L, 1, 20);
        assertEquals(1, result.getRecords().size());
        verify(conversationMapper).selectPage(any(), any());
    }
}