package com.windloo.ai.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windloo.ai.entity.Conversation;
import com.windloo.ai.entity.Message;
import com.windloo.ai.mapper.ConversationMapper;
import com.windloo.ai.mapper.MessageMapper;
import com.windloo.ai.service.ConversationService;
import com.windloo.common.exception.BizException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public ConversationServiceImpl(ConversationMapper cm, MessageMapper mm) {
        this.conversationMapper = cm; this.messageMapper = mm;
    }

    @Override
    public Conversation createConversation(Long userId, String title) {
        Conversation c = new Conversation();
        c.setUserId(userId);
        c.setTitle(title == null || title.isBlank() ? "新对话" : title);
        conversationMapper.insert(c);
        return c;
    }

    @Override
    public Conversation renameConversation(Long userId, Long id, String title) {
        Conversation c = requireOwned(userId, id);
        c.setTitle(title);
        conversationMapper.updateById(c);
        return c;
    }

    @Override
    public void deleteConversation(Long userId, Long id) {
        requireOwned(userId, id);
        LocalDateTime now = LocalDateTime.now();
        conversationMapper.update(null, new UpdateWrapper<Conversation>()
                .eq("id", id).set("is_deleted", 1).set("deletion_time", now));
        messageMapper.update(null, new UpdateWrapper<Message>()
                .eq("conversation_id", id).set("is_deleted", 1).set("deletion_time", now));
    }

    @Override
    public Page<Conversation> listConversations(Long userId, long page, long size) {
        Page<Conversation> p = new Page<>(page, size);
        return conversationMapper.selectPage(p, new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId).orderByDesc(Conversation::getCreateTime));
    }

    @Override
    public List<Message> listMessages(Long userId, Long conversationId) {
        requireOwned(userId, conversationId);
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId).orderByAsc(Message::getCreateTime));
    }

    @Override
    public Conversation requireOwned(Long userId, Long conversationId) {
        Conversation c = conversationMapper.selectById(conversationId);
        if (c == null || !userId.equals(c.getUserId()))
            throw new BizException(40300, "无权访问该会话");
        return c;
    }
}