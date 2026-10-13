package com.example.sqs.ai.service;

import com.example.sqs.core.entity.ChatMessage;
import com.example.sqs.core.enums.MessageRole;
import com.example.sqs.core.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 从数据库加载会话历史，构造模型上下文。
 *
 * <p>不使用 ChatMemory 内存态，避免与数据库双源不一致、重启丢上下文。
 */
@Component
@RequiredArgsConstructor
public class ConversationHistoryLoader {

    private final ChatMessageService chatMessageService;

    /**
     * 取会话最近 limit 条消息（正序）。
     */
    public List<Message> load(Long userId, Long sessionId, int limit) {
        List<ChatMessage> records = chatMessageService.recent(userId, sessionId, limit);
        List<Message> messages = new ArrayList<>(records.size());
        for (ChatMessage record : records) {
            String content = record.getContent();
            if (content == null) {
                continue;
            }
            if (MessageRole.ASSISTANT.getValue().equals(record.getRole())) {
                messages.add(new AssistantMessage(content));
            } else {
                messages.add(new UserMessage(content));
            }
        }
        return messages;
    }
}
