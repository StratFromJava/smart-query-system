package com.example.sqs.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sqs.core.entity.ChatMessage;
import com.example.sqs.core.enums.MessageRole;
import com.example.sqs.core.mapper.ChatMessageMapper;
import com.example.sqs.core.service.ChatMessageService;
import com.example.sqs.core.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage save(Long sessionId, Long userId, MessageRole role, String content,
                            String model, Integer tokens) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role.getValue());
        message.setContent(content);
        message.setModel(model);
        message.setTokens(tokens);
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<MessageVO> listBySession(Long userId, Long sessionId) {
        List<ChatMessage> messages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getUserId, userId)
                .orderByAsc(ChatMessage::getId));
        List<MessageVO> list = new ArrayList<>(messages.size());
        for (ChatMessage message : messages) {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(message, vo);
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<ChatMessage> recent(Long userId, Long sessionId, int limit) {
        return chatMessageMapper.selectRecent(sessionId, userId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySession(Long userId, Long sessionId) {
        int rows = chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getUserId, userId));
        log.debug("逻辑删除会话消息, sessionId={}, rows={}", sessionId, rows);
    }

    @Override
    public String firstUserContent(Long userId, Long sessionId) {
        List<ChatMessage> messages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getRole, MessageRole.USER.getValue())
                .orderByAsc(ChatMessage::getId)
                .last("LIMIT 1"));
        return messages.isEmpty() ? null : messages.get(0).getContent();
    }
}
