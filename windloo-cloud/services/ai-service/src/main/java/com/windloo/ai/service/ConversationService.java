package com.windloo.ai.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windloo.ai.entity.Conversation;
import com.windloo.ai.entity.Message;
import java.util.List;

public interface ConversationService {
    Conversation createConversation(Long userId, String title);
    Conversation renameConversation(Long userId, Long id, String title);
    void deleteConversation(Long userId, Long id);
    Page<Conversation> listConversations(Long userId, long page, long size);
    List<Message> listMessages(Long userId, Long conversationId);
    Conversation requireOwned(Long userId, Long conversationId);
}