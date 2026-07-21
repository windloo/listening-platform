package com.windloo.ai.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windloo.ai.dto.ConversationDTO;
import com.windloo.ai.dto.MessageDTO;
import com.windloo.ai.entity.Conversation;
import com.windloo.ai.entity.Message;
import com.windloo.ai.service.ConversationService;
import com.windloo.common.api.JsonResponse;
import com.windloo.common.api.PageResult;
import com.windloo.common.security.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ai/conversations")
public class ConversationController {
    private final ConversationService service;
    public ConversationController(ConversationService s) { this.service = s; }

    public record TitleReq(@NotBlank String title) {}

    @PostMapping
    public JsonResponse<ConversationDTO> create(@RequestBody(required = false) TitleReq req) {
        Conversation c = service.createConversation(SecurityUtil.currentUserId(), req == null ? null : req.title());
        return JsonResponse.ok(toDTO(c));
    }

    @GetMapping
    public JsonResponse<PageResult<ConversationDTO>> list(@RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "20") long size) {
        Page<Conversation> p = service.listConversations(SecurityUtil.currentUserId(), page, size);
        List<ConversationDTO> list = p.getRecords().stream().map(this::toDTO).toList();
        return JsonResponse.ok(new PageResult<>(list, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}/messages")
    public JsonResponse<List<MessageDTO>> messages(@PathVariable Long id) {
        List<Message> msgs = service.listMessages(SecurityUtil.currentUserId(), id);
        return JsonResponse.ok(msgs.stream().map(this::toMsgDTO).toList());
    }

    @PutMapping("/{id}")
    public JsonResponse<ConversationDTO> rename(@PathVariable Long id, @RequestBody @Valid TitleReq req) {
        Conversation c = service.renameConversation(SecurityUtil.currentUserId(), id, req.title());
        return JsonResponse.ok(toDTO(c));
    }

    @DeleteMapping("/{id}")
    public JsonResponse<Void> delete(@PathVariable Long id) {
        service.deleteConversation(SecurityUtil.currentUserId(), id);
        return JsonResponse.ok();
    }

    private ConversationDTO toDTO(Conversation c) {
        ConversationDTO d = new ConversationDTO(); BeanUtils.copyProperties(c, d); return d;
    }
    private MessageDTO toMsgDTO(Message m) {
        MessageDTO d = new MessageDTO(); BeanUtils.copyProperties(m, d); return d;
    }
}