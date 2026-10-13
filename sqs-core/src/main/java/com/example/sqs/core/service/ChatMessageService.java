package com.example.sqs.core.service;

import com.example.sqs.core.entity.ChatMessage;
import com.example.sqs.core.enums.MessageRole;
import com.example.sqs.core.vo.MessageVO;

import java.util.List;

/**
 * 消息领域服务。
 */
public interface ChatMessageService {

    /**
     * 落库一条消息。
     */
    ChatMessage save(Long sessionId, Long userId, MessageRole role, String content, String model, Integer tokens);

    List<MessageVO> listBySession(Long userId, Long sessionId);

    /**
     * 取会话最近 N 条消息（正序），用于构造模型上下文。
     */
    List<ChatMessage> recent(Long userId, Long sessionId, int limit);

    /**
     * 删除会话下的所有消息（逻辑删除）。
     */
    void deleteBySession(Long userId, Long sessionId);

    /**
     * 取会话中第一条用户提问，用于自动生成会话标题。
     */
    String firstUserContent(Long userId, Long sessionId);
}
