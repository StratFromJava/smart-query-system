package com.example.sqs.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sqs.common.exception.BizException;
import com.example.sqs.common.result.ResultCode;
import com.example.sqs.core.entity.ChatSession;
import com.example.sqs.core.mapper.ChatSessionMapper;
import com.example.sqs.core.service.ChatMessageService;
import com.example.sqs.core.service.ChatSessionService;
import com.example.sqs.core.vo.SessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话服务实现。
 */
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private static final String DEFAULT_TITLE = "新对话";

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageService chatMessageService;

    @Override
    public SessionVO create(Long userId, String title, String model) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title == null || title.isBlank() ? DEFAULT_TITLE : title.trim());
        session.setModel(model);
        chatSessionMapper.insert(session);
        return toVO(session);
    }

    @Override
    public List<SessionVO> list(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdateTime));
        return toVOList(sessions);
    }

    @Override
    public List<SessionVO> search(Long userId, String keyword) {
        List<ChatSession> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .like(ChatSession::getTitle, keyword)
                .orderByDesc(ChatSession::getUpdateTime));
        return toVOList(sessions);
    }

    @Override
    public SessionVO get(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getUserId, userId)
                .last("LIMIT 1"));
        return session == null ? null : toVO(session);
    }

    @Override
    public void rename(Long userId, Long sessionId, String title) {
        if (title == null || title.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "标题不能为空");
        }
        checkOwner(userId, sessionId);
        ChatSession update = new ChatSession();
        update.setId(sessionId);
        update.setTitle(title.trim());
        chatSessionMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long sessionId) {
        checkOwner(userId, sessionId);
        chatMessageService.deleteBySession(userId, sessionId);
        chatSessionMapper.deleteById(sessionId);
    }

    @Override
    public void updateModel(Long userId, Long sessionId, String model) {
        checkOwner(userId, sessionId);
        ChatSession update = new ChatSession();
        update.setId(sessionId);
        update.setModel(model);
        chatSessionMapper.updateById(update);
    }

    @Override
    public void checkOwner(Long userId, Long sessionId) {
        Long count = chatSessionMapper.selectCount(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getUserId, userId));
        if (count == null || count == 0) {
            throw new BizException(ResultCode.NOT_FOUND, "会话不存在");
        }
    }

    private List<SessionVO> toVOList(List<ChatSession> sessions) {
        List<SessionVO> list = new ArrayList<>(sessions.size());
        for (ChatSession session : sessions) {
            list.add(toVO(session));
        }
        return list;
    }

    private SessionVO toVO(ChatSession session) {
        SessionVO vo = new SessionVO();
        BeanUtils.copyProperties(session, vo);
        return vo;
    }
}
